package br.com.fiap.fase5triagemsus.usecases;


import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
import br.com.fiap.fase5triagemsus.domain.repositories.TriageRepository;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import br.com.fiap.fase5triagemsus.domain.valueobjects.TriageId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class FindTriageUseCase {

    private final TriageRepository triageRepository;


    @Transactional(readOnly = true)
    public Triage findById(String triageId) {
        TriageId id = TriageId.of(triageId);

        return triageRepository.findById(id)
                .orElseThrow(() -> new TriageNotFoundException("Triagem não encontrada com ID: " + triageId));
    }


    @Transactional(readOnly = true)
    public List<Triage> findByPatientId(String patientId) {
        PatientId id = PatientId.of(patientId);
        List<Triage> triages = triageRepository.findByPatientId(id);

        return triages;
    }


    @Transactional(readOnly = true)
    public List<Triage> findByPriority(PriorityLevel priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Prioridade é obrigatória");
        }
        List<Triage> triages = triageRepository.findByPriority(priority);
        return triages;
    }


    @Transactional(readOnly = true)
    public List<Triage> findCriticalTriages() {
        List<Triage> criticalTriages = triageRepository.findCriticalTriages();
        return criticalTriages;
    }


    @Transactional(readOnly = true)
    public List<Triage> findPendingTriages() {
        List<Triage> pendingTriages = triageRepository.findPendingTriages();
        return pendingTriages;
    }

    @Transactional(readOnly = true)
    public List<Triage> findPendingTriagesByStatus() {
        List<Triage> pendingTriagesByStatus = triageRepository.findPendingTriagesByStatus();
        return pendingTriagesByStatus;
    }


    @Transactional(readOnly = true)
    public List<Triage> findByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);
        List<Triage> triages = triageRepository.findByCreatedAtBetween(startOfDay, endOfDay);

        return triages;
    }


    @Transactional(readOnly = true)
    public List<Triage> findByPeriod(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Data de início e fim são obrigatórias");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        }

        List<Triage> triages = triageRepository.findByCreatedAtBetween(start, end);

        return triages;
    }


    @Transactional(readOnly = true)
    public TriageStatistics getStatisticsByDate(LocalDate date) {
        List<Triage> triagesOfDay = findByDate(date);

        long emergency = triagesOfDay.stream()
                .filter(t -> t.getPriority() == PriorityLevel.EMERGENCY)
                .count();

        long veryUrgent = triagesOfDay.stream()
                .filter(t -> t.getPriority() == PriorityLevel.VERY_URGENT)
                .count();

        long urgent = triagesOfDay.stream()
                .filter(t -> t.getPriority() == PriorityLevel.URGENT)
                .count();

        long lessUrgent = triagesOfDay.stream()
                .filter(t -> t.getPriority() == PriorityLevel.LESS_URGENT)
                .count();

        long nonUrgent = triagesOfDay.stream()
                .filter(t -> t.getPriority() == PriorityLevel.NON_URGENT)
                .count();

        long pending = triagesOfDay.stream()
                .filter(t -> !t.getProcessed())
                .count();

        return new TriageStatistics(
                date,
                triagesOfDay.size(),
                emergency,
                veryUrgent,
                urgent,
                lessUrgent,
                nonUrgent,
                pending
        );
    }




    public record TriageStatistics(
            LocalDate date,
            long total,
            long emergency,
            long veryUrgent,
            long urgent,
            long lessUrgent,
            long nonUrgent,
            long pending
    ) {
        public long getCritical() {
            return emergency + veryUrgent;
        }

        public double getCriticalPercentage() {
            return total > 0 ? (double) getCritical() / total * 100 : 0.0;
        }
    }


    public static class TriageNotFoundException extends RuntimeException {
        public TriageNotFoundException(String message) {
            super(message);
        }
    }
}