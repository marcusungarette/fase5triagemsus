package br.com.fiap.fase5triagemsus.infrastructure.config.persistence.repositories;


import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.domain.repositories.PatientRepository;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import br.com.fiap.fase5triagemsus.infrastructure.config.persistence.entities.PatientJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Slf4j
@Repository
@RequiredArgsConstructor
public class PatientRepositoryImpl implements PatientRepository {

    private final PatientJpaRepository jpaRepository;

    @Override
    public Patient save(Patient patient) {
        try {
            PatientJpaEntity jpaEntity = jpaRepository.findById(patient.getId().getValue())
                    .map(existing -> {
                        existing.updateFromDomain(patient);
                        return existing;
                    })
                    .orElseGet(() -> PatientJpaEntity.fromDomain(patient));

            PatientJpaEntity savedEntity = jpaRepository.save(jpaEntity);
            Patient savedPatient = savedEntity.toDomain();
            return savedPatient;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar paciente", e);
        }
    }

    @Override
    public Optional<Patient> findById(PatientId id) {
        try {
            return jpaRepository.findById(id.getValue())
                    .map(PatientJpaEntity::toDomain);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar paciente", e);
        }
    }

    @Override
    public Optional<Patient> findByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            String cleanCpf = cpf.replaceAll("[^0-9]", "");

            return jpaRepository.findByCpf(cleanCpf)
                    .map(PatientJpaEntity::toDomain);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar paciente por CPF", e);
        }
    }

    @Override
    public boolean existsByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }

        try {
            String cleanCpf = cpf.replaceAll("[^0-9]", "");

            return jpaRepository.existsByCpf(cleanCpf);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao verificar existência de paciente", e);
        }
    }

    @Override
    public void delete(Patient patient) {
        try {
            jpaRepository.deleteById(patient.getId().getValue());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover paciente", e);
        }
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 4) {
            return "***";
        }
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        if (cleanCpf.length() != 11) {
            return "***";
        }
        return cleanCpf.substring(0, 3) + ".***.***-" + cleanCpf.substring(9);
    }
}