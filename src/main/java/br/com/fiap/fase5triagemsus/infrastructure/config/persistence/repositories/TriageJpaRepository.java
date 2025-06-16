package br.com.fiap.fase5triagemsus.infrastructure.config.persistence.repositories;

import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
import br.com.fiap.fase5triagemsus.infrastructure.config.persistence.entities.TriageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface TriageJpaRepository extends JpaRepository<TriageJpaEntity, String> {

    List<TriageJpaEntity> findByPatientIdOrderByCreatedAtDesc(String patientId);
    List<TriageJpaEntity> findByPriorityOrderByCreatedAtDesc(PriorityLevel priority);
    List<TriageJpaEntity> findByProcessedFalseOrderByCreatedAtAsc();
    List<TriageJpaEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start,
            LocalDateTime end
    );


    @Query("SELECT t FROM TriageJpaEntity t " +
            "WHERE t.priority IN ('EMERGENCY', 'VERY_URGENT') " +
            "ORDER BY t.priority ASC, t.createdAt ASC")
    List<TriageJpaEntity> findCriticalTriages();


    @Query("SELECT t FROM TriageJpaEntity t " +
            "WHERE t.priority IN ('EMERGENCY', 'VERY_URGENT') " +
            "AND t.processed = true " +
            "ORDER BY t.priority ASC, t.createdAt ASC")
    List<TriageJpaEntity> findCriticalProcessedTriages();


    @Query("SELECT COUNT(t) FROM TriageJpaEntity t " +
            "WHERE t.priority = :priority " +
            "AND t.createdAt BETWEEN :start AND :end")
    long countByPriorityAndCreatedAtBetween(
            @Param("priority") PriorityLevel priority,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT t FROM TriageJpaEntity t " +
            "WHERE t.createdAt >= :startOfDay " +
            "AND t.createdAt < :endOfDay " +
            "ORDER BY t.priority ASC, t.createdAt ASC")
    List<TriageJpaEntity> findTodaysTriages(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );


    @Query("SELECT t FROM TriageJpaEntity t " +
            "WHERE t.processed = true " +
            "AND t.priority IS NOT NULL " +
            "AND t.createdAt <= :thresholdTime " +
            "ORDER BY t.priority ASC, t.createdAt ASC")
    List<TriageJpaEntity> findTriagesWaitingTooLong(@Param("thresholdTime") LocalDateTime thresholdTime);
}