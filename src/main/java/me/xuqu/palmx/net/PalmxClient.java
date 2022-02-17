package me.xuqu.palmx.net;

public interface PalmxClient {

    /**
     * 发送一个请求数据包并返回一个执行结果
     *
     * @return 服务方法的执行结果
     */
    Object sendAndExpect(RpcInvocation rpcInvocation);
}
