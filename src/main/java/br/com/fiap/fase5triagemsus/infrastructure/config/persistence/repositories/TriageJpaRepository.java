package br.com.fiap.fase5triagemsus.infrastructure.config.persistence.repositories;

import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
import br.com.fiap.fase5triagemsus.domain.enums.TriageStatus;
import br.com.fiap.fase5triagemsus.infrastructure.config.persistence.entities.TriageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TriageJpaRepository extends JpaRepository<TriageJpaEntity, String> {

    // ========== CONSULTAS EXISTENTES ==========
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

    // ========== NOVAS CONSULTAS PARA PROCESSAMENTO ASSÍNCRONO ==========

    /**
     * Busca triagens por status
     */
    List<TriageJpaEntity> findByStatusOrderByCreatedAtAsc(TriageStatus status);

    /**
     * Busca triagens pendentes (status PENDING)
     */
    @Query("SELECT t FROM TriageJpaEntity t " +
            "WHERE t.status = 'PENDING' " +
            "ORDER BY t.createdAt ASC")
    List<TriageJpaEntity> findPendingTriagesByStatus();

    /**
     * Busca triagens que falharam e podem ser reprocessadas
     */
    @Query("SELECT t FROM TriageJpaEntity t " +
            "WHERE t.status IN ('FAILED', 'RETRYING') " +
            "AND t.retryCount < :maxRetries " +
            "ORDER BY t.createdAt ASC")
    List<TriageJpaEntity> findRetriableTriages(@Param("maxRetries") int maxRetries);

    /**
     * Busca triagens que estão processando há muito tempo
     */
    @Query("SELECT t FROM TriageJpaEntity t " +
            "WHERE t.status = 'PROCESSING' " +
            "AND t.processingStartedAt < :thresholdTime " +
            "ORDER BY t.processingStartedAt ASC")
    List<TriageJpaEntity> findStuckProcessingTriages(@Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * Conta triagens por status em um período
     */
    @Query("SELECT COUNT(t) FROM TriageJpaEntity t " +
            "WHERE t.status = :status " +
            "AND t.createdAt BETWEEN :start AND :end")
    long countByStatusAndCreatedAtBetween(
            @Param("status") TriageStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Busca triagens críticas pendentes (removido - query JSON complexa)
     */
    @Query("SELECT t FROM TriageJpaEntity t " +
            "WHERE t.status = 'PENDING' " +
            "ORDER BY t.createdAt ASC")
    List<TriageJpaEntity> findUrgentPendingTriages();

    /**
     * Busca triagens para limpeza (antigas e finalizadas)
     */
    @Query("SELECT t FROM TriageJpaEntity t " +
            "WHERE t.status IN ('COMPLETED', 'FAILED', 'CANCELLED') " +
            "AND t.processingCompletedAt < :thresholdTime " +
            "ORDER BY t.processingCompletedAt ASC")
    List<TriageJpaEntity> findOldCompletedTriages(@Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * Estatísticas de triagens por status
     */
    @Query("SELECT t.status, COUNT(t) FROM TriageJpaEntity t " +
            "WHERE t.createdAt BETWEEN :start AND :end " +
            "GROUP BY t.status")
    List<Object[]> getTriageStatusStatistics(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Busca próximas triagens da fila (PENDING simples)
     */
    @Query("SELECT t FROM TriageJpaEntity t " +
            "WHERE t.status = 'PENDING' " +
            "ORDER BY t.createdAt ASC")
    List<TriageJpaEntity> findNextTriagesForProcessing(@Param("limit") int limit);
}