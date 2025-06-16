package br.com.fiap.fase5triagemsus.infrastructure.config.persistence.converters;


import br.com.fiap.fase5triagemsus.domain.valueobjects.Symptom;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Converter
public class SymptomsJsonConverter implements AttributeConverter<List<Symptom>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Symptom> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) {
            return "[]";
        }

        try {
            List<SymptomDto> dtos = symptoms.stream()
                    .map(this::toDto)
                    .toList();

            return objectMapper.writeValueAsString(dtos);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar sintomas para JSON", e);
            throw new RuntimeException("Erro ao serializar sintomas", e);
        }
    }

    @Override
    public List<Symptom> convertToEntityAttribute(String json) {
        if (json == null || json.trim().isEmpty() || "[]".equals(json.trim())) {
            return new ArrayList<>();
        }

        try {
            TypeReference<List<SymptomDto>> typeRef = new TypeReference<>() {};
            List<SymptomDto> dtos = objectMapper.readValue(json, typeRef);

            return dtos.stream()
                    .map(this::fromDto)
                    .toList();
        } catch (JsonProcessingException e) {
            log.error("Erro ao deserializar sintomas do JSON: {}", json, e);
            throw new RuntimeException("Erro ao deserializar sintomas", e);
        }
    }

    private SymptomDto toDto(Symptom symptom) {
        return new SymptomDto(
                symptom.getDescription(),
                symptom.getIntensity(),
                symptom.getLocation()
        );
    }

    private Symptom fromDto(SymptomDto dto) {
        return Symptom.of(dto.description, dto.intensity, dto.location);
    }

    private record SymptomDto(
            String description,
            Integer intensity,
            String location
    ) {}
}