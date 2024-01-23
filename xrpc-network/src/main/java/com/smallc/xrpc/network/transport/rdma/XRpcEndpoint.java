package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.RdmaEndpoint;
import com.ibm.disni.util.MemoryUtils;
import com.ibm.disni.verbs.*;
import com.smallc.xrpc.network.codec.XRpcHeaderCodec;
import com.smallc.xrpc.network.protocol.XRpcConstant;
import com.smallc.xrpc.network.protocol.XRpcHeader;
import com.smallc.xrpc.network.protocol.XRpcMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/10
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport.rdma
 */
public abstract class XRpcEndpoint extends RdmaEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(XRpcEndpoint.class);

    private int clusterId;
    protected XRpcEndpointGroup group;
    private IbvMr dataMr;
    private ByteBuffer dataBuffer; // direct buffer
    private int bufferSize;
    private int sendWrCount;
    private int recvWrCount;
    private ByteBuffer[] sendBuffers;
    private ByteBuffer[] recvBuffers;
    private SVCPostSend[] sendCalls;
    private SVCPostRecv[] recvCalls;
    private ConcurrentHashMap<Integer, SVCPostSend> pendingPostSends;
    private ArrayBlockingQueue<SVCPostSend> freePostSends;

    // Used for receiving data
    private int recvOffset;
    private byte[] recvData;

    public enum State {
        INITIAL, MERGING
    }

    private State state;

    public abstract void onMessageComplete(XRpcMessage message);

    protected XRpcEndpoint(XRpcEndpointGroup group, RdmaCmId idPriv, boolean serverSide) throws IOException {
        super(group, idPriv, serverSide);
        this.clusterId = group.newClusterId();
        this.group = group;

        this.bufferSize = group.getBufferSize();
        this.sendWrCount = group.getMaxSendWr();
        this.recvWrCount = group.getMaxRecvWr();
        this.sendBuffers = new ByteBuffer[sendWrCount];
        this.recvBuffers = new ByteBuffer[recvWrCount];
        this.sendCalls = new SVCPostSend[sendWrCount];
        this.recvCalls = new SVCPostRecv[recvWrCount];

        this.pendingPostSends = new ConcurrentHashMap<>();
        this.freePostSends = new ArrayBlockingQueue<>(sendWrCount);

        this.state = State.INITIAL;
    }

    /**
     * Pre allocate resources before connecting.
     *
     * @throws IOException
     */
    @Override
    protected synchronized void init() throws IOException {
        // Allocate and register memory
        dataBuffer = ByteBuffer.allocateDirect(bufferSize * (sendWrCount + recvWrCount));
        dataMr = registerMemory(dataBuffer).execute().free().getMr();

        // Split into two memory blocks
        int offset = bufferSize * sendWrCount;
        dataBuffer.limit(dataBuffer.position() + offset);
        ByteBuffer sendBuffer = dataBuffer.slice();

        dataBuffer.position(offset);
        dataBuffer.limit(dataBuffer.capacity());
        ByteBuffer recvBuffer = dataBuffer.slice();

        for (int i = 0; i < sendWrCount; i++) {
            sendBuffer.position(i * bufferSize);
            sendBuffer.limit(sendBuffer.position() + bufferSize);
            sendBuffers[i] = sendBuffer.slice();

            sendCalls[i] = buildSendWr(i);
            freePostSends.add(sendCalls[i]);
        }

        for (int i = 0; i < recvWrCount; i++) {
            recvBuffer.position(i * bufferSize);
            recvBuffer.limit(recvBuffer.position() + bufferSize);
            recvBuffers[i] = recvBuffer.slice();

            recvCalls[i] = buildRecvWr(i);
            recvCalls[i].execute(); // Pre-post recv wr
        }
    }

    @Override
    public synchronized void close() throws IOException, InterruptedException {
        super.close();
        deregisterMemory(dataMr);
    }

    public int getClusterId() {
        return clusterId;
    }

    private byte[] encode(XRpcMessage message) {
        ByteBuffer buffer = ByteBuffer.allocate(message.getHeader().getTotalLength());
        buffer.put(XRpcHeaderCodec.encode(message.getHeader()));
        buffer.put(message.getPayload());
        return buffer.array();
    }

    private XRpcMessage decode(byte[] data) {
        // logger.info("Receive data length: {}", data.length);
        int splitIndex = XRpcConstant.FIXED_HEADER_LENGTH;
        byte[] headerBytes = Arrays.copyOfRange(data, 0, splitIndex);
        byte[] payload = Arrays.copyOfRange(data, splitIndex, data.length);
        XRpcHeader header = XRpcHeaderCodec.decode(headerBytes);
        return new XRpcMessage(header, payload);
    }

    private List<byte[]> sliceData(byte[] data, int chunkSize) {
        List<byte[]> chunkList = new ArrayList<>();
        int offset = 0;
        while (offset < data.length) {
            int size = Math.min(chunkSize, data.length - offset);
            byte[] chunk = new byte[size];
            System.arraycopy(data, offset, chunk, 0, size);
            chunkList.add(chunk);
            offset += size;
        }
        return chunkList;
    }

    public synchronized boolean send(XRpcMessage message) throws IOException, InterruptedException {
        byte[] data = encode(message);
        short totalSize = (short) data.length;
        ByteBuffer newData = ByteBuffer.allocate(totalSize + Short.BYTES);
        // Add a header field to represent the size of the data
        newData.putShort(totalSize).put(data);
        // Slice message
        List<byte[]> chunkList = sliceData(newData.array(), bufferSize);
        // Send chunks
        for (byte[] chunk : chunkList) {
            SVCPostSend postSend = freePostSends.take(); // TODO: take or poll
            if (null != postSend) {
                int id = (int) postSend.getWrMod(0).getWr_id();
                postSend.getWrMod(0).getSgeMod(0).setLength(chunk.length);
                postSend.getWrMod(0).setSend_flags(IbvSendWR.IBV_SEND_SIGNALED);
                if (chunk.length <= group.getMaxInlineData()) {
                    postSend.getWrMod(0).setSend_flags(postSend.getWrMod(0).getSend_flags() | IbvSendWR.IBV_SEND_INLINE);
                }
                sendBuffers[id].position(0);
                sendBuffers[id].put(chunk);
                pendingPostSends.put(id, postSend);
                postSend.execute();
            } else {
                return false;
            }
        }
        return true;
    }

    protected void postRecv(int index) throws IOException {
        // logger.info("Post recv: {}", index);
        recvBuffers[index].clear();
        recvCalls[index].execute();
    }

    protected void freePostSend(int id) {
        SVCPostSend postSend = pendingPostSends.remove(id);
        this.freePostSends.add(postSend);
    }

    private void mergeDataFsm(ByteBuffer chunkBuffer) {
        switch (state) {
            case INITIAL: {
                short totalSize = chunkBuffer.getShort();
                recvData = new byte[totalSize];
                int length = Math.min(totalSize, chunkBuffer.remaining());
                chunkBuffer.get(recvData, 0, length);
                recvOffset = length;
                state = State.MERGING;
            }
            break;
            case MERGING: {
                int length = Math.min(recvData.length - recvOffset, chunkBuffer.remaining());
                chunkBuffer.get(recvData, recvOffset, length);
                recvOffset += length;
            }
            break;
        }
    }

    protected void handleRecvEvent(IbvWC wc) throws IOException {
        int id = (int) wc.getWr_id();
        // Merge data chunk
        mergeDataFsm(recvBuffers[id]);
        postRecv(id);
        if (recvOffset >= recvData.length) {
            XRpcMessage message = decode(recvData);
            onMessageComplete(message);
            // Start handling next message
            state = State.INITIAL;
        }
    }

    protected void handleSendEvent(IbvWC wc) throws IOException {
        // logger.info("Free post send.");
        int id = (int) wc.getWr_id();
        freePostSend(id); // TODO whether release
    }

    /**
     * Only execute in a single thread
     *
     * @param wc
     * @throws IOException
     */
    public void dispatchCqEvent(IbvWC wc) throws IOException {
        if (wc.getStatus() == 5) {
            return;
        } else if (wc.getStatus() != 0) {
            throw new IOException("Faulty operation! wc.status " + wc.getStatus());
        }

        IbvWC.IbvWcOpcode opcode = IbvWC.IbvWcOpcode.valueOf(wc.getOpcode());
        switch (opcode) {
            case IBV_WC_RECV:
                handleRecvEvent(wc);
                break;
            case IBV_WC_SEND:
                handleSendEvent(wc);
                break;
            default:
                throw new IOException("Unknown opcode: " + wc.getOpcode());
        }
    }

    private SVCPostSend buildSendWr(int wrId) throws IOException {
        ArrayList<IbvSendWR> sendWrList = new ArrayList<IbvSendWR>(1);
        LinkedList<IbvSge> sgeList = new LinkedList<IbvSge>();

        IbvSge sge = new IbvSge();
        sge.setAddr(MemoryUtils.getAddress(sendBuffers[wrId]));
        sge.setLength(bufferSize);
        sge.setLkey(dataMr.getLkey());
        sgeList.add(sge);

        IbvSendWR sendWr = new IbvSendWR();
        sendWr.setSg_list(sgeList);
        sendWr.setWr_id(wrId);
        sendWrList.add(sendWr);
        sendWr.setSend_flags(IbvSendWR.IBV_SEND_SIGNALED);
        sendWr.setOpcode(IbvSendWR.IbvWrOcode.IBV_WR_SEND.ordinal());

        return postSend(sendWrList);
    }

    private SVCPostRecv buildRecvWr(int wrId) throws IOException {
        ArrayList<IbvRecvWR> recvWrList = new ArrayList<IbvRecvWR>(1);
        LinkedList<IbvSge> sgeList = new LinkedList<IbvSge>();

        IbvSge sge = new IbvSge();
        sge.setAddr(MemoryUtils.getAddress(recvBuffers[wrId]));
        sge.setLength(bufferSize);
        sge.setLkey(dataMr.getLkey());
        sgeList.add(sge);

        IbvRecvWR recvWr = new IbvRecvWR();
        recvWr.setSg_list(sgeList);
        recvWr.setWr_id(wrId);
        recvWrList.add(recvWr);

        return postRecv(recvWrList);
    }

}
