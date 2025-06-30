package br.com.fiap.fase5triagemsus.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class QueueConfig {

    public static final String TRIAGE_QUEUE = "triage:queue";
    public static final String TRIAGE_PRIORITY_QUEUE = "triage:priority:queue";
    public static final String TRIAGE_RETRY_QUEUE = "triage:retry:queue";
    public static final String TRIAGE_DLQ = "triage:dlq";

    public static final String PROCESSING_SET = "triage:processing";
    public static final String COMPLETED_SET = "triage:completed";
    public static final String FAILED_SET = "triage:failed";

    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final int DEFAULT_RETRY_DELAY_SECONDS = 30;
    public static final int DEFAULT_PROCESSING_TIMEOUT_MINUTES = 10;

    public static class QueueNames {
        public static final String TRIAGE_MAIN = TRIAGE_QUEUE;
        public static final String TRIAGE_HIGH_PRIORITY = TRIAGE_PRIORITY_QUEUE;
        public static final String TRIAGE_RETRY = TRIAGE_RETRY_QUEUE;
        public static final String TRIAGE_DEAD_LETTER = TRIAGE_DLQ;
    }

    public static class SetNames {
        public static final String PROCESSING = PROCESSING_SET;
        public static final String COMPLETED = COMPLETED_SET;
        public static final String FAILED = FAILED_SET;
    }
}