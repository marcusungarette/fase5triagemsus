package br.com.fiap.fase5triagemsus.domain.entities;

import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
import br.com.fiap.fase5triagemsus.domain.enums.TriageStatus;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import br.com.fiap.fase5triagemsus.domain.valueobjects.Symptom;
import br.com.fiap.fase5triagemsus.domain.valueobjects.TriageId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Triage {

    private TriageId id;
    private PatientId patientId;
    private List<Symptom> symptoms;
    private PriorityLevel priority;
    private String aiRecommendation;
    private String observations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean processed;
    private TriageStatus status;
    private LocalDateTime processingStartedAt;
    private LocalDateTime processingCompletedAt;
    private String errorMessage;
    private Integer retryCount;
    private Double confidenceScore;
    private String rawAiResponse;

    private Triage(TriageId id, PatientId patientId, List<Symptom> symptoms,
                   PriorityLevel priority, String aiRecommendation, String observations,
                   LocalDateTime createdAt, LocalDateTime updatedAt, Boolean processed,
                   TriageStatus status, LocalDateTime processingStartedAt,
                   LocalDateTime processingCompletedAt, String errorMessage,
                   Integer retryCount, Double confidenceScore, String rawAiResponse) {
        this.id = validateId(id);
        this.patientId = validatePatientId(patientId);
        this.symptoms = new ArrayList<>(validateSymptoms(symptoms));
        this.priority = priority;
        this.aiRecommendation = aiRecommendation;
        this.observations = observations;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.processed = processed != null ? processed : false;
        this.status = status != null ? status : TriageStatus.PENDING;
        this.processingStartedAt = processingStartedAt;
        this.processingCompletedAt = processingCompletedAt;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount != null ? retryCount : 0;
        this.confidenceScore = confidenceScore;
        this.rawAiResponse = rawAiResponse;
    }

    public static Triage create(PatientId patientId, List<Symptom> symptoms) {
        LocalDateTime now = LocalDateTime.now();
        return new Triage(
                TriageId.generate(),
                patientId,
                symptoms,
                null, // Priority será definida após processamento da IA
                null,
                null,
                now,
                now,
                false,
                TriageStatus.PENDING, // Status inicial
                null,
                null,
                null,
                0,
                null,
                null
        );
    }

    public static Triage restore(TriageId id, PatientId patientId, List<Symptom> symptoms,
                                 PriorityLevel priority, String aiRecommendation, String observations,
                                 LocalDateTime createdAt, LocalDateTime updatedAt, Boolean processed) {
        return new Triage(id, patientId, symptoms, priority, aiRecommendation, observations,
                createdAt, updatedAt, processed, TriageStatus.COMPLETED, null, null, null, 0, null, null);
    }

    public static Triage restoreWithStatus(TriageId id, PatientId patientId, List<Symptom> symptoms,
                                           PriorityLevel priority, String aiRecommendation, String observations,
                                           LocalDateTime createdAt, LocalDateTime updatedAt, Boolean processed,
                                           TriageStatus status, LocalDateTime processingStartedAt,
                                           LocalDateTime processingCompletedAt, String errorMessage,
                                           Integer retryCount, Double confidenceScore, String rawAiResponse) {
        return new Triage(id, patientId, symptoms, priority, aiRecommendation, observations,
                createdAt, updatedAt, processed, status, processingStartedAt, processingCompletedAt,
                errorMessage, retryCount, confidenceScore, rawAiResponse);
    }

    public Triage withStatus(TriageStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();

        return new Triage(
                this.id, this.patientId, this.symptoms, this.priority, this.aiRecommendation,
                this.observations, this.createdAt, now, this.processed, newStatus,
                newStatus == TriageStatus.PROCESSING ? now : this.processingStartedAt,
                newStatus.isFinalStatus() ? now : this.processingCompletedAt,
                this.errorMessage, this.retryCount, this.confidenceScore, this.rawAiResponse
        );
    }

    public Triage withCompletedResult(String recommendation, PriorityLevel priorityLevel,
                                      Double confidenceScore, String rawAiResponse) {
        LocalDateTime now = LocalDateTime.now();

        return new Triage(
                this.id, this.patientId, this.symptoms, priorityLevel, recommendation,
                this.observations, this.createdAt, now, true, TriageStatus.COMPLETED,
                this.processingStartedAt, now, this.errorMessage, this.retryCount,
                confidenceScore, rawAiResponse
        );
    }

    public Triage withError(String errorMessage) {
        LocalDateTime now = LocalDateTime.now();

        return new Triage(
                this.id, this.patientId, this.symptoms, this.priority, this.aiRecommendation,
                this.observations, this.createdAt, now, false, TriageStatus.FAILED,
                this.processingStartedAt, now, errorMessage, this.retryCount,
                this.confidenceScore, this.rawAiResponse
        );
    }

    public Triage withIncrementedRetry() {
        return new Triage(
                this.id, this.patientId, this.symptoms, this.priority, this.aiRecommendation,
                this.observations, this.createdAt, LocalDateTime.now(), this.processed,
                TriageStatus.RETRYING, this.processingStartedAt, this.processingCompletedAt,
                this.errorMessage, this.retryCount + 1, this.confidenceScore, this.rawAiResponse
        );
    }

    public Triage withCancelled() {
        LocalDateTime now = LocalDateTime.now();

        return new Triage(
                this.id, this.patientId, this.symptoms, this.priority, this.aiRecommendation,
                this.observations, this.createdAt, now, false, TriageStatus.CANCELLED,
                this.processingStartedAt, now, this.errorMessage, this.retryCount,
                this.confidenceScore, this.rawAiResponse
        );
    }


    public void processWithAI(PriorityLevel priority, String recommendation) {
        if (this.status != TriageStatus.PENDING && this.status != TriageStatus.PROCESSING) {
            throw new IllegalStateException("Triagem não pode ser processada no status: " + this.status);
        }

        this.priority = validatePriority(priority);
        this.aiRecommendation = recommendation;
        this.processed = true;
        this.status = TriageStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
        this.processingCompletedAt = LocalDateTime.now();
    }

    public void addObservations(String observations) {
        this.observations = observations;
        this.updatedAt = LocalDateTime.now();
    }


    public List<Symptom> getSymptoms() {
        return Collections.unmodifiableList(symptoms);
    }

    public boolean hasSevereSymptoms() {
        return symptoms.stream().anyMatch(Symptom::isSevere);
    }

    public long countSevereSymptoms() {
        return symptoms.stream().filter(Symptom::isSevere).count();
    }

    public long countModerateSymptoms() {
        return symptoms.stream().filter(Symptom::isModerate).count();
    }

    public long countMildSymptoms() {
        return symptoms.stream().filter(Symptom::isMild).count();
    }

    public boolean isCritical() {
        return priority != null && priority.isCritical();
    }

    public boolean requiresFastAttention() {
        return priority != null && priority.requiresFastAttention();
    }

    public Integer getEstimatedWaitTimeMinutes() {
        return priority != null ? priority.getMaxWaitTimeMinutes() : null;
    }

    public long getMinutesSinceCreation() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
    }


    public boolean canBeCancelled() {
        return this.status.isCancellable();
    }

    public boolean canBeRetried(int maxRetries) {
        return this.retryCount < maxRetries &&
                (this.status == TriageStatus.FAILED || this.status == TriageStatus.RETRYING);
    }

    public boolean isReady() {
        return this.status == TriageStatus.COMPLETED &&
                this.aiRecommendation != null &&
                this.priority != null;
    }

    public boolean needsAttention() {
        if (this.status == TriageStatus.FAILED) {
            return true;
        }

        if (this.status == TriageStatus.PROCESSING && this.processingStartedAt != null) {
            return this.processingStartedAt.isBefore(LocalDateTime.now().minusMinutes(10));
        }

        return false;
    }

    public Long getProcessingTimeSeconds() {
        if (this.processingStartedAt == null) {
            return null;
        }

        LocalDateTime endTime = this.processingCompletedAt != null ?
                this.processingCompletedAt : LocalDateTime.now();

        return java.time.Duration.between(this.processingStartedAt, endTime).getSeconds();
    }

    public boolean isUrgent() {
        if (this.symptoms == null || this.symptoms.isEmpty()) {
            return false;
        }

        List<String> urgentSymptoms = List.of(
                "dor no peito", "dificuldade para respirar", "perda de consciência",
                "sangramento severo", "dor abdominal intensa", "febre alta",
                "convulsão", "paralisia", "queimadura grave"
        );

        return this.symptoms.stream()
                .anyMatch(symptom -> urgentSymptoms.stream()
                        .anyMatch(urgent -> symptom.getDescription().toLowerCase().contains(urgent.toLowerCase())));
    }

    private TriageId validateId(TriageId id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da triagem é obrigatório");
        }
        return id;
    }

    private PatientId validatePatientId(PatientId patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("ID do paciente é obrigatório");
        }
        return patientId;
    }

    private List<Symptom> validateSymptoms(List<Symptom> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) {
            throw new IllegalArgumentException("Lista de sintomas não pode estar vazia");
        }

        if (symptoms.size() > 10) {
            throw new IllegalArgumentException("Número máximo de sintomas por triagem é 10");
        }

        return symptoms;
    }

    private PriorityLevel validatePriority(PriorityLevel priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Prioridade é obrigatória");
        }
        return priority;
    }
}