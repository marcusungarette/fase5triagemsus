package br.com.fiap.fase5triagemsus.infrastructure.services.background;

import br.com.fiap.fase5triagemsus.domain.valueobjects.QueueMessage;
import br.com.fiap.fase5triagemsus.infrastructure.config.QueueConfig;
import br.com.fiap.fase5triagemsus.infrastructure.config.properties.QueueProperties;
import br.com.fiap.fase5triagemsus.infrastructure.services.queue.QueueService;
import br.com.fiap.fase5triagemsus.usecases.ProcessTriageUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriageQueueConsumer implements CommandLineRunner {

    private final QueueService queueService;
    private final ProcessTriageUseCase processTriageUseCase;
    private final QueueProperties queueProperties;

    @Override
    public void run(String... args) {
        log.info("Iniciando consumers de triagem...");

        for (int i = 0; i < queueProperties.getConsumerThreads(); i++) {
            startConsumer("consumer-" + i);
        }
    }

    @Async("queueConsumerExecutor")
    public void startConsumer(String consumerName) {
        log.info("Iniciando consumer: {}", consumerName);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                processNextBatch(consumerName);
                Thread.sleep(queueProperties.getProcessingIntervalSeconds() * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Erro no consumer {}: {}", consumerName, e.getMessage(), e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("Consumer {} parado", consumerName);
    }

    private void processNextBatch(String consumerName) {
        List<QueueMessage> priorityMessages = queueService.receiveMultipleFromQueue(
                QueueConfig.TRIAGE_PRIORITY_QUEUE,
                queueProperties.getBatchSize() / 2
        );

        List<QueueMessage> regularMessages = queueService.receiveMultipleFromQueue(
                QueueConfig.TRIAGE_QUEUE,
                queueProperties.getBatchSize() - priorityMessages.size()
        );

        List<QueueMessage> allMessages = List.of(priorityMessages, regularMessages)
                .stream()
                .flatMap(List::stream)
                .toList();

        if (!allMessages.isEmpty()) {
            log.debug("Consumer {} processando {} mensagens", consumerName, allMessages.size());

            List<CompletableFuture<Void>> futures = allMessages.stream()
                    .map(this::processMessageAsync)
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    @Async("triageProcessingExecutor")
    public CompletableFuture<Void> processMessageAsync(QueueMessage message) {
        return CompletableFuture.runAsync(() -> processMessage(message));
    }

    private void processMessage(QueueMessage message) {
        try {
            queueService.markAsProcessing(message);

            ProcessTriageUseCase.ProcessingResult result = processTriageUseCase.execute(message);

            switch (result.status()) {
                case SUCCESS -> {
                    queueService.markAsCompleted(message);
                    queueService.ackMessage(QueueConfig.TRIAGE_QUEUE, message);
                    log.debug("Triagem processada: {}", message.getTriageId());
                }
                case FAILED -> {
                    queueService.markAsFailed(message, result.message());
                    queueService.nackMessage(QueueConfig.TRIAGE_QUEUE, message);
                    log.warn("Falha no processamento: {} - {}", message.getTriageId(), result.message());
                }
                case SKIPPED -> {
                    queueService.ackMessage(QueueConfig.TRIAGE_QUEUE, message);
                    log.debug("Triagem ignorada: {} - {}", message.getTriageId(), result.message());
                }
            }

        } catch (Exception e) {
            log.error("Erro crítico no processamento: {}", e.getMessage(), e);
            queueService.markAsFailed(message, "Erro crítico: " + e.getMessage());
            queueService.nackMessage(QueueConfig.TRIAGE_QUEUE, message);
        }
    }
}