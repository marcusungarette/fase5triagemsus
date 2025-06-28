package br.com.fiap.fase5triagemsus.infrastructure.services.queue;

import br.com.fiap.fase5triagemsus.domain.valueobjects.QueueMessage;
import br.com.fiap.fase5triagemsus.infrastructure.config.QueueConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisQueueService implements QueueService {

    private final RedisTemplate<String, QueueMessage> queueRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void sendToQueue(String queueName, QueueMessage message) {
        try {
            queueRedisTemplate.opsForList().leftPush(queueName, message);
            log.debug("Mensagem enviada para fila {}: {}", queueName, message.getTriageId());
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem para fila {}: {}", queueName, e.getMessage(), e);
            throw new QueueException("Erro ao enviar mensagem para fila", e);
        }
    }

    @Override
    public void sendToQueue(String queueName, QueueMessage message, Duration delay) {
        try {
            String delayedKey = queueName + ":delayed:" + System.currentTimeMillis();
            queueRedisTemplate.opsForValue().set(delayedKey, message, delay);

            String script = """
                local delayedKey = KEYS[1]
                local targetQueue = KEYS[2]
                local message = redis.call('GET', delayedKey)
                if message then
                    redis.call('DEL', delayedKey)
                    redis.call('LPUSH', targetQueue, message)
                    return 1
                end
                return 0
                """;

            redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                    List.of(delayedKey, queueName));

            log.debug("Mensagem agendada para fila {} com delay {}: {}", queueName, delay, message.getTriageId());
        } catch (Exception e) {
            log.error("Erro ao agendar mensagem: {}", e.getMessage(), e);
            throw new QueueException("Erro ao agendar mensagem", e);
        }
    }

    @Override
    public void sendToPriorityQueue(QueueMessage message) {
        double score = calculatePriorityScore(message);
        try {
            queueRedisTemplate.opsForZSet().add(QueueConfig.TRIAGE_PRIORITY_QUEUE, message, score);
            log.debug("Mensagem enviada para fila prioritária com score {}: {}", score, message.getTriageId());
        } catch (Exception e) {
            log.error("Erro ao enviar para fila prioritária: {}", e.getMessage(), e);
            throw new QueueException("Erro ao enviar para fila prioritária", e);
        }
    }

    @Override
    public void sendToRetryQueue(QueueMessage message, Duration delay) {
        QueueMessage retryMessage = message.withIncrementedRetry();
        sendToQueue(QueueConfig.TRIAGE_RETRY_QUEUE, retryMessage, delay);
    }

    @Override
    public void sendToDeadLetterQueue(QueueMessage message, String reason) {
        try {
            String dlqEntry = String.format("%s:%s:%s",
                    message.getTriageId(), LocalDateTime.now(), reason);
            queueRedisTemplate.opsForList().leftPush(QueueConfig.TRIAGE_DLQ, message);
            redisTemplate.opsForSet().add(QueueConfig.TRIAGE_DLQ + ":reasons", dlqEntry);

            log.warn("Mensagem enviada para DLQ: {} - Motivo: {}", message.getTriageId(), reason);
        } catch (Exception e) {
            log.error("Erro ao enviar para DLQ: {}", e.getMessage(), e);
            throw new QueueException("Erro ao enviar para DLQ", e);
        }
    }

    @Override
    public Optional<QueueMessage> receiveFromQueue(String queueName) {
        try {
            QueueMessage message = queueRedisTemplate.opsForList().rightPop(queueName);
            if (message != null) {
                log.debug("Mensagem recebida da fila {}: {}", queueName, message.getTriageId());
            }
            return Optional.ofNullable(message);
        } catch (Exception e) {
            log.error("Erro ao receber mensagem da fila {}: {}", queueName, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<QueueMessage> receiveFromQueue(String queueName, Duration timeout) {
        try {
            QueueMessage message = queueRedisTemplate.opsForList()
                    .rightPop(queueName, timeout.getSeconds(), TimeUnit.SECONDS);
            if (message != null) {
                log.debug("Mensagem recebida da fila {} com timeout: {}", queueName, message.getTriageId());
            }
            return Optional.ofNullable(message);
        } catch (Exception e) {
            log.error("Erro ao receber mensagem com timeout da fila {}: {}", queueName, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<QueueMessage> receiveMultipleFromQueue(String queueName, int count) {
        try {
            List<QueueMessage> messages = queueRedisTemplate.opsForList().rightPop(queueName, count);
            log.debug("Recebidas {} mensagens da fila {}", messages != null ? messages.size() : 0, queueName);
            return messages != null ? messages : List.of();
        } catch (Exception e) {
            log.error("Erro ao receber múltiplas mensagens: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public void ackMessage(String queueName, QueueMessage message) {
        try {
            removeFromProcessing(message);
            log.debug("Mensagem confirmada: {}", message.getTriageId());
        } catch (Exception e) {
            log.error("Erro ao confirmar mensagem: {}", e.getMessage(), e);
        }
    }

    @Override
    public void nackMessage(String queueName, QueueMessage message) {
        try {
            removeFromProcessing(message);
            if (message.canRetry(QueueConfig.DEFAULT_MAX_RETRIES)) {
                Duration delay = Duration.ofSeconds(message.getRetryDelaySeconds());
                sendToRetryQueue(message, delay);
            } else {
                sendToDeadLetterQueue(message, "Max retries exceeded");
            }
            log.debug("Mensagem rejeitada: {}", message.getTriageId());
        } catch (Exception e) {
            log.error("Erro ao rejeitar mensagem: {}", e.getMessage(), e);
        }
    }

    @Override
    public void markAsProcessing(QueueMessage message) {
        try {
            String processingEntry = String.format("%s:%d", message.getTriageId(), System.currentTimeMillis());
            redisTemplate.opsForSet().add(QueueConfig.PROCESSING_SET, processingEntry);
            log.debug("Mensagem marcada como processando: {}", message.getTriageId());
        } catch (Exception e) {
            log.error("Erro ao marcar como processando: {}", e.getMessage(), e);
        }
    }

    @Override
    public void markAsCompleted(QueueMessage message) {
        try {
            removeFromProcessing(message);
            redisTemplate.opsForSet().add(QueueConfig.COMPLETED_SET, message.getTriageId());
            log.debug("Mensagem marcada como concluída: {}", message.getTriageId());
        } catch (Exception e) {
            log.error("Erro ao marcar como concluída: {}", e.getMessage(), e);
        }
    }

    @Override
    public void markAsFailed(QueueMessage message, String reason) {
        try {
            removeFromProcessing(message);
            String failedEntry = String.format("%s:%s:%s",
                    message.getTriageId(), LocalDateTime.now(), reason);
            redisTemplate.opsForSet().add(QueueConfig.FAILED_SET, failedEntry);
            log.debug("Mensagem marcada como falhada: {} - {}", message.getTriageId(), reason);
        } catch (Exception e) {
            log.error("Erro ao marcar como falhada: {}", e.getMessage(), e);
        }
    }

    @Override
    public long getQueueSize(String queueName) {
        try {
            Long size = queueRedisTemplate.opsForList().size(queueName);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("Erro ao obter tamanho da fila {}: {}", queueName, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public long getProcessingCount() {
        try {
            Long count = redisTemplate.opsForSet().size(QueueConfig.PROCESSING_SET);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Erro ao obter contagem de processamento: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public QueueStats getQueueStats() {
        try {
            long pending = getQueueSize(QueueConfig.TRIAGE_QUEUE);
            long processing = getProcessingCount();
            long completed = redisTemplate.opsForSet().size(QueueConfig.COMPLETED_SET);
            long failed = redisTemplate.opsForSet().size(QueueConfig.FAILED_SET);
            long retry = getQueueSize(QueueConfig.TRIAGE_RETRY_QUEUE);
            long deadLetter = getQueueSize(QueueConfig.TRIAGE_DLQ);

            return new QueueStats(pending, processing, completed, failed, retry, deadLetter);
        } catch (Exception e) {
            log.error("Erro ao obter estatísticas: {}", e.getMessage(), e);
            return new QueueStats(0, 0, 0, 0, 0, 0);
        }
    }

    @Override
    public void cleanupProcessing(Duration timeout) {
        try {
            long cutoffTime = System.currentTimeMillis() - timeout.toMillis();

            String script = """
                local processingSet = KEYS[1]
                local cutoffTime = ARGV[1]
                local members = redis.call('SMEMBERS', processingSet)
                local removedCount = 0
                
                for i=1,#members do
                    local member = members[i]
                    local parts = {}
                    for part in string.gmatch(member, "[^:]+") do
                        table.insert(parts, part)
                    end
                    
                    if #parts >= 2 then
                        local timestamp = tonumber(parts[2])
                        if timestamp and timestamp < tonumber(cutoffTime) then
                            redis.call('SREM', processingSet, member)
                            removedCount = removedCount + 1
                        end
                    end
                end
                
                return removedCount
                """;

            Long removed = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                    List.of(QueueConfig.PROCESSING_SET), String.valueOf(cutoffTime));

            if (removed != null && removed > 0) {
                log.info("Limpeza de processamento: {} itens removidos", removed);
            }
        } catch (Exception e) {
            log.error("Erro na limpeza de processamento: {}", e.getMessage(), e);
        }
    }

    private double calculatePriorityScore(QueueMessage message) {
        double baseScore = System.currentTimeMillis();

        if (message.isHighPriority()) {
            baseScore -= 1_000_000;
        }

        baseScore -= (message.getRetryCount() * 10_000);

        return baseScore;
    }

    private void removeFromProcessing(QueueMessage message) {
        try {
            String pattern = message.getTriageId() + ":*";
            String script = """
                local processingSet = KEYS[1]
                local pattern = ARGV[1]
                local members = redis.call('SMEMBERS', processingSet)
                local removedCount = 0
                
                for i=1,#members do
                    local member = members[i]
                    if string.match(member, pattern) then
                        redis.call('SREM', processingSet, member)
                        removedCount = removedCount + 1
                    end
                end
                
                return removedCount
                """;

            redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                    List.of(QueueConfig.PROCESSING_SET), message.getTriageId() + ":.*");
        } catch (Exception e) {
            log.error("Erro ao remover do processamento: {}", e.getMessage(), e);
        }
    }

    public static class QueueException extends RuntimeException {
        public QueueException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}