package br.com.fiap.fase5triagemsus.infrastructure.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;


public class GeminiResponseDto {

    @Builder
    public record Response(
            @JsonProperty("candidates") List<Candidate> candidates,
            @JsonProperty("usageMetadata") UsageMetadata usageMetadata
    ) {}

    @Builder
    public record Candidate(
            @JsonProperty("content") Content content,
            @JsonProperty("finishReason") String finishReason,
            @JsonProperty("index") Integer index,
            @JsonProperty("safetyRatings") List<SafetyRating> safetyRatings
    ) {}

    @Builder
    public record Content(
            @JsonProperty("parts") List<Part> parts,
            @JsonProperty("role") String role
    ) {}

    @Builder
    public record Part(
            @JsonProperty("text") String text
    ) {}

    @Builder
    public record SafetyRating(
            @JsonProperty("category") String category,
            @JsonProperty("probability") String probability
    ) {}

    @Builder
    public record UsageMetadata(
            @JsonProperty("promptTokenCount") Integer promptTokenCount,
            @JsonProperty("candidatesTokenCount") Integer candidatesTokenCount,
            @JsonProperty("totalTokenCount") Integer totalTokenCount
    ) {}
}