package me.xuqu.palmx.net.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import me.xuqu.palmx.common.PalmxConstants;
import me.xuqu.palmx.exception.RpcInvocationException;
import me.xuqu.palmx.net.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理响应数据包
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

    public static final Map<Integer, Promise<Object>> map = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) {
        // 从缓存中移除该序列号的 Promise
        Promise<Object> promise = map.remove(rpcResponse.getSequenceId());

        if (promise != null) {
            if (rpcResponse.getStatus() == PalmxConstants.NETTY_RPC_RESPONSE_STATUS_OK) {
                promise.setSuccess(rpcResponse.getData());
            } else {
                promise.setFailure(new RpcInvocationException(rpcResponse.getMessage()));
            }
        }
    }
}
