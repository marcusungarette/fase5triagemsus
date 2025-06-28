package br.com.fiap.fase5triagemsus.infrastructure.services.background;

import br.com.fiap.fase5triagemsus.infrastructure.config.properties.QueueProperties;
import br.com.fiap.fase5triagemsus.infrastructure.services.queue.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueCleanupScheduler {

    private final QueueService queueService;
    private final QueueProperties queueProperties;

    @Scheduled(fixedDelay = 1800000) // 30 minutos em milliseconds
    public void cleanupStuckProcessing() {
        try {
            Duration timeout = Duration.ofMinutes(queueProperties.getProcessingTimeoutMinutes());
            queueService.cleanupProcessing(timeout);
            log.debug("Limpeza de processamento órfão executada");
        } catch (Exception e) {
            log.error("Erro na limpeza: {}", e.getMessage(), e);
        }
    }
}