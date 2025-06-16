package br.com.fiap.fase5triagemsus.domain.repositories;


import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import br.com.fiap.fase5triagemsus.domain.valueobjects.TriageId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface TriageRepository {

    Triage save(Triage triage);
    Optional<Triage> findById(TriageId id);
    List<Triage> findByPatientId(PatientId patientId);
    List<Triage> findByPriority(PriorityLevel priority);
    List<Triage> findPendingTriages();
    List<Triage> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Triage> findCriticalTriages();
    void delete(Triage triage);
}