package me.xuqu.palmx.serialize;

import me.xuqu.palmx.common.SerializationType;
import me.xuqu.palmx.serialize.impl.JavaSerialization;
import me.xuqu.palmx.serialize.impl.JsonSerialization;
import me.xuqu.palmx.serialize.impl.KryoSerialization;
import me.xuqu.palmx.serialize.impl.ProtostuffSerialization;

public interface Serialization {

    <T> byte[] serialize(T t);

    <T> T deserialize(Class<T> tClass, byte[] bytes);

    static <T> byte[] serialize(byte idx, T t) {
        return switch (SerializationType.values()[idx]) {
            case JAVA -> java().serialize(t);
            case JSON -> json().serialize(t);
            case KRYO -> kryo().serialize(t);
            case PROTOSTUFF -> protostuff().serialize(t);
        };
    }


    static <T> T deserialize(byte idx, Class<T> tClass, byte[] bytes) {
        return switch (SerializationType.values()[idx]) {
            case JAVA -> java().deserialize(tClass, bytes);
            case JSON -> json().deserialize(tClass, bytes);
            case KRYO -> kryo().deserialize(tClass, bytes);
            case PROTOSTUFF -> protostuff().deserialize(tClass, bytes);
        };
    }

    class Holder {
        static final Serialization javaSerialization = new JavaSerialization();
        static final Serialization jsonSerialization = new JsonSerialization();
        static final Serialization kryoSerialization = new KryoSerialization();
        static final Serialization protostuffSerialization = new ProtostuffSerialization();
    }

    static Serialization java() {
        return Holder.javaSerialization;
    }

    static Serialization json() {
        return Holder.jsonSerialization;
    }

    static Serialization kryo() {
        return Holder.kryoSerialization;
    }

    static Serialization protostuff() {
        return Holder.protostuffSerialization;
    }
}
