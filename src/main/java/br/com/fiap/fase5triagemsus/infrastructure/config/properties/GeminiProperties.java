package br.com.fiap.fase5triagemsus.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;


@Data
@Validated
@ConfigurationProperties(prefix = "triage.ai.gemini")
public class GeminiProperties {


    @NotBlank(message = "API Key do Gemini é obrigatória")
    private String apiKey;

    @NotBlank(message = "URL base da API Gemini é obrigatória")
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";

    @NotBlank(message = "Modelo do Gemini é obrigatório")
    private String model = "gemini-1.5-pro";

    @NotNull(message = "Timeout é obrigatório")
    private Duration timeout = Duration.ofSeconds(30);

    @Positive(message = "Número de tentativas deve ser positivo")
    private Integer maxRetries = 3;

    private Double temperature = 0.2;

    @Positive(message = "Máximo de tokens deve ser positivo")
    private Integer maxTokens = 1000;
}