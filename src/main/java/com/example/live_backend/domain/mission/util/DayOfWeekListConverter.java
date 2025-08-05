package com.example.live_backend.domain.mission.util;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Converter
public class DayOfWeekListConverter implements AttributeConverter<List<DayOfWeek>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<DayOfWeek> attribute) {

        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.FAILED_TO_CONVERT_TO_JSON);
        }
    }

    @Override
    public List<DayOfWeek> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<DayOfWeek>>() {});
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FAILED_TO_CONVERT_FROM_JSON);
        }
    }
}
