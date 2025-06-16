package br.com.fiap.fase5triagemsus.domain.repositories;


import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;

import java.util.Optional;


public interface PatientRepository {

    Patient save(Patient patient);
    Optional<Patient> findById(PatientId id);
    Optional<Patient> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    void delete(Patient patient);
}