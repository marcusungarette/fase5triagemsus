package br.com.fiap.fase5triagemsus.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;


@Data
@Validated
@ConfigurationProperties(prefix = "triage.business")
public class BusinessProperties {


    @Min(value = 1, message = "Deve permitir pelo menos 1 sintoma")
    @Max(value = 20, message = "Não deve permitir mais que 20 sintomas")
    private Integer maxSymptomsPerRequest = 10;


    @Positive(message = "TTL do cache deve ser positivo")
    private Long cacheTtl = 300L; // 5 minutos


    @Min(value = 0, message = "Idade mínima não pode ser negativa")
    private Integer minAge = 0;


    @Max(value = 120, message = "Idade máxima não pode ser superior a 120 anos")
    private Integer maxAge = 120;


    @Positive(message = "Tempo máximo de processamento deve ser positivo")
    private Integer maxProcessingTimeSeconds = 60;
}