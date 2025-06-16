package br.com.fiap.fase5triagemsus.presentation.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequestDto {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF deve estar no formato 000.000.000-00")
    private String cpf;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotBlank(message = "Gênero é obrigatório")
    @Pattern(regexp = "^(M|F|MASCULINO|FEMININO|OUTRO)$",
            message = "Gênero deve ser: M, F, MASCULINO, FEMININO ou OUTRO")
    private String gender;

    @Pattern(regexp = "\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}",
            message = "Telefone deve estar no formato (00) 00000-0000")
    private String phone;

    @Email(message = "Email deve ter formato válido")
    @Size(max = 100, message = "Email não pode exceder 100 caracteres")
    private String email;


    public String getCleanCpf() {
        return cpf != null ? cpf.replaceAll("[^0-9]", "") : null;
    }
    public String getCleanPhone() {
        return phone != null ? phone.replaceAll("[^0-9]", "") : null;
    }
    public String getNormalizedGender() {
        return gender != null ? gender.trim().toUpperCase() : null;
    }
}