package br.com.fiap.fase5triagemsus.infrastructure.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;


public class GeminiRequestDto {

    @Builder
    public record Request(
            @JsonProperty("contents") List<Content> contents,
            @JsonProperty("generationConfig") GenerationConfig generationConfig
    ) {}

    @Builder
    public record Content(
            @JsonProperty("parts") List<Part> parts
    ) {}

    @Builder
    public record Part(
            @JsonProperty("text") String text
    ) {}

    @Builder
    public record GenerationConfig(
            @JsonProperty("temperature") Double temperature,
            @JsonProperty("maxOutputTokens") Integer maxOutputTokens,
            @JsonProperty("topP") Double topP,
            @JsonProperty("topK") Integer topK
    ) {}
}