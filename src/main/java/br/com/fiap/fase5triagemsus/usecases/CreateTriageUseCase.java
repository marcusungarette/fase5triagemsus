package br.com.fiap.fase5triagemsus.usecases;


import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.repositories.PatientRepository;
import br.com.fiap.fase5triagemsus.domain.repositories.TriageRepository;
import br.com.fiap.fase5triagemsus.domain.services.AITriageService;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import br.com.fiap.fase5triagemsus.domain.valueobjects.Symptom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTriageUseCase {

    private final TriageRepository triageRepository;
    private final PatientRepository patientRepository;
    private final AITriageService aiTriageService;

    @Transactional
    public Triage execute(CreateTriageCommand command) {
        //log.info("Iniciando criação de triagem para paciente ID: {}", command.patientId());

        PatientId patientId = PatientId.of(command.patientId());
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Paciente não encontrado: " + command.patientId()));

        List<Symptom> symptoms = command.symptoms().stream()
                .map(symptomDto -> Symptom.of(
                        symptomDto.description(),
                        symptomDto.intensity(),
                        symptomDto.location()
                ))
                .toList();

        Triage triage = Triage.create(patientId, symptoms);

        Triage savedTriage = triageRepository.save(triage);
        //log.debug("Triagem criada com ID: {}", savedTriage.getId().getValue());

        try {
            //log.debug("Iniciando análise de IA para triagem: {}", savedTriage.getId().getValue());
            AITriageService.TriageAnalysisResult analysisResult = aiTriageService.analyzeTriageSituation(savedTriage, patient);

            savedTriage.processWithAI(analysisResult.priority(), analysisResult.recommendation());

            savedTriage = triageRepository.save(savedTriage);

            //log.info("Triagem processada com sucesso. ID: {}, Prioridade: {}",
                    //savedTriage.getId().getValue(), savedTriage.getPriority());

        } catch (Exception e) {
            //log.error("Erro ao processar triagem com IA: {}", e.getMessage(), e);
            throw new TriageProcessingException("Erro ao processar triagem: " + e.getMessage(), e);
        }

        return savedTriage;
    }


    public record CreateTriageCommand(
            String patientId,
            List<SymptomDto> symptoms
    ) {
        public CreateTriageCommand {
            if (patientId == null || patientId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID do paciente é obrigatório");
            }
            if (symptoms == null || symptoms.isEmpty()) {
                throw new IllegalArgumentException("Lista de sintomas não pode estar vazia");
            }
            if (symptoms.size() > 10) {
                throw new IllegalArgumentException("Número máximo de sintomas é 10");
            }
        }
    }


    public record SymptomDto(
            String description,
            Integer intensity,
            String location
    ) {
        public SymptomDto {
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Descrição do sintoma é obrigatória");
            }
            if (intensity == null || intensity < 1 || intensity > 10) {
                throw new IllegalArgumentException("Intensidade deve estar entre 1 e 10");
            }
        }
    }


    public static class PatientNotFoundException extends RuntimeException {
        public PatientNotFoundException(String message) {
            super(message);
        }
    }

    public static class TriageProcessingException extends RuntimeException {
        public TriageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}