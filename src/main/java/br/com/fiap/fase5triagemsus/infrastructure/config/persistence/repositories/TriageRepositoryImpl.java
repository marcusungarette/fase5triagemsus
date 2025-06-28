package br.com.fiap.fase5triagemsus.infrastructure.config.persistence.repositories;

import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
import br.com.fiap.fase5triagemsus.domain.enums.TriageStatus;
import br.com.fiap.fase5triagemsus.domain.repositories.TriageRepository;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import br.com.fiap.fase5triagemsus.domain.valueobjects.TriageId;
import br.com.fiap.fase5triagemsus.infrastructure.config.persistence.entities.TriageJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TriageRepositoryImpl implements TriageRepository {

    private final TriageJpaRepository jpaRepository;

    // ========== MÉTODOS EXISTENTES ==========

    @Override
    public Triage save(Triage triage) {
        //log.debug("Salvando triagem: {}", triage.getId().getValue());

        try {
            TriageJpaEntity jpaEntity = jpaRepository.findById(triage.getId().getValue())
                    .map(existing -> {
                        existing.updateFromDomain(triage);
                        return existing;
                    })
                    .orElseGet(() -> TriageJpaEntity.fromDomain(triage));

            TriageJpaEntity savedEntity = jpaRepository.save(jpaEntity);
            Triage savedTriage = savedEntity.toDomain();

            //log.debug("Triagem salva com sucesso: {}", savedTriage.getId().getValue());
            return savedTriage;

        } catch (Exception e) {
            //log.error("Erro ao salvar triagem: {}", triage.getId().getValue(), e);
            throw new RuntimeException("Erro ao salvar triagem", e);
        }
    }

    @Override
    public Optional<Triage> findById(TriageId id) {
        //log.debug("Buscando triagem por ID: {}", id.getValue());

        try {
            return jpaRepository.findById(id.getValue())
                    .map(TriageJpaEntity::toDomain);
        } catch (Exception e) {
            //log.error("Erro ao buscar triagem por ID: {}", id.getValue(), e);
            throw new RuntimeException("Erro ao buscar triagem", e);
        }
    }

    @Override
    public List<Triage> findByPatientId(PatientId patientId) {
        //log.debug("Buscando triagens do paciente: {}", patientId.getValue());

        try {
            return jpaRepository.findByPatientIdOrderByCreatedAtDesc(patientId.getValue())
                    .stream()
                    .map(TriageJpaEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            //log.error("Erro ao buscar triagens do paciente: {}", patientId.getValue(), e);
            throw new RuntimeException("Erro ao buscar triagens do paciente", e);
        }
    }

    @Override
    public List<Triage> findByPriority(PriorityLevel priority) {
        //log.debug("Buscando triagens com prioridade: {}", priority);

        try {
            return jpaRepository.findByPriorityOrderByCreatedAtDesc(priority)
                    .stream()
                    .map(TriageJpaEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            //log.error("Erro ao buscar triagens por prioridade: {}", priority, e);
            throw new RuntimeException("Erro ao buscar triagens por prioridade", e);
        }
    }

    @Override
    public List<Triage> findPendingTriages() {
        //log.debug("Buscando triagens pendentes (método legado)");

        try {
            return jpaRepository.findByProcessedFalseOrderByCreatedAtAsc()
                    .stream()
                    .map(TriageJpaEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            //log.error("Erro ao buscar triagens pendentes", e);
            throw new RuntimeException("Erro ao buscar triagens pendentes", e);
        }
    }

    @Override
    public List<Triage> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        //log.debug("Buscando triagens entre {} e {}", start, end);

        try {
            return jpaRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end)
                    .stream()
                    .map(TriageJpaEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            //log.error("Erro ao buscar triagens por período", e);
            throw new RuntimeException("Erro ao buscar triagens por período", e);
        }
    }

    @Override
    public List<Triage> findCriticalTriages() {
        //log.debug("Buscando triagens críticas");

        try {
            return jpaRepository.findCriticalTriages()
                    .stream()
                    .map(TriageJpaEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            //log.error("Erro ao buscar triagens críticas", e);
            throw new RuntimeException("Erro ao buscar triagens críticas", e);
        }
    }

    @Override
    public void delete(Triage triage) {
        //log.debug("Removendo triagem: {}", triage.getId().getValue());

        try {
            jpaRepository.deleteById(triage.getId().getValue());
            //log.debug("Triagem removida com sucesso: {}", triage.getId().getValue());
        } catch (Exception e) {
            //log.error("Erro ao remover triagem: {}", triage.getId().getValue(), e);
            throw new RuntimeException("Erro ao remover triagem", e);
        }
    }

    // ========== NOVOS MÉTODOS PARA PROCESSAMENTO ASSÍNCRONO ==========

    @Override
    public List<Triage> findByStatus(TriageStatus status) {
        //log.debug("Buscando triagens com status: {}", status);

        try {
            return jpaRepository.findByStatusOrderByCreatedAtAsc(status)
                    .stream()
                    .map(TriageJpaEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            //log.error("Erro ao buscar triagens por status: {}", status, e);
            throw new RuntimeException("Erro ao buscar triagens por status", e);
        }
    }

    @Override
    public List<Triage> findPendingTriagesByStatus() {
        //log.debug("Buscando triagens pendentes por status");

        try {
            return jpaRepository.findPendingTriagesByStatus()
                    .stream()
                    .map(TriageJpaEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            //log.error("Erro ao buscar triagens pendentes por status", e);
            throw new RuntimeException("Erro ao buscar triagens pendentes por status", e);
        }
    }

    @Override
    public List<Triage> findRetriableTriages(int maxRetries) {
        //log.debug("Buscando triagens que podem ser reprocessadas (max retries: {})", maxRetries);

        try {
            return jpaRepository.findRetriableTriages(maxRetries)
                    .stream()
                    .map(TriageJpaEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            //log.error("Erro ao buscar triagens reprocessáveis", e);
            throw new RuntimeException("Erro ao buscar triagens reprocessáveis", e);
        }
    }

    @Override
    public List<Triage> findStuckProcessingTriages(LocalDateTime thresholdTime) {
        //log.debug("Buscando triagens presas em processamento antes de: {}", thresholdTime);

        try {
            return jpaRepository.findStuckProcessingTriages(thresholdTime)
                    .stream()
                    .map(TriageJpaEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            //log.error("Erro ao buscar triagens presas", e);
            throw new RuntimeException("Erro ao buscar triagens presas", e);
        }
    }

    @Override
    public long countByStatusAndPeriod(TriageStatus status, LocalDateTime start, LocalDateTime end) {
        //log.debug("Contando triagens com status {} entre {} e {}", status, start, end);

        try {
            return jpaRepository.countByStatusAndCreatedAtBetween(status, start, end);
        } catch (Exception e) {
            //log.error("Erro ao contar triagens por status e período", e);
            throw new RuntimeException("Erro ao contar triagens por status e período", e);
        }
    }

    @Override
    public List<Triage> findUrgentPendingTriages() {
        try {
            // Busca todos pendentes e filtra por urgência na aplicação
            return jpaRepository.findUrgentPendingTriages()
                    .stream()
                    .map(TriageJpaEntity::toDomain)
                    .filter(Triage::isUrgent)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar triagens urgentes pendentes", e);
        }
    }

    @Override
    public List<Triage> findNextTriagesForProcessing(int limit) {
        try {
            return jpaRepository.findNextTriagesForProcessing(limit)
                    .stream()
                    .limit(limit)
                    .map(TriageJpaEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar próximas triagens para processamento", e);
        }
    }

    @Override
    public List<Triage> findOldCompletedTriages(LocalDateTime thresholdTime) {
        //log.debug("Buscando triagens antigas finalizadas antes de: {}", thresholdTime);

        try {
            return jpaRepository.findOldCompletedTriages(thresholdTime)
                    .stream()
                    .map(TriageJpaEntity::toDomain)
                    .toList();
        } catch (Exception e) {
            //log.error("Erro ao buscar triagens antigas", e);
            throw new RuntimeException("Erro ao buscar triagens antigas", e);
        }
    }

    @Override
    public TriageStatusStatistics getStatusStatistics(LocalDateTime start, LocalDateTime end) {
        //log.debug("Calculando estatísticas de status entre {} e {}", start, end);

        try {
            List<Object[]> results = jpaRepository.getTriageStatusStatistics(start, end);

            Map<TriageStatus, Long> statusCounts = results.stream()
                    .collect(Collectors.toMap(
                            row -> (TriageStatus) row[0],
                            row -> (Long) row[1]
                    ));

            long pending = statusCounts.getOrDefault(TriageStatus.PENDING, 0L);
            long processing = statusCounts.getOrDefault(TriageStatus.PROCESSING, 0L);
            long completed = statusCounts.getOrDefault(TriageStatus.COMPLETED, 0L);
            long failed = statusCounts.getOrDefault(TriageStatus.FAILED, 0L);
            long cancelled = statusCounts.getOrDefault(TriageStatus.CANCELLED, 0L);
            long retrying = statusCounts.getOrDefault(TriageStatus.RETRYING, 0L);

            long total = pending + processing + completed + failed + cancelled + retrying;

            return new TriageStatusStatistics(
                    pending, processing, completed, failed, cancelled, retrying, total
            );
        } catch (Exception e) {
            //log.error("Erro ao calcular estatísticas de status", e);
            throw new RuntimeException("Erro ao calcular estatísticas de status", e);
        }
    }
}