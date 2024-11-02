package com.picktoss.picktossserver.global.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import jakarta.persistence.AttributeConverter;

import java.io.IOException;
import java.util.List;

public class StringListConvert implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

    // DB에 저장 될 때 사용
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorInfo.UNABLE_TO_CONVERT_LIST_TO_STRING);
        }
    }

    // DB의 데이터를 Object로 매핑할 때 사용
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, List.class);
        } catch (IOException e) {
            throw new CustomException(ErrorInfo.UNABLE_TO_CONVERT_STRING_TO_LIST);
        }
    }
}
