package me.xuqu.palmx.net;

import lombok.Data;
import me.xuqu.palmx.common.PalmxConfig;

@Data
public class RpcMessage {
    private int sequenceId;
    private byte messageType;
    private byte serializationType;
    private Object data;

    public RpcMessage(int sequenceId, Object data) {
        this.sequenceId = sequenceId;
        this.data = data;
        this.serializationType = (byte) PalmxConfig.getSerializationType().ordinal();
    }
}
