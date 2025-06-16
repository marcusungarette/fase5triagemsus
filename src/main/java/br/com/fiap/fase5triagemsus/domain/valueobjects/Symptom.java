package br.com.fiap.fase5triagemsus.domain.valueobjects;


import lombok.EqualsAndHashCode;
import lombok.Getter;


@Getter
@EqualsAndHashCode
public final class Symptom {

    private final String description;
    private final Integer intensity; // 1-10
    private final String location; // Opcional: localização do sintoma

    private Symptom(String description, Integer intensity, String location) {
        this.description = validateDescription(description);
        this.intensity = validateIntensity(intensity);
        this.location = location;
    }


    public static Symptom of(String description, Integer intensity) {
        return new Symptom(description, intensity, null);
    }

    public static Symptom of(String description, Integer intensity, String location) {
        return new Symptom(description, intensity, location);
    }

    public boolean isSevere() {
        return intensity >= 7;
    }

    public boolean isModerate() {
        return intensity >= 4 && intensity <= 6;
    }

    public boolean isMild() {
        return intensity >= 1 && intensity <= 3;
    }

    private String validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição do sintoma é obrigatória");
        }
        if (description.length() > 500) {
            throw new IllegalArgumentException("Descrição do sintoma não pode exceder 500 caracteres");
        }
        return description.trim();
    }

    private Integer validateIntensity(Integer intensity) {
        if (intensity == null || intensity < 1 || intensity > 10) {
            throw new IllegalArgumentException("Intensidade do sintoma deve estar entre 1 e 10");
        }
        return intensity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(description);
        sb.append(" (intensidade: ").append(intensity).append(")");

        if (location != null && !location.trim().isEmpty()) {
            sb.append(" - localização: ").append(location);
        }

        return sb.toString();
    }
}