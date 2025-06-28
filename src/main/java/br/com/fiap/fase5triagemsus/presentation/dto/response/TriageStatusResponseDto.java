package br.com.fiap.fase5triagemsus.presentation.dto.response;

import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.TriageStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TriageStatusResponseDto {

    private String id;
    private String patientId;

    // Status information
    private String status;
    private String statusDescription;
    private Boolean isProcessing;
    private Boolean isCompleted;
    private Boolean isFailed;
    private Boolean isCancelled;
    private Boolean isPending;
    private Boolean canBeCancelled;
    private Boolean canBeRetried;

    // Processing information
    private Integer retryCount;
    private Integer maxRetries;
    private String errorMessage;
    private Long processingTimeSeconds;
    private Double confidenceScore;
    private Boolean needsAttention;

    // Priority information (if available)
    private String priority;
    private String priorityColor;
    private Boolean isCritical;
    private Boolean isUrgent;

    // Progress information
    private String progressMessage;
    private Integer progressPercentage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingStartedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingCompletedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedCompletionAt;

    public static TriageStatusResponseDto fromDomain(Triage triage) {
        return fromDomain(triage, 3); // Default max retries
    }

    public static TriageStatusResponseDto fromDomain(Triage triage, int maxRetries) {
        if (triage == null) {
            throw new IllegalArgumentException("Triage cannot be null");
        }

        TriageStatus status = triage.getStatus();
        if (status == null) {
            throw new IllegalStateException("Triage status cannot be null");
        }

        return TriageStatusResponseDto.builder()
                .id(triage.getId().getValue())
                .patientId(triage.getPatientId().getValue())
                // Status information
                .status(status.name())
                .statusDescription(status.getDescription())
                .isProcessing(status.isActiveStatus())
                .isCompleted(status.isSuccessStatus())
                .isFailed(status.isErrorStatus())
                .isCancelled(status == TriageStatus.CANCELLED)
                .isPending(status == TriageStatus.PENDING)
                .canBeCancelled(triage.canBeCancelled())
                .canBeRetried(triage.canBeRetried(maxRetries))
                // Processing information
                .retryCount(triage.getRetryCount())
                .maxRetries(maxRetries)
                .errorMessage(triage.getErrorMessage())
                .processingTimeSeconds(triage.getProcessingTimeSeconds())
                .confidenceScore(triage.getConfidenceScore())
                .needsAttention(triage.needsAttention())
                // Priority information
                .priority(triage.getPriority() != null ? triage.getPriority().name() : null)
                .priorityColor(triage.getPriority() != null ? triage.getPriority().getColor() : null)
                .isCritical(triage.isCritical())
                .isUrgent(triage.isUrgent())
                // Progress information
                .progressMessage(buildProgressMessage(triage))
                .progressPercentage(calculateProgressPercentage(triage))
                // Timestamps
                .createdAt(triage.getCreatedAt())
                .updatedAt(triage.getUpdatedAt())
                .processingStartedAt(triage.getProcessingStartedAt())
                .processingCompletedAt(triage.getProcessingCompletedAt())
                .estimatedCompletionAt(calculateEstimatedCompletion(triage))
                .build();
    }

    private static String buildProgressMessage(Triage triage) {
        TriageStatus status = triage.getStatus();

        return switch (status) {
            case PENDING -> "Triagem na fila aguardando processamento";
            case PROCESSING -> "Analisando sintomas com inteligência artificial...";
            case RETRYING -> String.format("Tentativa %d de processamento em andamento",
                    triage.getRetryCount() + 1);
            case COMPLETED -> "Triagem concluída com sucesso";
            case FAILED -> "Falha no processamento após " + triage.getRetryCount() + " tentativas";
            case CANCELLED -> "Triagem cancelada pelo usuário";
        };
    }

    private static Integer calculateProgressPercentage(Triage triage) {
        TriageStatus status = triage.getStatus();

        return switch (status) {
            case PENDING -> 10;
            case PROCESSING -> 50;
            case RETRYING -> 30 + (triage.getRetryCount() * 10);
            case COMPLETED -> 100;
            case FAILED -> 0;
            case CANCELLED -> 0;
        };
    }

    private static LocalDateTime calculateEstimatedCompletion(Triage triage) {
        if (triage.getStatus().isFinalStatus()) {
            return triage.getProcessingCompletedAt();
        }

        if (triage.getStatus() == TriageStatus.PROCESSING && triage.getProcessingStartedAt() != null) {
            // Estima 2 minutos para processamento
            return triage.getProcessingStartedAt().plusMinutes(2);
        }

        if (triage.getStatus() == TriageStatus.PENDING) {
            // Estima baseado na urgência
            int estimatedMinutes = triage.isUrgent() ? 1 : 5;
            return LocalDateTime.now().plusMinutes(estimatedMinutes);
        }

        return null;
    }

//    // Métodos utilitários para facilitar uso no frontend
//    public boolean isInProgress() {
//        return Boolean.TRUE.equals(isProcessing) || Boolean.TRUE.equals(isPending);
//    }
//
//    public boolean hasResult() {
//        return Boolean.TRUE.equals(isCompleted);
//    }
//
//    public boolean hasError() {
//        return Boolean.TRUE.equals(isFailed) || errorMessage != null;
//    }
//
//    public String getStatusColor() {
//        if (Boolean.TRUE.equals(isCompleted)) return "green";
//        if (Boolean.TRUE.equals(isFailed)) return "red";
//        if (Boolean.TRUE.equals(isCancelled)) return "gray";
//        if (Boolean.TRUE.equals(isProcessing)) return "blue";
//        if (Boolean.TRUE.equals(isPending)) return "yellow";
//        return "gray";
//    }
}