package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.util.NativeAffinity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2024/1/16
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport.rdma
 */
public class XRpcResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(XRpcResourceManager.class);

    private XRpcResourceAllocator[] allocators;

    public XRpcResourceManager(int timeout) {
        this(1, timeout);
    }

    public XRpcResourceManager(int timeout, int threadCount) {
        this.allocators = new XRpcResourceAllocator[threadCount];
        for (int i = 0; i < threadCount; i++) {
            logger.info("Create a allocator, index {}, affinity {}.", i, 1 << i);
            allocators[i] = new XRpcResourceAllocator(1 << i, timeout);
            allocators[i].start();
        }
    }

    public void allocateResources(XRpcEndpoint endpoint) {
        allocators[endpoint.getClusterId()].addTask(endpoint);
    }

    public void close() throws InterruptedException {
        for (XRpcResourceAllocator allocator : allocators) {
            allocator.close();
        }
    }

    public static class XRpcResourceAllocator implements Runnable {
        private long affinity;
        private int timeout;
        private LinkedBlockingQueue<XRpcEndpoint> taskQueue;
        private boolean running;
        private Thread thread;

        public XRpcResourceAllocator(long affinity, int timeout) {
            this.affinity = affinity;
            this.timeout = timeout;
            if (timeout < 0) {
                this.timeout = Integer.MAX_VALUE;
            }
            this.taskQueue = new LinkedBlockingQueue<>();
            this.running = false;
            this.thread = new Thread(this);
        }

        public synchronized void start() {
            running = true;
            thread.start();
        }

        public void addTask(XRpcEndpoint endpoint) {
            taskQueue.add(endpoint);
        }

        @Override
        public void run() {
            NativeAffinity.setAffinity(affinity);
            while (running) {
                try {
                    XRpcEndpoint endpoint = taskQueue.poll(timeout, TimeUnit.MILLISECONDS);
                    if (null != endpoint) {
                        logger.info("Allocate Resources for endpoint.");
                        endpoint.allocateResources();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void close() throws InterruptedException {
            running = false;
            thread.join();
        }
    }

}
