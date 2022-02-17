package me.xuqu.palmx.net.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.common.PalmxConstants;
import me.xuqu.palmx.net.RpcInvocation;
import me.xuqu.palmx.net.RpcMessage;
import me.xuqu.palmx.net.RpcResponse;
import me.xuqu.palmx.provider.DefaultServiceProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class RpcInvocationHandler extends SimpleChannelInboundHandler<RpcInvocation> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Caught a exception", cause);
        ctx.close().syncUninterruptibly();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getInterfaceName();
        Object service = DefaultServiceProvider.getInstance().getService(serviceName);

        // 先初始化响应对象方便后面封装数据
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setSequenceId(rpcInvocation.getSequenceId());
        RpcMessage rpcMessage = new RpcMessage(rpcResponse.getSequenceId(), rpcResponse);
        rpcMessage.setMessageType(PalmxConstants.NETTY_RPC_RESPONSE_MESSAGE);

        // 如果未找到直接返回空
        if (service == null) {
            log.error("Unknown service[{}]", serviceName);
            rpcResponse.setStatus(PalmxConstants.NETTY_RPC_RESPONSE_STATUS_ERROR);
            rpcResponse.setMessage("Service[%s] instance not found".formatted(serviceName));

            channelHandlerContext.writeAndFlush(rpcMessage);
            return;
        }

        log.debug("Get service[{}] implementation, {}", serviceName, service);

        // 获取方法执行的信息
        String methodName = rpcInvocation.getMethodName();
        Class<?>[] paramTypes = rpcInvocation.getParameterTypes();
        Object[] arguments = rpcInvocation.getArguments();


        try {
            // 反射执行具体的方法
            Method method = service.getClass().getMethod(methodName, paramTypes);
            Object result = method.invoke(service, arguments);
            log.debug("Method<{}, {}> invoke successful, result = {}", methodName, arguments, result);

            // 正常情况，将方法执行的结果封装到响应结果中
            rpcResponse.setStatus(PalmxConstants.NETTY_RPC_RESPONSE_STATUS_OK);
            rpcResponse.setData(result);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("Method invoke failed, msg = {}", e.getCause().getMessage());
            // 异常情况，将异常对象封装到响应结果中
            rpcResponse.setStatus(PalmxConstants.NETTY_RPC_RESPONSE_STATUS_ERROR);
            rpcResponse.setMessage(e.getCause().getMessage());
        }

        channelHandlerContext.writeAndFlush(rpcMessage);
    }

}
