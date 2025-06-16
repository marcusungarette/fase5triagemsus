package br.com.fiap.fase5triagemsus.infrastructure.config.persistence.repositories;


import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
import br.com.fiap.fase5triagemsus.domain.repositories.TriageRepository;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import br.com.fiap.fase5triagemsus.domain.valueobjects.TriageId;
import br.com.fiap.fase5triagemsus.infrastructure.config.persistence.entities.TriageJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Slf4j
@Repository
@RequiredArgsConstructor
public class TriageRepositoryImpl implements TriageRepository {

    private final TriageJpaRepository jpaRepository;

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
        //log.debug("Buscando triagens pendentes");

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
}