package com.nordeus.jobfair.auctionservice.auctionservice.domain.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {

    final private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> parsedJson) {
        String jsonString = null;

        try {
            jsonString = objectMapper.writeValueAsString(parsedJson);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return jsonString;
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String jsonString) {
        Map<String, Object> parsedJson = null;

        try {
            parsedJson = objectMapper.readValue(jsonString,
                    new TypeReference<HashMap<String,Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return parsedJson;
    }
}
