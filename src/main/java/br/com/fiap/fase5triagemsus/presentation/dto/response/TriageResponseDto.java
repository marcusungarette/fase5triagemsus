package br.com.fiap.fase5triagemsus.presentation.dto.response;


import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.valueobjects.Symptom;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriageResponseDto {

    private String id;
    private String patientId;
    private List<SymptomResponseDto> symptoms;
    private String priority;
    private String priorityColor;
    private String priorityDescription;
    private Integer estimatedWaitTimeMinutes;
    private String aiRecommendation;
    private String observations;
    private Boolean processed;
    private Boolean isCritical;
    private Boolean requiresFastAttention;
    private Long minutesSinceCreation;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;


    public static TriageResponseDto fromDomain(Triage triage) {
        List<SymptomResponseDto> symptomDtos = triage.getSymptoms().stream()
                .map(SymptomResponseDto::fromDomain)
                .toList();

        return TriageResponseDto.builder()
                .id(triage.getId().getValue())
                .patientId(triage.getPatientId().getValue())
                .symptoms(symptomDtos)
                .priority(triage.getPriority() != null ? triage.getPriority().name() : null)
                .priorityColor(triage.getPriority() != null ? triage.getPriority().getColor() : null)
                .priorityDescription(triage.getPriority() != null ? triage.getPriority().getDescription() : null)
                .estimatedWaitTimeMinutes(triage.getEstimatedWaitTimeMinutes())
                .aiRecommendation(triage.getAiRecommendation())
                .observations(triage.getObservations())
                .processed(triage.getProcessed())
                .isCritical(triage.isCritical())
                .requiresFastAttention(triage.requiresFastAttention())
                .minutesSinceCreation(triage.getMinutesSinceCreation())
                .createdAt(triage.getCreatedAt())
                .updatedAt(triage.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SymptomResponseDto {
        private String description;
        private Integer intensity;
        private String location;
        private String intensityLevel; // "LEVE", "MODERADO", "GRAVE"
        private String intensityDescription;


        public static SymptomResponseDto fromDomain(Symptom symptom) {
            return SymptomResponseDto.builder()
                    .description(symptom.getDescription())
                    .intensity(symptom.getIntensity())
                    .location(symptom.getLocation())
                    .intensityLevel(determineIntensityLevel(symptom))
                    .intensityDescription(buildIntensityDescription(symptom))
                    .build();
        }

        private static String determineIntensityLevel(Symptom symptom) {
            if (symptom.isSevere()) {
                return "GRAVE";
            } else if (symptom.isModerate()) {
                return "MODERADO";
            } else {
                return "LEVE";
            }
        }

        private static String buildIntensityDescription(Symptom symptom) {
            String level = determineIntensityLevel(symptom);
            return String.format("%s (%d/10)", level, symptom.getIntensity());
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriageSummaryDto {
        private String id;
        private String patientId;
        private String priority;
        private String priorityColor;
        private Integer symptomsCount;
        private Boolean processed;
        private Boolean isCritical;
        private Long minutesSinceCreation;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public static TriageSummaryDto fromDomain(Triage triage) {
            return TriageSummaryDto.builder()
                    .id(triage.getId().getValue())
                    .patientId(triage.getPatientId().getValue())
                    .priority(triage.getPriority() != null ? triage.getPriority().name() : null)
                    .priorityColor(triage.getPriority() != null ? triage.getPriority().getColor() : null)
                    .symptomsCount(triage.getSymptoms().size())
                    .processed(triage.getProcessed())
                    .isCritical(triage.isCritical())
                    .minutesSinceCreation(triage.getMinutesSinceCreation())
                    .createdAt(triage.getCreatedAt())
                    .build();
        }
    }
}