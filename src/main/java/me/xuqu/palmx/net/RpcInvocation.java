package me.xuqu.palmx.net;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcInvocation implements Serializable {
    private String interfaceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] arguments;

    private transient int sequenceId;
}
