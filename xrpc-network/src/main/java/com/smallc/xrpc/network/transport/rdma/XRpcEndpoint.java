package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.RdmaEndpoint;
import com.ibm.disni.util.MemoryUtils;
import com.ibm.disni.verbs.*;
import com.smallc.xrpc.network.codec.XRpcHeaderCodec;
import com.smallc.xrpc.network.protocol.XRpcConstant;
import com.smallc.xrpc.network.protocol.XRpcHeader;
import com.smallc.xrpc.network.protocol.XRpcMessage;

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
 * @since com.smallc.xrpc.network.transport.rdma
 */
public abstract class XRpcEndpoint extends RdmaEndpoint {

    private int clusterId;
    protected XRpcEndpointGroup group;
    private IbvMr dataMr;
    private ByteBuffer dataBuffer; // direct buffer
    private ByteBuffer[] sendBuffers;
    private ByteBuffer[] recvBuffers;
    private SVCPostSend[] sendCalls;
    private SVCPostRecv[] recvCalls;
    private ConcurrentHashMap<Integer, SVCPostSend> pendingPostSends;
    private ArrayBlockingQueue<SVCPostSend> freePostSends;
    private int bufferSize;
    private int bufferCount;

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
        this.bufferCount = group.getBufferCount();
        this.sendBuffers = new ByteBuffer[bufferCount];
        this.recvBuffers = new ByteBuffer[bufferCount];
        this.sendCalls = new SVCPostSend[bufferCount];
        this.recvCalls = new SVCPostRecv[bufferCount];

        this.pendingPostSends = new ConcurrentHashMap<>();
        this.freePostSends = new ArrayBlockingQueue<>(bufferCount);
    }

    /**
     * Pre allocate resources before connecting.
     *
     * @throws IOException
     */
    @Override
    protected synchronized void init() throws IOException {
        // Allocate and register memory
        dataBuffer = ByteBuffer.allocateDirect(bufferSize * bufferCount * 2);
        dataMr = registerMemory(dataBuffer).execute().free().getMr();

        // Split into two memory blocks of the same size
        int offset = bufferSize * bufferCount;
        dataBuffer.limit(dataBuffer.position() + offset);
        ByteBuffer recvBuffer = dataBuffer.slice();

        dataBuffer.position(offset);
        dataBuffer.limit(dataBuffer.position() + offset);
        ByteBuffer sendBuffer = dataBuffer.slice();

        for (int i = 0; i < bufferCount; i++) {
            recvBuffer.position(i * bufferSize);
            recvBuffer.limit(recvBuffer.position() + bufferSize);
            recvBuffers[i] = recvBuffer.slice();

            sendBuffer.position(i * bufferSize);
            sendBuffer.limit(sendBuffer.position() + bufferSize);
            sendBuffers[i] = sendBuffer.slice();

            recvCalls[i] = buildRecvWr(i);
            sendCalls[i] = buildSendWr(i);

            freePostSends.add(sendCalls[i]);
            recvCalls[i].execute(); // pre post
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
        recvCalls[index].execute();
    }

    public void freePostSend(int id) {
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

    public void handleRecvEvent(IbvWC wc) throws IOException {
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

    private void handleSendEvent(IbvWC wc) throws IOException {
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
        ArrayList<IbvSendWR> sendWRs = new ArrayList<IbvSendWR>(1);
        LinkedList<IbvSge> sgeList = new LinkedList<IbvSge>();

        IbvSge sge = new IbvSge();
        sge.setAddr(MemoryUtils.getAddress(sendBuffers[wrId]));
        sge.setLength(bufferSize);
        sge.setLkey(dataMr.getLkey());
        IbvSendWR sendWR = new IbvSendWR();

        sendWR.setSg_list(sgeList);
        sendWR.setWr_id(wrId);
        sendWRs.add(sendWR);
        sendWR.setSend_flags(IbvSendWR.IBV_SEND_SIGNALED);
        sendWR.setOpcode(IbvSendWR.IbvWrOcode.IBV_WR_SEND.ordinal());

        return postSend(sendWRs);
    }

    private SVCPostRecv buildRecvWr(int wrId) throws IOException {
        ArrayList<IbvRecvWR> recvWRs = new ArrayList<IbvRecvWR>(1);
        LinkedList<IbvSge> sgeList = new LinkedList<IbvSge>();

        IbvSge sge = new IbvSge();
        sge.setAddr(MemoryUtils.getAddress(recvBuffers[wrId]));
        sge.setLength(bufferSize);
        sge.setLkey(dataMr.getLkey());

        IbvRecvWR recvWR = new IbvRecvWR();
        recvWR.setSg_list(sgeList);
        recvWR.setWr_id(wrId);
        recvWRs.add(recvWR);

        return postRecv(recvWRs);
    }

}
