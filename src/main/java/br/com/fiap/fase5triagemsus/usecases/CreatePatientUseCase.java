package br.com.fiap.fase5triagemsus.usecases;


import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.domain.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePatientUseCase {

    private final PatientRepository patientRepository;


    @Transactional
    public Patient execute(CreatePatientCommand command) {
        //log.info("Iniciando criação de paciente com CPF: {}", maskCpf(command.cpf()));

        if (patientRepository.existsByCpf(command.cpf())) {
            throw new PatientAlreadyExistsException("Paciente com CPF " + maskCpf(command.cpf()) + " já existe");
        }

        Patient patient = Patient.create(
                command.name(),
                command.cpf(),
                command.birthDate(),
                command.gender(),
                command.phone(),
                command.email()
        );

        Patient savedPatient = patientRepository.save(patient);

        //log.info("Paciente criado com sucesso. ID: {}", savedPatient.getId().getValue());

        return savedPatient;
    }


    public record CreatePatientCommand(
            String name,
            String cpf,
            LocalDate birthDate,
            String gender,
            String phone,
            String email
    ) {
        public CreatePatientCommand {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome é obrigatório");
            }
            if (cpf == null || cpf.trim().isEmpty()) {
                throw new IllegalArgumentException("CPF é obrigatório");
            }
            if (birthDate == null) {
                throw new IllegalArgumentException("Data de nascimento é obrigatória");
            }
            if (gender == null || gender.trim().isEmpty()) {
                throw new IllegalArgumentException("Gênero é obrigatório");
            }
        }
    }


    public static class PatientAlreadyExistsException extends RuntimeException {
        public PatientAlreadyExistsException(String message) {
            super(message);
        }
    }


    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 4) {
            return "***";
        }
        String cleanCpf = cpf.replaceAll("[^0-9]", "");
        if (cleanCpf.length() != 11) {
            return "***";
        }
        return cleanCpf.substring(0, 3) + ".***.***-" + cleanCpf.substring(9);
    }
}