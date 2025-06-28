package br.com.fiap.fase5triagemsus.presentation.dto.response;

import br.com.fiap.fase5triagemsus.infrastructure.services.queue.QueueService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueStatusDto {

    // Queue counts
    private long pendingCount;
    private long processingCount;
    private long completedCount;
    private long failedCount;
    private long retryCount;
    private long deadLetterCount;
    private long cancelledCount;

    // Calculated metrics
    private long totalProcessed;
    private long activeCount;
    private double successRate;
    private double failureRate;
    private double retryRate;

    // Performance metrics
    private Double averageProcessingTimeSeconds;
    private Double throughputPerMinute;
    private Integer estimatedWaitTimeMinutes;

    // Health indicators
    private QueueHealth health;
    private String healthDescription;
    private Boolean hasBacklog;
    private Boolean needsAttention;

    // System status
    private Boolean isOperational;
    private Integer activeConsumers;
    private String systemMessage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdated;

    public static QueueStatusDto fromQueueStats(QueueService.QueueStats stats) {
        return fromQueueStats(stats, null);
    }

    public static QueueStatusDto fromQueueStats(QueueService.QueueStats stats, QueueMetrics metrics) {
        if (stats == null) {
            return createEmptyStatus();
        }

        long totalProcessed = stats.completedCount() + stats.failedCount();
        long activeCount = stats.pendingCount() + stats.processingCount() + stats.retryCount();

        double successRate = calculateSuccessRate(stats);
        double failureRate = calculateFailureRate(stats);
        double retryRate = calculateRetryRate(stats);

        QueueHealth health = determineHealth(stats);
        boolean hasBacklog = stats.pendingCount() > 10; // Threshold configurable
        boolean needsAttention = stats.failedCount() > 0 || stats.deadLetterCount() > 0 || hasBacklog;

        return QueueStatusDto.builder()
                // Queue counts
                .pendingCount(stats.pendingCount())
                .processingCount(stats.processingCount())
                .completedCount(stats.completedCount())
                .failedCount(stats.failedCount())
                .retryCount(stats.retryCount())
                .deadLetterCount(stats.deadLetterCount())
                .cancelledCount(0L) // Not available in basic stats
                // Calculated metrics
                .totalProcessed(totalProcessed)
                .activeCount(activeCount)
                .successRate(successRate)
                .failureRate(failureRate)
                .retryRate(retryRate)
                // Performance metrics (from optional metrics)
                .averageProcessingTimeSeconds(metrics != null ? metrics.averageProcessingTime() : null)
                .throughputPerMinute(metrics != null ? metrics.throughputPerMinute() : null)
                .estimatedWaitTimeMinutes(calculateEstimatedWaitTime(stats))
                // Health indicators
                .health(health)
                .healthDescription(getHealthDescription(health))
                .hasBacklog(hasBacklog)
                .needsAttention(needsAttention)
                // System status
                .isOperational(health != QueueHealth.CRITICAL)
                .activeConsumers(metrics != null ? metrics.activeConsumers() : null)
                .systemMessage(buildSystemMessage(stats, health))
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private static QueueStatusDto createEmptyStatus() {
        return QueueStatusDto.builder()
                .pendingCount(0L)
                .processingCount(0L)
                .completedCount(0L)
                .failedCount(0L)
                .retryCount(0L)
                .deadLetterCount(0L)
                .cancelledCount(0L)
                .totalProcessed(0L)
                .activeCount(0L)
                .successRate(0.0)
                .failureRate(0.0)
                .retryRate(0.0)
                .estimatedWaitTimeMinutes(0)
                .health(QueueHealth.UNKNOWN)
                .healthDescription("Sem dados disponíveis")
                .hasBacklog(false)
                .needsAttention(false)
                .isOperational(true)
                .systemMessage("Sistema inicializando...")
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private static double calculateSuccessRate(QueueService.QueueStats stats) {
        long total = stats.completedCount() + stats.failedCount();
        return total > 0 ? Math.round((double) stats.completedCount() / total * 100 * 100.0) / 100.0 : 0.0;
    }

    private static double calculateFailureRate(QueueService.QueueStats stats) {
        long total = stats.completedCount() + stats.failedCount();
        return total > 0 ? Math.round((double) stats.failedCount() / total * 100 * 100.0) / 100.0 : 0.0;
    }

    private static double calculateRetryRate(QueueService.QueueStats stats) {
        long total = stats.pendingCount() + stats.processingCount() + stats.completedCount() +
                stats.failedCount() + stats.retryCount();
        return total > 0 ? Math.round((double) stats.retryCount() / total * 100 * 100.0) / 100.0 : 0.0;
    }

    private static QueueHealth determineHealth(QueueService.QueueStats stats) {
        // Critical: many failures or dead letters
        if (stats.failedCount() > 10 || stats.deadLetterCount() > 5) {
            return QueueHealth.CRITICAL;
        }

        // Warning: high pending count or some failures
        if (stats.pendingCount() > 50 || stats.failedCount() > 0 || stats.retryCount() > 10) {
            return QueueHealth.WARNING;
        }

        // Healthy: normal operation
        if (stats.processingCount() > 0 || stats.completedCount() > 0) {
            return QueueHealth.HEALTHY;
        }

        // Idle: no activity
        return QueueHealth.IDLE;
    }

    private static String getHealthDescription(QueueHealth health) {
        return switch (health) {
            case HEALTHY -> "Fila operando normalmente";
            case WARNING -> "Fila com alguns problemas, monitoramento necessário";
            case CRITICAL -> "Fila com problemas críticos, intervenção necessária";
            case IDLE -> "Fila sem atividade";
            case UNKNOWN -> "Status da fila desconhecido";
        };
    }

    private static Integer calculateEstimatedWaitTime(QueueService.QueueStats stats) {
        if (stats.pendingCount() == 0) {
            return 0;
        }

        // Estimativa simples: 1 minuto por item na fila (ajustável)
        long waitTime = stats.pendingCount();
        return (int) Math.min(waitTime, 60); // Max 60 minutes
    }

    private static String buildSystemMessage(QueueService.QueueStats stats, QueueHealth health) {
        if (health == QueueHealth.CRITICAL) {
            return "Sistema com problemas críticos - verifique logs";
        }

        if (health == QueueHealth.WARNING && stats.pendingCount() > 20) {
            return "Alto volume de triagens pendentes";
        }

        if (health == QueueHealth.IDLE) {
            return "Sistema aguardando novas triagens";
        }

        if (stats.processingCount() > 0) {
            return String.format("Processando %d triagem(s)", stats.processingCount());
        }

        return "Sistema operando normalmente";
    }

    // Enums
    public enum QueueHealth {
        HEALTHY, WARNING, CRITICAL, IDLE, UNKNOWN
    }

    // Record para métricas opcionais
    public record QueueMetrics(
            Double averageProcessingTime,
            Double throughputPerMinute,
            Integer activeConsumers,
            LocalDateTime lastProcessed
    ) {}

//    // Métodos utilitários
//    public boolean isHealthy() {
//        return health == QueueHealth.HEALTHY;
//    }
//
//    public boolean hasErrors() {
//        return failedCount > 0 || deadLetterCount > 0;
//    }
//
//    public double getOverallEfficiency() {
//        if (totalProcessed == 0) return 0.0;
//        return Math.round((successRate - retryRate) * 100.0) / 100.0;
//    }
//
//    public String getStatusSummary() {
//        return String.format("%d pendentes, %d processando, %.1f%% sucesso",
//                pendingCount, processingCount, successRate);
//    }
}