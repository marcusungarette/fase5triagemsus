package br.com.fiap.fase5triagemsus.domain.services;


import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.domain.entities.Triage;


public interface AITriageService {


    TriageAnalysisResult analyzeTriageSituation(Triage triage, Patient patient);


    record TriageAnalysisResult(
            br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel priority,
            String recommendation,
            String reasoning,
            Double confidenceScore
    ) {
        public TriageAnalysisResult {
            if (priority == null) {
                throw new IllegalArgumentException("Prioridade é obrigatória");
            }
            if (recommendation == null || recommendation.trim().isEmpty()) {
                throw new IllegalArgumentException("Recomendação é obrigatória");
            }
            if (confidenceScore != null && (confidenceScore < 0.0 || confidenceScore > 1.0)) {
                throw new IllegalArgumentException("Score de confiança deve estar entre 0.0 e 1.0");
            }
        }
    }
}