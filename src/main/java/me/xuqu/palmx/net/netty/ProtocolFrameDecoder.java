package me.xuqu.palmx.net.netty;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import static me.xuqu.palmx.common.PalmxConstants.*;

public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    public ProtocolFrameDecoder() {
        this(NETTY_MAX_FRAME_LENGTH, NETTY_MESSAGE_HEADER_LENGTH - 4, NETTY_MESSAGE_LENGTH_FIELD_LENGTH);
    }

    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }
}
