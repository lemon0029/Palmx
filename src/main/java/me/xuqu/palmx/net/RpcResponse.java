package me.xuqu.palmx.net;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponse implements Serializable {
    private byte status;
    private Object data;
    private String message;
    private transient int sequenceId;
}
