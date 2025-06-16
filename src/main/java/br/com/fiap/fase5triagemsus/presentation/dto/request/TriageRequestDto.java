package br.com.fiap.fase5triagemsus.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriageRequestDto {

    @NotBlank(message = "ID do paciente é obrigatório")
    private String patientId;

    @NotNull(message = "Lista de sintomas é obrigatória")
    @Size(min = 1, max = 10, message = "Deve ter entre 1 e 10 sintomas")
    @Valid
    private List<SymptomRequestDto> symptoms;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SymptomRequestDto {

        @NotBlank(message = "Descrição do sintoma é obrigatória")
        @Size(min = 3, max = 500, message = "Descrição deve ter entre 3 e 500 caracteres")
        private String description;

        @NotNull(message = "Intensidade é obrigatória")
        @Min(value = 1, message = "Intensidade mínima é 1")
        @Max(value = 10, message = "Intensidade máxima é 10")
        private Integer intensity;

        @Size(max = 100, message = "Localização não pode exceder 100 caracteres")
        private String location;


        public String getNormalizedDescription() {
            return description != null ? description.trim() : null;
        }
        public String getNormalizedLocation() {
            return location != null && !location.trim().isEmpty() ? location.trim() : null;
        }
    }
}