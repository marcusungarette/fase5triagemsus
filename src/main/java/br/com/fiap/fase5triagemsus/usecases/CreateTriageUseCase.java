package br.com.fiap.fase5triagemsus.usecases;

import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.repositories.PatientRepository;
import br.com.fiap.fase5triagemsus.domain.repositories.TriageRepository;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import br.com.fiap.fase5triagemsus.domain.valueobjects.QueueMessage;
import br.com.fiap.fase5triagemsus.domain.valueobjects.Symptom;
import br.com.fiap.fase5triagemsus.infrastructure.config.QueueConfig;
import br.com.fiap.fase5triagemsus.infrastructure.services.queue.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTriageUseCase {

    private final TriageRepository triageRepository;
    private final PatientRepository patientRepository;
    private final QueueService queueService;

    @Transactional
    public Triage execute(CreateTriageCommand command) {
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

        QueueMessage queueMessage = QueueMessage.builder()
                .triageId(savedTriage.getId().getValue())
                .patientId(savedTriage.getPatientId().getValue())
                .symptoms(symptoms.stream().map(Symptom::getDescription).toList())
                .patientAge(patient.getAge())
                .createdAt(LocalDateTime.now())
                .priority(savedTriage.isUrgent() ? 1 : 3)
                .retryCount(0)
                .build();

        if (savedTriage.isUrgent()) {
            queueService.sendToPriorityQueue(queueMessage);
        } else {
            queueService.sendToQueue(QueueConfig.TRIAGE_QUEUE, queueMessage);
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
}