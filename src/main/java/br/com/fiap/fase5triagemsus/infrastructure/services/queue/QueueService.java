package br.com.fiap.fase5triagemsus.infrastructure.services.queue;

import br.com.fiap.fase5triagemsus.domain.valueobjects.QueueMessage;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface QueueService {

    void sendToQueue(String queueName, QueueMessage message);

    void sendToQueue(String queueName, QueueMessage message, Duration delay);

    void sendToPriorityQueue(QueueMessage message);

    void sendToRetryQueue(QueueMessage message, Duration delay);

    void sendToDeadLetterQueue(QueueMessage message, String reason);

    Optional<QueueMessage> receiveFromQueue(String queueName);

    Optional<QueueMessage> receiveFromQueue(String queueName, Duration timeout);

    List<QueueMessage> receiveMultipleFromQueue(String queueName, int count);

    void ackMessage(String queueName, QueueMessage message);

    void nackMessage(String queueName, QueueMessage message);

    void markAsProcessing(QueueMessage message);

    void markAsCompleted(QueueMessage message);

    void markAsFailed(QueueMessage message, String reason);

    long getQueueSize(String queueName);

    long getProcessingCount();

    QueueStats getQueueStats();

    void cleanupProcessing(Duration timeout);

    record QueueStats(
            long pendingCount,
            long processingCount,
            long completedCount,
            long failedCount,
            long retryCount,
            long deadLetterCount
    ) {}
}