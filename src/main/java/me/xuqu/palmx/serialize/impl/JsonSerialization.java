package me.xuqu.palmx.serialize.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import me.xuqu.palmx.serialize.Serialization;

import java.io.IOException;

@Slf4j
public class JsonSerialization implements Serialization {

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true).build();

    @Override
    public <T> byte[] serialize(T t) {
        try {
            return jsonMapper.writeValueAsBytes(t);
        } catch (JsonProcessingException e) {
            log.error("Json serialize exception, data: {}, exception: {}", t, e);
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public <T> T deserialize(Class<T> tClass, byte[] bytes) {
        try {
            return jsonMapper.readValue(bytes, tClass);
        } catch (IOException e) {
            log.error("Json deserialize exception, class: {}, bytes: {}, exception: {}", tClass, bytes, e);
            e.printStackTrace();
            return null;
        }
    }
}
