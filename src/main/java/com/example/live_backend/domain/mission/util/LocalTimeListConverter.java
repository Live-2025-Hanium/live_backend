package com.example.live_backend.domain.mission.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Converter // 이 클래스를 컨버터로 사용한다고 JPA에 알립니다.
public class LocalTimeListConverter implements AttributeConverter<List<LocalTime>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public String convertToDatabaseColumn(List<LocalTime> attribute) {
        // 엔티티의 List<LocalTime> 필드를 DB에 저장할 때 JSON 문자열로 변환
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("LocalTime 리스트를 JSON으로 변환하는 데 실패했습니다.", e);
        }
    }

    @Override
    public List<LocalTime> convertToEntityAttribute(String dbData) {
        // DB의 JSON 문자열을 엔티티의 List<LocalTime> 필드로 변환
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<LocalTime>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON을 LocalTime 리스트로 변환하는 데 실패했습니다.", e);
        }
    }
}
