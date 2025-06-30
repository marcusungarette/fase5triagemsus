package br.com.fiap.fase5triagemsus.usecases;

import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.TriageStatus;
import br.com.fiap.fase5triagemsus.domain.repositories.PatientRepository;
import br.com.fiap.fase5triagemsus.domain.repositories.TriageRepository;
import br.com.fiap.fase5triagemsus.domain.services.AITriageService;
import br.com.fiap.fase5triagemsus.domain.valueobjects.PatientId;
import br.com.fiap.fase5triagemsus.domain.valueobjects.QueueMessage;
import br.com.fiap.fase5triagemsus.domain.valueobjects.TriageId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessTriageUseCase {

    private final TriageRepository triageRepository;
    private final PatientRepository patientRepository;
    private final AITriageService aiTriageService;

    @Transactional
    public ProcessingResult execute(QueueMessage queueMessage) {
        TriageId triageId = TriageId.of(queueMessage.getTriageId());

        Triage triage = triageRepository.findById(triageId)
                .orElseThrow(() -> new TriageNotFoundException("Triagem não encontrada: " + triageId.getValue()));

        if (!triage.getStatus().equals(TriageStatus.PENDING)) {
            return ProcessingResult.skipped("Triagem não está pendente");
        }

        triage = triage.withStatus(TriageStatus.PROCESSING);
        triageRepository.save(triage);

        try {
            PatientId patientId = PatientId.of(queueMessage.getPatientId());
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException("Paciente não encontrado: " + patientId.getValue()));

            AITriageService.TriageAnalysisResult result = aiTriageService.analyzeTriageSituation(triage, patient);

            triage = triage.withCompletedResult(
                    result.recommendation(),
                    result.priority(),
                    result.confidenceScore(),
                    result.reasoning()
            );

            triageRepository.save(triage);
            return ProcessingResult.success(triage);

        } catch (Exception e) {
            triage = triage.withError("Erro no processamento: " + e.getMessage());
            triageRepository.save(triage);

            return ProcessingResult.failed(e.getMessage());
        }
    }

    public record ProcessingResult(
            ProcessingStatus status,
            String message,
            Triage triage
    ) {
        public static ProcessingResult success(Triage triage) {
            return new ProcessingResult(ProcessingStatus.SUCCESS, "Processado com sucesso", triage);
        }

        public static ProcessingResult failed(String message) {
            return new ProcessingResult(ProcessingStatus.FAILED, message, null);
        }

        public static ProcessingResult skipped(String message) {
            return new ProcessingResult(ProcessingStatus.SKIPPED, message, null);
        }
    }

    public enum ProcessingStatus {
        SUCCESS, FAILED, SKIPPED
    }

    public static class TriageNotFoundException extends RuntimeException {
        public TriageNotFoundException(String message) {
            super(message);
        }
    }

    public static class PatientNotFoundException extends RuntimeException {
        public PatientNotFoundException(String message) {
            super(message);
        }
    }
}