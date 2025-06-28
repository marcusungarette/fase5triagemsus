package br.com.fiap.fase5triagemsus.usecases;

import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.TriageStatus;
import br.com.fiap.fase5triagemsus.domain.repositories.TriageRepository;
import br.com.fiap.fase5triagemsus.domain.valueobjects.TriageId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateTriageStatusUseCase {

    private final TriageRepository triageRepository;

    @Transactional
    public Triage updateStatus(String triageId, TriageStatus newStatus) {
        TriageId id = TriageId.of(triageId);
        Triage triage = triageRepository.findById(id)
                .orElseThrow(() -> new TriageNotFoundException("Triagem não encontrada: " + triageId));

        if (!canTransitionTo(triage.getStatus(), newStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Transição inválida de %s para %s", triage.getStatus(), newStatus));
        }

        Triage updatedTriage = triage.withStatus(newStatus);
        return triageRepository.save(updatedTriage);
    }

    @Transactional
    public Triage cancelTriage(String triageId) {
        TriageId id = TriageId.of(triageId);
        Triage triage = triageRepository.findById(id)
                .orElseThrow(() -> new TriageNotFoundException("Triagem não encontrada: " + triageId));

        if (!triage.canBeCancelled()) {
            throw new TriageCannotBeCancelledException("Triagem não pode ser cancelada no status: " + triage.getStatus());
        }

        Triage cancelledTriage = triage.withCancelled();
        return triageRepository.save(cancelledTriage);
    }

    private boolean canTransitionTo(TriageStatus current, TriageStatus target) {
        if (current.isFinalStatus()) {
            return false;
        }

        return switch (current) {
            case PENDING -> target == TriageStatus.PROCESSING || target == TriageStatus.CANCELLED;
            case PROCESSING -> target == TriageStatus.COMPLETED || target == TriageStatus.RETRYING || target == TriageStatus.FAILED;
            case RETRYING -> target == TriageStatus.COMPLETED || target == TriageStatus.FAILED;
            case COMPLETED, FAILED, CANCELLED -> false;
        };
    }

    public static class TriageNotFoundException extends RuntimeException {
        public TriageNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidStatusTransitionException extends RuntimeException {
        public InvalidStatusTransitionException(String message) {
            super(message);
        }
    }

    public static class TriageCannotBeCancelledException extends RuntimeException {
        public TriageCannotBeCancelledException(String message) {
            super(message);
        }
    }
}