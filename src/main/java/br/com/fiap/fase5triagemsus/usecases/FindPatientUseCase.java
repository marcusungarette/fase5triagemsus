package br.com.fiap.fase5triagemsus.usecases;

import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.domain.repositories.PatientRepository;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class FindPatientUseCase {

    private final PatientRepository patientRepository;


    @Transactional(readOnly = true)
    public Patient findById(String patientId) {
        //log.debug("Buscando paciente por ID: {}", patientId);

        PatientId id = PatientId.of(patientId);

        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Paciente não encontrado com ID: " + patientId));
    }


    @Transactional(readOnly = true)
    public Patient findByCpf(String cpf) {
        //log.debug("Buscando paciente por CPF: {}", maskCpf(cpf));

        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }

        return patientRepository.findByCpf(cpf)
                .orElseThrow(() -> new PatientNotFoundException("Paciente não encontrado com CPF: " + maskCpf(cpf)));
    }


    @Transactional(readOnly = true)
    public boolean existsByCpf(String cpf) {
        //log.debug("Verificando existência de paciente com CPF: {}", maskCpf(cpf));

        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }

        return patientRepository.existsByCpf(cpf);
    }

    public static class PatientNotFoundException extends RuntimeException {
        public PatientNotFoundException(String message) {
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