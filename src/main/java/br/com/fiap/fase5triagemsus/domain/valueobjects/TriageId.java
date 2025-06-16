package br.com.fiap.fase5triagemsus.domain.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;


@Getter
@EqualsAndHashCode
public final class TriageId {

    private final String value;

    private TriageId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da triagem não pode ser nulo ou vazio");
        }
        this.value = value;
    }

    public static TriageId of(String value) {
        return new TriageId(value);
    }

    public static TriageId generate() {
        return new TriageId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}