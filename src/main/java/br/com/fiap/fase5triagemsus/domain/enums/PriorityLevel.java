package br.com.fiap.fase5triagemsus.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum PriorityLevel {


    EMERGENCY(1, "Emergência", "Risco de vida imediato", "Vermelho", 0),
    VERY_URGENT(2, "Muito Urgente", "Risco de vida potencial", "Laranja", 10),
    URGENT(3, "Urgente", "Condições que podem deteriorar", "Amarelo", 60),
    LESS_URGENT(4, "Pouco Urgente", "Condições estáveis", "Verde", 120),
    NON_URGENT(5, "Não Urgente", "Condições crônicas ou menores", "Azul", 240);

    private final Integer level;
    private final String name;
    private final String description;
    private final String color;
    private final Integer maxWaitTimeMinutes;
    public boolean isCritical() {
        return this == EMERGENCY || this == VERY_URGENT;
    }
    public boolean requiresFastAttention() {
        return maxWaitTimeMinutes <= 60;
    }
}