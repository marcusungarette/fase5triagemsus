package br.com.fiap.fase5triagemsus.domain.valueobjects;

import lombok.Value;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // ← ADICIONAR ESTA ANOTAÇÃO
public class QueueMessage {

    @JsonProperty("triageId")
    String triageId;

    @JsonProperty("patientId")
    String patientId;

    @JsonProperty("symptoms")
    List<String> symptoms;

    @JsonProperty("patientAge")
    Integer patientAge;

    @JsonProperty("patientWeight")
    Double patientWeight;

    @JsonProperty("patientHeight")
    Double patientHeight;

    @JsonProperty("preExistingConditions")
    List<String> preExistingConditions;

    @JsonProperty("createdAt")
    LocalDateTime createdAt;

    @JsonProperty("priority")
    @Builder.Default
    Integer priority = 3;

    @JsonProperty("retryCount")
    @Builder.Default
    Integer retryCount = 0;

    @JsonProperty("lastRetryAt")
    LocalDateTime lastRetryAt;

    @JsonCreator
    public QueueMessage(
            @JsonProperty("triageId") String triageId,
            @JsonProperty("patientId") String patientId,
            @JsonProperty("symptoms") List<String> symptoms,
            @JsonProperty("patientAge") Integer patientAge,
            @JsonProperty("patientWeight") Double patientWeight,
            @JsonProperty("patientHeight") Double patientHeight,
            @JsonProperty("preExistingConditions") List<String> preExistingConditions,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("priority") Integer priority,
            @JsonProperty("retryCount") Integer retryCount,
            @JsonProperty("lastRetryAt") LocalDateTime lastRetryAt) {

        this.triageId = triageId;
        this.patientId = patientId;
        this.symptoms = symptoms != null ? List.copyOf(symptoms) : List.of();
        this.patientAge = patientAge;
        this.patientWeight = patientWeight;
        this.patientHeight = patientHeight;
        this.preExistingConditions = preExistingConditions != null ? List.copyOf(preExistingConditions) : List.of();
        this.createdAt = createdAt;
        this.priority = priority != null ? priority : 3;
        this.retryCount = retryCount != null ? retryCount : 0;
        this.lastRetryAt = lastRetryAt;
    }

    public QueueMessage withIncrementedRetry() {
        return QueueMessage.builder()
                .triageId(this.triageId)
                .patientId(this.patientId)
                .symptoms(this.symptoms)
                .patientAge(this.patientAge)
                .patientWeight(this.patientWeight)
                .patientHeight(this.patientHeight)
                .preExistingConditions(this.preExistingConditions)
                .createdAt(this.createdAt)
                .priority(this.priority)
                .retryCount(this.retryCount + 1)
                .lastRetryAt(LocalDateTime.now())
                .build();
    }

    public boolean canRetry(int maxRetries) {
        return this.retryCount < maxRetries;
    }

    public boolean isHighPriority() {
        return this.priority <= 2;
    }

    public long getRetryDelaySeconds() {
        return Math.min(300, (long) Math.pow(2, this.retryCount) * 10); // Max 5 minutos
    }
}