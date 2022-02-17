package me.xuqu.palmx.serialize;

import me.xuqu.palmx.net.RpcInvocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class SerializationTests {

    public static final RpcInvocation RPC_INVOCATION = new RpcInvocation();

    static {
        RPC_INVOCATION.setInterfaceName("me.xuqu.service.FooService");
        RPC_INVOCATION.setMethodName("test");
        RPC_INVOCATION.setParameterTypes(new Class[]{int.class, String.class, String.class});
        // protostuff 不会序列化 null 值，因此在反序列化时会出现错误，这是一个限制
        // RPC_INVOCATION.setArguments(new Object[]{1, null, "bar"});
        RPC_INVOCATION.setArguments(new Object[]{1, "bar", null});
        RPC_INVOCATION.setSequenceId(100);
    }

    @Test
    public void java() {
        switcher(Serialization.java());
    }

    @Test
    public void json() {
        switcher(Serialization.json());
    }

    @Test
    public void kryo() {
        switcher(Serialization.kryo());
    }

    @Test
    public void protostuff() {
        switcher(Serialization.protostuff());
    }

    public void switcher(Serialization serialization) {
        byte[] bytes = serialization.serialize(RPC_INVOCATION);
        Assertions.assertNotEquals(0, bytes.length);

        RpcInvocation rpcInvocation = serialization.deserialize(RpcInvocation.class, bytes);

        Assertions.assertTrue(Arrays.deepEquals(rpcInvocation.getArguments(), RPC_INVOCATION.getArguments()));
        Assertions.assertTrue(Arrays.deepEquals(rpcInvocation.getParameterTypes(), RPC_INVOCATION.getParameterTypes()));
        Assertions.assertEquals(rpcInvocation.getMethodName(), RPC_INVOCATION.getMethodName());
        Assertions.assertEquals(rpcInvocation.getInterfaceName(), RPC_INVOCATION.getInterfaceName());
        Assertions.assertEquals(0, rpcInvocation.getSequenceId());
    }

}
