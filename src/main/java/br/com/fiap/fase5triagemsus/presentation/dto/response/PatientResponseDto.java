package br.com.fiap.fase5triagemsus.presentation.dto.response;


import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponseDto {

    private String id;
    private String name;
    private String cpfMasked;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String gender;
    private String phone;
    private String email;
    private Integer age;
    private String ageGroup; // "CRIANÇA", "ADOLESCENTE", "ADULTO", "IDOSO"

    public static PatientResponseDto fromDomain(Patient patient) {
        return PatientResponseDto.builder()
                .id(patient.getId().getValue())
                .name(patient.getName())
                .cpfMasked(maskCpf(patient.getCpf()))
                .birthDate(patient.getBirthDate())
                .gender(patient.getGender())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .age(patient.getAge())
                .ageGroup(determineAgeGroup(patient))
                .build();
    }

    private static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return "***.***.***-**";
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }

    private static String determineAgeGroup(Patient patient) {
        if (patient.isChild()) {
            return "CRIANÇA";
        } else if (patient.isAdolescent()) {
            return "ADOLESCENTE";
        } else if (patient.isElderly()) {
            return "IDOSO";
        } else {
            return "ADULTO";
        }
    }
}