package me.xuqu.palmx.serialize.impl;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.serialize.Serialization;

@Slf4j
public class ProtostuffSerialization implements Serialization {

    private final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    @Override
    @SuppressWarnings("unchecked")
    public <T> byte[] serialize(T t) {
        Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(t.getClass());
        byte[] bytes;
        try {
            bytes = ProtostuffIOUtil.toByteArray(t, schema, BUFFER);
        } finally {
            BUFFER.clear();
        }
        return bytes;
    }

    @Override
    public <T> T deserialize(Class<T> tClass, byte[] bytes) {
        Schema<T> schema = RuntimeSchema.getSchema(tClass);
        T t = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, t, schema);
        return t;
    }
}
