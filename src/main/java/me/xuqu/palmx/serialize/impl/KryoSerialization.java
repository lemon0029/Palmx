package me.xuqu.palmx.serialize.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.net.RpcInvocation;
import me.xuqu.palmx.serialize.Serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class KryoSerialization implements Serialization {

    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcInvocation.class);
        kryo.register(Object[].class);
        kryo.register(Class[].class);
        kryo.register(Class.class);
        return kryo;
    });

    @Override
    public <T> byte[] serialize(T t) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, t);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (IOException e) {
            log.error("Kryo serialize exception, data: {}, exception: {}", t, e);
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public <T> T deserialize(Class<T> tClass, byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            Object o = kryo.readObject(input, tClass);
            kryoThreadLocal.remove();
            return tClass.cast(o);
        } catch (Exception e) {
            log.error("Kryo deserialize exception, class: {}, bytes: {}, exception: {}", tClass, bytes, e);
            e.printStackTrace();
            return null;
        }
    }
}
