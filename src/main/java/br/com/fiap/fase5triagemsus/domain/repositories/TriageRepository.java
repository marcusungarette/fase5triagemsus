package br.com.fiap.fase5triagemsus.domain.repositories;

import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
import br.com.fiap.fase5triagemsus.domain.enums.TriageStatus;
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
    List<Triage> findByStatus(TriageStatus status);
    List<Triage> findPendingTriagesByStatus();
    List<Triage> findRetriableTriages(int maxRetries);
    List<Triage> findStuckProcessingTriages(LocalDateTime thresholdTime);
    long countByStatusAndPeriod(TriageStatus status, LocalDateTime start, LocalDateTime end);
    List<Triage> findUrgentPendingTriages();
    List<Triage> findNextTriagesForProcessing(int limit);
    List<Triage> findOldCompletedTriages(LocalDateTime thresholdTime);
    TriageStatusStatistics getStatusStatistics(LocalDateTime start, LocalDateTime end);

    record TriageStatusStatistics(
            long pending,
            long processing,
            long completed,
            long failed,
            long cancelled,
            long retrying,
            long total
    ) {
        public double getSuccessRate() {
            return total > 0 ? (double) completed / total * 100 : 0.0;
        }

        public double getFailureRate() {
            return total > 0 ? (double) failed / total * 100 : 0.0;
        }

        public long getActiveProcessing() {
            return processing + retrying;
        }

        public long getFinalized() {
            return completed + failed + cancelled;
        }
    }
}