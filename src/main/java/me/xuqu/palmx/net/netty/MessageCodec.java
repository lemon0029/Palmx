package me.xuqu.palmx.net.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.common.PalmxConstants;
import me.xuqu.palmx.exception.PalmxException;
import me.xuqu.palmx.net.RpcInvocation;
import me.xuqu.palmx.net.RpcMessage;
import me.xuqu.palmx.net.RpcResponse;

import java.util.List;

import static me.xuqu.palmx.common.PalmxConstants.NETTY_RPC_INVOCATION_MESSAGE;
import static me.xuqu.palmx.common.PalmxConstants.NETTY_RPC_RESPONSE_MESSAGE;
import static me.xuqu.palmx.serialize.Serialization.deserialize;
import static me.xuqu.palmx.serialize.Serialization.serialize;

@Slf4j
public class MessageCodec extends ByteToMessageCodec<RpcMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) {
        byteBuf.writeInt(PalmxConstants.NETTY_MESSAGE_MAGIC_NUMBER);
        byteBuf.writeByte(PalmxConstants.NETTY_MESSAGE_VERSION);
        byteBuf.writeInt(rpcMessage.getSequenceId());
        byteBuf.writeByte(rpcMessage.getSerializationType());
        byteBuf.writeByte(rpcMessage.getMessageType());
        byteBuf.writeByte(0xff);

        byte[] data = serialize(rpcMessage.getSerializationType(), rpcMessage.getData());

        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        int magicNumber = byteBuf.readInt();

        // 可以简单的校验消息的正确性，比如说魔数
        if (magicNumber != PalmxConstants.NETTY_MESSAGE_MAGIC_NUMBER) {
            log.error("Unknown message, magic number is {}", magicNumber);
            throw new PalmxException("Magic number is wrong");
        }

        // version
        byteBuf.readByte();

        int sequenceId = byteBuf.readInt();
        byte serializedType = byteBuf.readByte();
        byte messageType = byteBuf.readByte();
        // padding: 0xff
        byteBuf.readByte();

        int length = byteBuf.readInt();

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes, 0, length);

        if (messageType == NETTY_RPC_INVOCATION_MESSAGE) {
            RpcInvocation rpcInvocation = deserialize(serializedType, RpcInvocation.class, bytes);
            rpcInvocation.setSequenceId(sequenceId);
            list.add(rpcInvocation);
        } else if (messageType == NETTY_RPC_RESPONSE_MESSAGE) {
            RpcResponse rpcResponse = deserialize(serializedType, RpcResponse.class, bytes);
            rpcResponse.setSequenceId(sequenceId);
            list.add(rpcResponse);
        }
    }
}
