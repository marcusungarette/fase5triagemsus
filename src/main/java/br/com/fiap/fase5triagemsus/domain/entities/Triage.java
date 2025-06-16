package br.com.fiap.fase5triagemsus.domain.entities;



import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
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


    private Triage(TriageId id, PatientId patientId, List<Symptom> symptoms,
                   PriorityLevel priority, String aiRecommendation, String observations,
                   LocalDateTime createdAt, LocalDateTime updatedAt, Boolean processed) {
        this.id = validateId(id);
        this.patientId = validatePatientId(patientId);
        this.symptoms = new ArrayList<>(validateSymptoms(symptoms));
        this.priority = priority;
        this.aiRecommendation = aiRecommendation;
        this.observations = observations;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.processed = processed != null ? processed : false;
    }


    public static Triage create(PatientId patientId, List<Symptom> symptoms) {
        return new Triage(
                TriageId.generate(),
                patientId,
                symptoms,
                null, // Priority será definida após processamento da IA
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
        );
    }


    public static Triage restore(TriageId id, PatientId patientId, List<Symptom> symptoms,
                                 PriorityLevel priority, String aiRecommendation, String observations,
                                 LocalDateTime createdAt, LocalDateTime updatedAt, Boolean processed) {
        return new Triage(id, patientId, symptoms, priority, aiRecommendation, observations,
                createdAt, updatedAt, processed);
    }


    public void processWithAI(PriorityLevel priority, String recommendation) {
        if (processed) {
            throw new IllegalStateException("Triagem já foi processada");
        }

        this.priority = validatePriority(priority);
        this.aiRecommendation = recommendation;
        this.processed = true;
        this.updatedAt = LocalDateTime.now();
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