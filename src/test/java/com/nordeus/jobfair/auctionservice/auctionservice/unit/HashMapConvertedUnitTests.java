package com.nordeus.jobfair.auctionservice.auctionservice.unit;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.util.HashMapConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class HashMapConvertedUnitTests {

    private HashMapConverter hashMapConverter;

    public HashMapConvertedUnitTests() {
        this.hashMapConverter = new HashMapConverter();
    }

    @Test
    public void convertToDatabaseColumnTest() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("address", "123 Main Street");
        attributes.put("zipcode", 12345);

        String expectedJsonAttributes = "{\"zipcode\":12345,\"address\":\"123 Main Street\"}";

        assertThat(this.hashMapConverter.convertToDatabaseColumn(attributes)).isEqualTo(expectedJsonAttributes);
    }

    @Test
    void convertToEntityAttributeTest() {
        String jsonStringToParse = "{\"address\":\"123 Main Street\",\"zipcode\":12345}";

        Map<String, Object> expectedParsedAttributes = new HashMap<>();
        expectedParsedAttributes.put("address", "123 Main Street");
        expectedParsedAttributes.put("zipcode", 12345);

        assertThat(this.hashMapConverter.convertToEntityAttribute(jsonStringToParse)).isEqualTo(expectedParsedAttributes);
    }

}
