package br.com.fiap.fase5triagemsus.infrastructure.config.persistence.entities;

import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
import br.com.fiap.fase5triagemsus.domain.enums.TriageStatus;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import br.com.fiap.fase5triagemsus.domain.valueobjects.Symptom;
import br.com.fiap.fase5triagemsus.domain.valueobjects.TriageId;
import br.com.fiap.fase5triagemsus.infrastructure.config.persistence.converters.SymptomsJsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "triages", indexes = {
        @Index(name = "idx_triage_patient_id", columnList = "patient_id"),
        @Index(name = "idx_triage_priority", columnList = "priority"),
        @Index(name = "idx_triage_processed", columnList = "processed"),
        @Index(name = "idx_triage_status", columnList = "status"), // Novo índice
        @Index(name = "idx_triage_created_at", columnList = "created_at"),
        @Index(name = "idx_triage_retry_count", columnList = "retry_count") // Novo índice
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TriageJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "symptoms", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = SymptomsJsonConverter.class)
    private List<Symptom> symptoms;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private PriorityLevel priority;

    @Column(name = "ai_recommendation", columnDefinition = "TEXT")
    private String aiRecommendation;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "processed", nullable = false)
    private Boolean processed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TriageStatus status;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "raw_ai_response", columnDefinition = "TEXT")
    private String rawAiResponse;


    public static TriageJpaEntity fromDomain(Triage triage) {
        return new TriageJpaEntity(
                triage.getId().getValue(),
                triage.getPatientId().getValue(),
                triage.getSymptoms(),
                triage.getPriority(),
                triage.getAiRecommendation(),
                triage.getObservations(),
                triage.getProcessed(),
                triage.getCreatedAt(),
                triage.getUpdatedAt(),
                triage.getStatus() != null ? triage.getStatus() : TriageStatus.PENDING,
                triage.getProcessingStartedAt(),
                triage.getProcessingCompletedAt(),
                triage.getErrorMessage(),
                triage.getRetryCount() != null ? triage.getRetryCount() : 0,
                triage.getConfidenceScore(),
                triage.getRawAiResponse()
        );
    }

    public Triage toDomain() {
        return Triage.restoreWithStatus(
                TriageId.of(this.id),
                PatientId.of(this.patientId),
                this.symptoms,
                this.priority,
                this.aiRecommendation,
                this.observations,
                this.createdAt,
                this.updatedAt,
                this.processed,
                this.status,
                this.processingStartedAt,
                this.processingCompletedAt,
                this.errorMessage,
                this.retryCount,
                this.confidenceScore,
                this.rawAiResponse
        );
    }

    public void updateFromDomain(Triage triage) {
        this.symptoms = triage.getSymptoms();
        this.priority = triage.getPriority();
        this.aiRecommendation = triage.getAiRecommendation();
        this.observations = triage.getObservations();
        this.processed = triage.getProcessed();
        this.status = triage.getStatus() != null ? triage.getStatus() : TriageStatus.PENDING;
        this.processingStartedAt = triage.getProcessingStartedAt();
        this.processingCompletedAt = triage.getProcessingCompletedAt();
        this.errorMessage = triage.getErrorMessage();
        this.retryCount = triage.getRetryCount() != null ? triage.getRetryCount() : 0;
        this.confidenceScore = triage.getConfidenceScore();
        this.rawAiResponse = triage.getRawAiResponse();
        // updatedAt será atualizado automaticamente pelo @UpdateTimestamp
    }
}