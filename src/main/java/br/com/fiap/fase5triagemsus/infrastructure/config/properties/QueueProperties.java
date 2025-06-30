package br.com.fiap.fase5triagemsus.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@Data
@Validated
@ConfigurationProperties(prefix = "triage.queue")
public class QueueProperties {

    @Min(value = 1, message = "Número máximo de tentativas deve ser pelo menos 1")
    private Integer maxRetries = 3;

    @Positive(message = "Delay de retry deve ser positivo")
    private Integer retryDelaySeconds = 30;

    @Positive(message = "Timeout de processamento deve ser positivo")
    private Integer processingTimeoutMinutes = 10;

    @Positive(message = "Intervalo de limpeza deve ser positivo")
    private Integer cleanupIntervalMinutes = 30;

    @Min(value = 1, message = "Tamanho do batch deve ser pelo menos 1")
    private Integer batchSize = 10;

    @Min(value = 1, message = "Número de threads consumidoras deve ser pelo menos 1")
    private Integer consumerThreads = 3;

    @Positive(message = "Timeout de polling deve ser positivo")
    private Integer pollTimeoutSeconds = 5;

    @Positive(message = "Intervalo entre processamentos deve ser positivo")
    private Integer processingIntervalSeconds = 1;

    private Boolean enablePriorityQueue = true;
    private Boolean enableRetryQueue = true;
    private Boolean enableDeadLetterQueue = true;
    private Boolean enableMetrics = true;
}