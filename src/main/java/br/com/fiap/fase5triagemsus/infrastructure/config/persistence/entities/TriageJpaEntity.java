package br.com.fiap.fase5triagemsus.infrastructure.config.persistence.entities;


import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
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
        @Index(name = "idx_triage_created_at", columnList = "created_at")
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
                triage.getUpdatedAt()
        );
    }


    public Triage toDomain() {
        return Triage.restore(
                TriageId.of(this.id),
                PatientId.of(this.patientId),
                this.symptoms,
                this.priority,
                this.aiRecommendation,
                this.observations,
                this.createdAt,
                this.updatedAt,
                this.processed
        );
    }


    public void updateFromDomain(Triage triage) {
        this.symptoms = triage.getSymptoms();
        this.priority = triage.getPriority();
        this.aiRecommendation = triage.getAiRecommendation();
        this.observations = triage.getObservations();
        this.processed = triage.getProcessed();
        // updatedAt será atualizado automaticamente pelo @UpdateTimestamp
    }
}