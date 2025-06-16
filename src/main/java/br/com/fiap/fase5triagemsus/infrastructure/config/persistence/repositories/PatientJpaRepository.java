package br.com.fiap.fase5triagemsus.infrastructure.config.persistence.repositories;


import br.com.fiap.fase5triagemsus.infrastructure.config.persistence.entities.PatientJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PatientJpaRepository extends JpaRepository<PatientJpaEntity, String> {


    Optional<PatientJpaEntity> findByCpf(String cpf);
    boolean existsByCpf(String cpf);

    @Query("SELECT p FROM PatientJpaEntity p WHERE p.cpf = :cpf")
    Optional<PatientJpaEntity> findPatientByCpf(@Param("cpf") String cpf);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM PatientJpaEntity p WHERE p.cpf = :cpf AND p.id != :id")
    boolean existsByCpfAndIdNot(@Param("cpf") String cpf, @Param("id") String id);
}