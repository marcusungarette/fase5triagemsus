package br.com.fiap.fase5triagemsus.infrastructure.ai.services;


import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
import br.com.fiap.fase5triagemsus.domain.services.AITriageService;
import br.com.fiap.fase5triagemsus.infrastructure.ai.dto.GeminiRequestDto;
import br.com.fiap.fase5triagemsus.infrastructure.ai.dto.GeminiResponseDto;
import br.com.fiap.fase5triagemsus.infrastructure.ai.prompts.TriagePromptBuilder;
import br.com.fiap.fase5triagemsus.infrastructure.config.properties.GeminiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAITriageService implements AITriageService {

    @Qualifier("geminiWebClient")
    private final WebClient webClient;
    private final GeminiProperties geminiProperties;
    private final TriagePromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Override
    public TriageAnalysisResult analyzeTriageSituation(Triage triage, Patient patient) {
        //log.info("Iniciando análise de triagem com Gemini. Triagem ID: {}", triage.getId().getValue());

        try {
            // 1. Construir prompt
            String prompt = promptBuilder.buildTriagePrompt(triage, patient);
            //log.debug("Prompt construído para triagem: {}", triage.getId().getValue());

            // 2. Preparar requisição
            GeminiRequestDto.Request request = buildGeminiRequest(prompt);

            // 3. Chamar API Gemini
            GeminiResponseDto.Response response = callGeminiAPI(request);

            // 4. Processar resposta
            TriageAnalysisResult result = processGeminiResponse(response);

            //log.info("Análise concluída. Triagem: {}, Prioridade: {}, Confiança: {}",
                    //triage.getId().getValue(), result.priority(), result.confidenceScore());

            return result;

        } catch (Exception e) {
            //log.error("Erro ao analisar triagem com Gemini: {}", e.getMessage(), e);

            // Fallback: classificação conservadora baseada em sintomas
            return createFallbackAnalysis(triage, patient, e);
        }
    }


    private GeminiRequestDto.Request buildGeminiRequest(String prompt) {
        GeminiRequestDto.Part part = GeminiRequestDto.Part.builder()
                .text(prompt)
                .build();

        GeminiRequestDto.Content content = GeminiRequestDto.Content.builder()
                .parts(List.of(part))
                .build();

        GeminiRequestDto.GenerationConfig config = GeminiRequestDto.GenerationConfig.builder()
                .temperature(geminiProperties.getTemperature())
                .maxOutputTokens(geminiProperties.getMaxTokens())
                .topP(0.8)
                .topK(40)
                .build();

        return GeminiRequestDto.Request.builder()
                .contents(List.of(content))
                .generationConfig(config)
                .build();
    }


    private GeminiResponseDto.Response callGeminiAPI(GeminiRequestDto.Request request) {
        String endpoint = String.format("/models/%s:generateContent", geminiProperties.getModel());

        return webClient
                .post()
                .uri(endpoint)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponseDto.Response.class)
                .timeout(geminiProperties.getTimeout())
                .retryWhen(Retry.backoff(geminiProperties.getMaxRetries(), Duration.ofSeconds(1))
                        .filter(this::isRetryableError))
                //.doOnSubscribe(s -> log.info("Enviando requisição para Gemini API"))
                //.doOnSuccess(r -> log.info("✅ Resposta recebida da Gemini API"))
                //.doOnError(e -> log.error("❌ Erro na chamada da Gemini API: {}", e.getMessage()))
                .block();
    }


    private TriageAnalysisResult processGeminiResponse(GeminiResponseDto.Response response) {
        if (response.candidates() == null || response.candidates().isEmpty()) {
            throw new AIAnalysisException("Nenhuma resposta válida recebida da API Gemini");
        }

        GeminiResponseDto.Candidate candidate = response.candidates().get(0);
        if (candidate.content() == null || candidate.content().parts() == null || candidate.content().parts().isEmpty()) {
            throw new AIAnalysisException("Conteúdo da resposta vazio");
        }

        String responseText = candidate.content().parts().get(0).text();
        //log.debug("Resposta da IA: {}", responseText);

        return parseTriageResponse(responseText);
    }


    private TriageAnalysisResult parseTriageResponse(String responseText) {
        try {
            // Extrair JSON da resposta (pode vir com texto adicional)
            String jsonText = extractJsonFromResponse(responseText);

            JsonNode jsonNode = objectMapper.readTree(jsonText);

            String priorityStr = jsonNode.get("priority").asText();
            String recommendation = jsonNode.get("recommendation").asText();
            String reasoning = jsonNode.get("reasoning").asText();
            double confidence = jsonNode.get("confidence").asDouble();

            PriorityLevel priority = PriorityLevel.valueOf(priorityStr);

            return new TriageAnalysisResult(priority, recommendation, reasoning, confidence);

        } catch (JsonProcessingException e) {
            //log.error("Erro ao fazer parse da resposta JSON: {}", responseText, e);
            throw new AIAnalysisException("Erro ao processar resposta da IA: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            //log.error("Prioridade inválida na resposta: {}", responseText, e);
            throw new AIAnalysisException("Prioridade inválida retornada pela IA");
        }
    }


    private String extractJsonFromResponse(String responseText) {
        int startIndex = responseText.indexOf('{');
        int endIndex = responseText.lastIndexOf('}');

        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            throw new AIAnalysisException("JSON não encontrado na resposta da IA");
        }

        return responseText.substring(startIndex, endIndex + 1);
    }


    private TriageAnalysisResult createFallbackAnalysis(Triage triage, Patient patient, Exception originalError) {
        //log.warn("Criando análise de fallback para triagem: {}", triage.getId().getValue());

        // Lógica conservadora baseada em sintomas graves
        PriorityLevel fallbackPriority = determineFallbackPriority(triage, patient);

        String recommendation = String.format(
                "ATENÇÃO: Análise realizada em modo de emergência devido a falha no sistema de IA. " +
                        "Recomenda-se avaliação médica presencial imediata. " +
                        "Paciente apresenta %d sintoma(s), sendo %d grave(s). " +
                        "Erro original: %s",
                triage.getSymptoms().size(),
                triage.countSevereSymptoms(),
                originalError.getMessage()
        );

        String reasoning = "Classificação conservadora devido a falha no sistema de IA. " +
                "Baseada em análise de sintomas graves e características do paciente.";

        return new TriageAnalysisResult(fallbackPriority, recommendation, reasoning, 0.5);
    }

    /**
     * Determina prioridade de fallback baseada em regras simples
     */
    private PriorityLevel determineFallbackPriority(Triage triage, Patient patient) {
        // Se tem sintomas graves, classificar como urgente
        if (triage.countSevereSymptoms() > 0) {
            return patient.isElderly() || patient.isChild() ? PriorityLevel.VERY_URGENT : PriorityLevel.URGENT;
        }

        // Se tem muitos sintomas moderados
        if (triage.countModerateSymptoms() >= 3) {
            return PriorityLevel.URGENT;
        }

        // Caso padrão: pouco urgente
        return PriorityLevel.LESS_URGENT;
    }

    /**
     * Verifica se o erro é passível de retry
     */
    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            // Retry em erros 5xx (server) e 429 (rate limit)
            return statusCode >= 500 || statusCode == 429;
        }

        if (throwable instanceof WebClientException) {
            // Retry em erros de timeout ou conectividade
            return true;
        }

        return false;
    }

    /**
     * Exceção específica para erros de análise de IA
     */
    public static class AIAnalysisException extends RuntimeException {
        public AIAnalysisException(String message) {
            super(message);
        }

        public AIAnalysisException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}