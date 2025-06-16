package br.com.fiap.fase5triagemsus.domain.entities;


import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Para JPA
public class Patient {

    private PatientId id;
    private String name;
    private String cpf;
    private LocalDate birthDate;
    private String gender;
    private String phone;
    private String email;


    private Patient(PatientId id, String name, String cpf, LocalDate birthDate,
                    String gender, String phone, String email) {
        this.id = validateId(id);
        this.name = validateName(name);
        this.cpf = validateCpf(cpf);
        this.birthDate = validateBirthDate(birthDate);
        this.gender = validateGender(gender);
        this.phone = phone; // Opcional
        this.email = email; // Opcional
    }


    public static Patient create(String name, String cpf, LocalDate birthDate,
                                 String gender, String phone, String email) {
        return new Patient(PatientId.generate(), name, cpf, birthDate, gender, phone, email);
    }

    public static Patient restore(PatientId id, String name, String cpf, LocalDate birthDate,
                                  String gender, String phone, String email) {
        return new Patient(id, name, cpf, birthDate, gender, phone, email);
    }


    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }


    public boolean isChild() {
        return getAge() < 12;
    }


    public boolean isAdolescent() {
        return getAge() >= 12 && getAge() <= 17;
    }


    public boolean isElderly() {
        return getAge() >= 65;
    }


    public void updateContactInfo(String phone, String email) {
        this.phone = phone;
        this.email = email;
    }



    private PatientId validateId(PatientId id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do paciente é obrigatório");
        }
        return id;
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do paciente é obrigatório");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Nome não pode exceder 100 caracteres");
        }
        return name.trim();
    }

    private String validateCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }

        String cleanCpf = cpf.replaceAll("[^0-9]", "");

        if (cleanCpf.length() != 11) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos");
        }

        if (cleanCpf.matches("(\\d)\\1{10}")) {
            throw new IllegalArgumentException("CPF inválido");
        }

        return cleanCpf;
    }

    private LocalDate validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória");
        }

        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode ser futura");
        }

        if (birthDate.isBefore(LocalDate.now().minusYears(120))) {
            throw new IllegalArgumentException("Data de nascimento inválida");
        }

        return birthDate;
    }

    private String validateGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Gênero é obrigatório");
        }

        String normalizedGender = gender.trim().toUpperCase();
        if (!normalizedGender.matches("^(M|F|MASCULINO|FEMININO|OUTRO)$")) {
            throw new IllegalArgumentException("Gênero deve ser: M, F, MASCULINO, FEMININO ou OUTRO");
        }

        return normalizedGender;
    }
}