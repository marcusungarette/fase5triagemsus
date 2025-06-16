package br.com.fiap.fase5triagemsus.domain.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;


@Getter
@EqualsAndHashCode
public final class PatientId {

    private final String value;

    private PatientId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do paciente não pode ser nulo ou vazio");
        }
        this.value = value;
    }


    public static PatientId of(String value) {
        return new PatientId(value);
    }
    public static PatientId generate() {
        return new PatientId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}