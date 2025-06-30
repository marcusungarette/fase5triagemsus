package br.com.fiap.fase5triagemsus.presentation.dto.response;

import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.TriageStatus;
import br.com.fiap.fase5triagemsus.domain.valueobjects.Symptom;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TriageResponseDto {

    private String id;
    private String patientId;
    private List<SymptomResponseDto> symptoms;

    // Status fields
    private String status;
    private String statusDescription;
    private Boolean isProcessing;
    private Boolean isCompleted;
    private Boolean canBeCancelled;

    // Priority fields
    private String priority;
    private String priorityColor;
    private String priorityDescription;
    private Integer estimatedWaitTimeMinutes;

    // AI Analysis fields
    private String aiRecommendation;
    private String observations;
    private Double confidenceScore;

    // Legacy field for backward compatibility
    private Boolean processed;

    // Business logic fields
    private Boolean isCritical;
    private Boolean requiresFastAttention;
    private Boolean isUrgent;
    private Long minutesSinceCreation;

    // Processing fields
    private Integer retryCount;
    private String errorMessage;
    private Long processingTimeSeconds;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingStartedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingCompletedAt;

    public static TriageResponseDto fromDomain(Triage triage) {
        List<SymptomResponseDto> symptomDtos = triage.getSymptoms().stream()
                .map(SymptomResponseDto::fromDomain)
                .toList();

        return TriageResponseDto.builder()
                .id(triage.getId().getValue())
                .patientId(triage.getPatientId().getValue())
                .symptoms(symptomDtos)
                // Status fields
                .status(triage.getStatus() != null ? triage.getStatus().name() : TriageStatus.PENDING.name())
                .statusDescription(triage.getStatus() != null ? triage.getStatus().getDescription() : TriageStatus.PENDING.getDescription())
                .isProcessing(triage.getStatus() != null && triage.getStatus().isActiveStatus())
                .isCompleted(triage.getStatus() != null && triage.getStatus().isSuccessStatus())
                .canBeCancelled(triage.canBeCancelled())
                // Priority fields
                .priority(triage.getPriority() != null ? triage.getPriority().name() : null)
                .priorityColor(triage.getPriority() != null ? triage.getPriority().getColor() : null)
                .priorityDescription(triage.getPriority() != null ? triage.getPriority().getDescription() : null)
                .estimatedWaitTimeMinutes(triage.getEstimatedWaitTimeMinutes())
                // AI Analysis fields
                .aiRecommendation(triage.getAiRecommendation())
                .observations(triage.getObservations())
                .confidenceScore(triage.getConfidenceScore())
                // Legacy field
                .processed(triage.getProcessed())
                // Business logic fields
                .isCritical(triage.isCritical())
                .requiresFastAttention(triage.requiresFastAttention())
                .isUrgent(triage.isUrgent())
                .minutesSinceCreation(triage.getMinutesSinceCreation())
                // Processing fields
                .retryCount(triage.getRetryCount() != null ? triage.getRetryCount() : 0)
                .errorMessage(triage.getErrorMessage())
                .processingTimeSeconds(triage.getProcessingTimeSeconds())
                // Timestamps
                .createdAt(triage.getCreatedAt())
                .updatedAt(triage.getUpdatedAt())
                .processingStartedAt(triage.getProcessingStartedAt())
                .processingCompletedAt(triage.getProcessingCompletedAt())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SymptomResponseDto {
        private String description;
        private Integer intensity;
        private String location;
        private String intensityLevel;
        private String intensityDescription;
        private Boolean isSevere;
        private Boolean isModerate;
        private Boolean isMild;

        public static SymptomResponseDto fromDomain(Symptom symptom) {
            return SymptomResponseDto.builder()
                    .description(symptom.getDescription())
                    .intensity(symptom.getIntensity())
                    .location(symptom.getLocation())
                    .intensityLevel(determineIntensityLevel(symptom))
                    .intensityDescription(buildIntensityDescription(symptom))
                    .isSevere(symptom.isSevere())
                    .isModerate(symptom.isModerate())
                    .isMild(symptom.isMild())
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TriageSummaryDto {
        private String id;
        private String patientId;

        // Status fields
        private String status;
        private String statusDescription;
        private Boolean isProcessing;
        private Boolean isCompleted;

        // Priority fields
        private String priority;
        private String priorityColor;

        // Summary fields
        private Integer symptomsCount;
        private Integer severeSymptomsCount;
        private Boolean processed;
        private Boolean isCritical;
        private Boolean isUrgent;
        private Long minutesSinceCreation;

        // Processing fields
        private Integer retryCount;
        private Boolean hasError;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;

        public static TriageSummaryDto fromDomain(Triage triage) {
            return TriageSummaryDto.builder()
                    .id(triage.getId().getValue())
                    .patientId(triage.getPatientId().getValue())
                    // Status fields
                    .status(triage.getStatus() != null ? triage.getStatus().name() : null)
                    .statusDescription(triage.getStatus() != null ? triage.getStatus().getDescription() : null)
                    .isProcessing(triage.getStatus() != null && triage.getStatus().isActiveStatus())
                    .isCompleted(triage.getStatus() != null && triage.getStatus().isSuccessStatus())
                    // Priority fields
                    .priority(triage.getPriority() != null ? triage.getPriority().name() : null)
                    .priorityColor(triage.getPriority() != null ? triage.getPriority().getColor() : null)
                    // Summary fields
                    .symptomsCount(triage.getSymptoms().size())
                    .severeSymptomsCount((int) triage.countSevereSymptoms())
                    .processed(triage.getProcessed())
                    .isCritical(triage.isCritical())
                    .isUrgent(triage.isUrgent())
                    .minutesSinceCreation(triage.getMinutesSinceCreation())
                    // Processing fields
                    .retryCount(triage.getRetryCount())
                    .hasError(triage.getErrorMessage() != null)
                    // Timestamps
                    .createdAt(triage.getCreatedAt())
                    .updatedAt(triage.getUpdatedAt())
                    .build();
        }
    }
}