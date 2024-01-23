package com.smallc.xrpc.demo.benchmark;

import com.smallc.xrpc.api.hello.DataTransferService;
import com.smallc.xrpc.client.XRpcClient;
import com.smallc.xrpc.common.serializer.SerializationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2024/1/14
 * @since com.smallc.xrpc.network.transport.TransportServer.demo.benchmark
 */
public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private XRpcClient client;
    private URI serviceUri;

    public void init(String host, int port) {
        logger.info("Create xRPC client instance...");
        client = new XRpcClient();
        serviceUri = URI.create("rpc://" + host + ":" + port);
    }

    /**
     * Test Latency
     *
     * @param sendCount
     * @param dataSize  byte
     */
    public void testLatency(int sendCount, int dataSize) {
        byte[] data = new byte[dataSize];
        DataTransferService dataTransferService = client.getRemoteService(DataTransferService.class, serviceUri, SerializationType.JSON);
        double drop_rate = 0.1;
        int drop = (int) (sendCount * drop_rate);
        long duration = 0;
        long startTime, endTime;
        for (int i = 0; i < sendCount; i++) {
            startTime = System.nanoTime();
            dataTransferService.send(data.toString());
            endTime = System.nanoTime();
            if (i > drop) {
                duration += (endTime - startTime);
            }
        }
        long latency = duration / (sendCount - drop) / 1000; // us
        logger.warn("RPC Latency: {} us [sendCount: {}, dataSize: {}B]", latency, sendCount, dataSize);
    }

    /**
     * Test Throughput
     *
     * @param executionTime second
     * @param threadCount
     * @param dataSize
     */
    public void testThroughput(long executionTime, int threadCount, int dataSize) throws InterruptedException {
        long[] ops = new long[threadCount];
        Thread[] workers = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            DataTransferService dataTransferService = client.getRemoteService(DataTransferService.class, serviceUri, SerializationType.JSON);
            assert dataTransferService != null;
            workers[i] = new Thread(new Throughput(i, executionTime, dataSize, ops, dataTransferService));
            workers[i].start();
        }
        long tot = 0;
        for (int i = 0; i < threadCount; i++) {
            workers[i].join();
            tot += ops[i];
        }
        logger.info("RPC Throughput: {} ops [executionTime: {}s, threadCount: {}, dataSize: {}B]", tot / executionTime, executionTime, threadCount, dataSize);
    }

    private class Throughput implements Runnable {

        private int id;
        private long executionTime;
        private int dataSize;
        private long[] ops;
        private DataTransferService dataTransferService;

        public Throughput(int id, long executionTime, int dataSize, long[] ops, DataTransferService dataTransferService) {
            this.id = id;
            this.executionTime = executionTime;
            this.dataSize = dataSize;
            this.ops = ops;
            this.dataTransferService = dataTransferService;
        }

        @Override
        public void run() {
            byte[] data = new byte[dataSize];
            long diff = 0;
            long timeout = executionTime * 1000000; // us
            long startTime = System.nanoTime();
            while (diff < timeout) {
                dataTransferService.send(data.toString());
                ops[id]++;
                diff = (System.nanoTime() - startTime) / 1000; // us
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Check if enough command-line arguments are provided
        if (args.length < 2) {
            System.out.println("Usage: java Client <ip-address> <port>");
            System.exit(1); // Exit the program indicating insufficient parameters
        }

        // Retrieve command-line arguments
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client();
        client.init(host, port);
        client.testLatency(10000, 64);
        //client.testThroughput(10, 16, 64);
    }

}
