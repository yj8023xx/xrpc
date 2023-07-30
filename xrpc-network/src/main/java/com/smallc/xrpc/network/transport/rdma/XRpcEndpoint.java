package com.smallc.xrpc.network.transport.rdma;

import com.ibm.disni.RdmaEndpoint;
import com.ibm.disni.util.MemoryUtils;
import com.ibm.disni.verbs.*;
import com.smallc.xrpc.network.protocol.XRpcHeader;
import com.smallc.xrpc.network.protocol.XRpcMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/10
 * @since com.smallc.xrpc.network.transport.rdma
 */
public abstract class XRpcEndpoint extends RdmaEndpoint {

    private XRpcEndpointGroup group;
    private IbvMr dataMr;
    private ByteBuffer dataBuffer;
    private ByteBuffer sendBuffer;
    private ByteBuffer recvBuffer;
    private ByteBuffer[] sendBufs;
    private ByteBuffer[] recvBufs;
    private SVCPostSend[] sendCalls;
    private SVCPostRecv[] recvCalls;
    private ConcurrentHashMap<Integer, SVCPostSend> pendingPostSends;
    private ArrayBlockingQueue<SVCPostSend> freePostSends;
    private int bufferSize;
    private int bufferCount;

    public abstract void handleRecvEvent(XRpcMessage message);

    protected XRpcEndpoint(XRpcEndpointGroup group, RdmaCmId idPriv, boolean serverSide) throws IOException {
        super(group, idPriv, serverSide);
        this.group = group;

        this.bufferSize = group.getBufferSize();
        this.bufferCount = group.getBufferCount();
        this.sendBufs = new ByteBuffer[bufferCount];
        this.recvBufs = new ByteBuffer[bufferCount];
        this.sendCalls = new SVCPostSend[bufferCount];
        this.recvCalls = new SVCPostRecv[bufferCount];

        this.freePostSends = new ArrayBlockingQueue<>(bufferCount);
        this.pendingPostSends = new ConcurrentHashMap<>();
    }

    @Override
    protected synchronized void init() throws IOException {
        int sendBufferOffset = bufferSize * bufferCount;

        // allocate and register memory
        dataBuffer = ByteBuffer.allocateDirect(bufferSize * bufferCount * 2);
        dataMr = registerMemory(dataBuffer).execute().free().getMr();

        // split into two memory blocks of the same size
        dataBuffer.limit(dataBuffer.position() + sendBufferOffset);
        recvBuffer = dataBuffer.slice();

        dataBuffer.position(sendBufferOffset);
        dataBuffer.limit(dataBuffer.position() + sendBufferOffset);
        sendBuffer = dataBuffer.slice();

        for (int i = 0; i < bufferCount; i++) {
            recvBuffer.position(i * bufferSize);
            recvBuffer.limit(recvBuffer.position() + bufferSize);
            recvBufs[i] = recvBuffer.slice();

            sendBuffer.position(i * bufferSize);
            sendBuffer.limit(sendBuffer.position() + bufferSize);
            sendBufs[i] = sendBuffer.slice();

            recvCalls[i] = buildRecvWr(i);
            sendCalls[i] = buildSendWr(i);

            freePostSends.add(sendCalls[i]);
            recvCalls[i].execute();
        }
    }

    @Override
    public synchronized void close() throws IOException, InterruptedException {
        super.close();
        deregisterMemory(dataMr);
    }


    // TODO 对消息进行分段
    public boolean send(XRpcMessage message) throws IOException {
        SVCPostSend postSend = freePostSends.poll();
        if (null != postSend) {
            int id = (int) postSend.getWrMod(0).getWr_id();
            encode(message, sendBufs[id]);
            pendingPostSends.put(id, postSend);
            postSend.execute();
            return true;
        }
        return false;
    }

    protected void postRecv(int index) throws IOException {
        recvCalls[index].execute();
    }

    public void encode(XRpcMessage message, ByteBuffer buffer) {
        buffer.putInt(message.getHeader().getMagicNumber());
        buffer.putInt(message.getHeader().getVersion());
        buffer.putInt(message.getHeader().getHeaderLength());
        buffer.putInt(message.getHeader().getTotalLength());
        buffer.putInt(message.getHeader().getMessageTypeId());
        buffer.putInt(message.getHeader().getSerializationId());
        buffer.putInt(message.getHeader().getRequestId());
        buffer.put(message.getPayload());
    }

    public XRpcMessage decode(ByteBuffer byteBuffer) {
        int magicNumber = byteBuffer.getInt();
        int version = byteBuffer.getInt();
        int headerLength = byteBuffer.getInt();
        int totalLength = byteBuffer.getInt();
        int messageType = byteBuffer.getInt();
        int serializeMethod = byteBuffer.getInt();
        int requestId = byteBuffer.getInt();
        int payloadLength = totalLength - headerLength;
        byte[] payload = new byte[payloadLength];
        byteBuffer.get(payload);
        return new XRpcMessage(new XRpcHeader(magicNumber, version, headerLength, totalLength, messageType, serializeMethod, requestId), payload);
    }

    public void dispatchCqEvent(IbvWC wc) {
        IbvWC.IbvWcOpcode opcode = IbvWC.IbvWcOpcode.valueOf(wc.getOpcode());
        switch (opcode) {
            case IBV_WC_RECV: {
                int id = (int) wc.getWr_id();
                XRpcMessage message = decode(recvBufs[id]);
                handleRecvEvent(message);
                try {
                    postRecv(id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            break;
            case IBV_WC_SEND: {
                int id = (int) wc.getWr_id();
                try {
                    freePostSend(id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            break;
            default:
                break;
        }
    }

    private SVCPostSend buildSendWr(int wrId) throws IOException {
        ArrayList<IbvSendWR> sendWRs = new ArrayList<IbvSendWR>(1);
        LinkedList<IbvSge> sgeList = new LinkedList<IbvSge>();

        IbvSge sge = new IbvSge();
        sge.setAddr(MemoryUtils.getAddress(sendBufs[wrId]));
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
        sge.setAddr(MemoryUtils.getAddress(recvBufs[wrId]));
        sge.setLength(bufferSize);
        sge.setLkey(dataMr.getLkey());

        IbvRecvWR recvWR = new IbvRecvWR();
        recvWR.setSg_list(sgeList);
        recvWR.setWr_id(wrId);
        recvWRs.add(recvWR);

        return postRecv(recvWRs);
    }

    public void freePostSend(int id) throws IOException {
        SVCPostSend postSend = pendingPostSends.remove(id);
        this.freePostSends.add(postSend);
    }

}
