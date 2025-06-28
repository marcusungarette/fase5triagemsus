package br.com.fiap.fase5triagemsus.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TriageStatus {

    PENDING("Aguardando processamento", "A triagem foi criada e está na fila para processamento"),
    PROCESSING("Processando", "A triagem está sendo analisada pela IA"),
    COMPLETED("Concluída", "A triagem foi processada com sucesso e o resultado está disponível"),
    FAILED("Falhou", "A triagem não pôde ser processada após várias tentativas"),
    CANCELLED("Cancelada", "A triagem foi cancelada antes da conclusão"),
    RETRYING("Tentando novamente", "A triagem está sendo reprocessada após uma falha temporária");

    private final String displayName;
    private final String description;

    public boolean isFinalStatus() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    public boolean isActiveStatus() {
        return this == PROCESSING || this == RETRYING;
    }

    public boolean isCancellable() {
        return this == PENDING || this == RETRYING;
    }

    public boolean isErrorStatus() {
        return this == FAILED;
    }

    public boolean isSuccessStatus() {
        return this == COMPLETED;
    }

    public static TriageStatus getNextStatus(TriageStatus currentStatus, boolean isSuccess) {
        return switch (currentStatus) {
            case PENDING -> PROCESSING;
            case PROCESSING -> isSuccess ? COMPLETED : RETRYING;
            case RETRYING -> isSuccess ? COMPLETED : FAILED;
            case COMPLETED, FAILED, CANCELLED -> currentStatus; // Estados finais não mudam
        };
    }

    public static TriageStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return PENDING;
        }

        try {
            return TriageStatus.valueOf(status.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}