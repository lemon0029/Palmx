package me.xuqu.palmx.net;

import me.xuqu.palmx.common.PalmxConstants;
import me.xuqu.palmx.util.SequenceIdGenerator;

public abstract class AbstractPalmxClient implements PalmxClient {
    @Override
    public Object sendAndExpect(RpcInvocation rpcInvocation) {
        RpcMessage rpcMessage = new RpcMessage(SequenceIdGenerator.nextId(), rpcInvocation);
        rpcMessage.setMessageType(PalmxConstants.NETTY_RPC_INVOCATION_MESSAGE);
        return doSend(rpcMessage);
    }

    protected abstract Object doSend(RpcMessage rpcMessage);
}
