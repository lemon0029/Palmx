package me.xuqu.palmx.net.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import me.xuqu.palmx.net.RpcInvocation;
import me.xuqu.palmx.net.RpcMessage;
import me.xuqu.palmx.util.SequenceIdGenerator;
import org.junit.jupiter.api.Test;

public class MessageCodecTests {

    static EmbeddedChannel embeddedChannel = new EmbeddedChannel(
            new ProtocolFrameDecoder(),
            new LoggingHandler(LogLevel.DEBUG),
            new MessageCodec()
    );

    private static final RpcInvocation RPC_INVOCATION = new RpcInvocation();
    private static final RpcMessage RPC_MESSAGE;

    static {
        RPC_INVOCATION.setInterfaceName("me.xuqu.service.FooService");
        RPC_INVOCATION.setMethodName("test");
        RPC_INVOCATION.setParameterTypes(new Class[]{int.class, String.class, String.class});
        // protostuff 不会序列化 null 值，因此在反序列化时会出现错误，这是一个限制
        // RPC_INVOCATION.setArguments(new Object[]{1, null, "bar"});
        RPC_INVOCATION.setArguments(new Object[]{1, "bar", null});
        RPC_INVOCATION.setSequenceId(100);
        RPC_MESSAGE = new RpcMessage(SequenceIdGenerator.nextId(), RPC_INVOCATION);
    }

    @Test
    public void messageEncode() {
        embeddedChannel.writeOutbound(RPC_MESSAGE);
    }

    @Test
    public void messageDecode() {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, RPC_MESSAGE, buffer);

        embeddedChannel.writeInbound(buffer);
    }
}
