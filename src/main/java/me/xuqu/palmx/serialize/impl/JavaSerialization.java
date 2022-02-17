package me.xuqu.palmx.serialize.impl;

import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.serialize.Serialization;

import java.io.*;

@Slf4j
public class JavaSerialization implements Serialization {

    @Override
    public <T> byte[] serialize(T t) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(t);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Java serialize exception, data: {}, exception: {}", t, e);
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public <T> T deserialize(Class<T> tClass, byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return tClass.cast(objectInputStream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            log.error("Java deserialize exception, class: {}, bytes: {}, exception: {}", tClass, bytes, e);
            e.printStackTrace();
            return null;
        }
    }

}
