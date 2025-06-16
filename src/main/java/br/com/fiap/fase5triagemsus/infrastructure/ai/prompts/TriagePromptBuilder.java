package br.com.fiap.fase5triagemsus.infrastructure.ai.prompts;

import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.valueobjects.Symptom;
import org.springframework.stereotype.Component;


@Component
public class TriagePromptBuilder {

    private static final String SYSTEM_PROMPT = """
        Você é um sistema de triagem médica especializado seguindo o Protocolo de Manchester.
        Sua função é analisar sintomas de pacientes e classificar a urgência do atendimento.
        
        NÍVEIS DE PRIORIDADE (Protocolo de Manchester):
        1. EMERGENCY (Vermelho) - Risco de vida imediato - Atendimento imediato
        2. VERY_URGENT (Laranja) - Risco de vida potencial - Atendimento em 10 minutos
        3. URGENT (Amarelo) - Condições que podem deteriorar - Atendimento em 60 minutos
        4. LESS_URGENT (Verde) - Condições estáveis - Atendimento em 120 minutos
        5. NON_URGENT (Azul) - Condições crônicas ou menores - Atendimento em 240 minutos
        
        CRITÉRIOS DE EMERGÊNCIA:
        - Parada cardiorrespiratória
        - Inconsciência
        - Choque hipovolêmico
        - Obstrução de vias aéreas
        - Hemorragia grave
        - Dor torácica com sinais de infarto
        - AVC com menos de 4.5h
        
        CRITÉRIOS MUITO URGENTE:
        - Dor torácica intensa
        - Dispneia grave
        - Alterações neurológicas agudas
        - Dor abdominal intensa
        - Febre alta (>39°C) com sinais de sepse
        - Vômitos persistentes com desidratação
        
        CONSIDERAÇÕES ESPECIAIS:
        - Crianças (<12 anos): Priorizar se febre alta, vômitos, letargia
        - Idosos (≥65 anos): Sinais podem ser menos evidentes, considerar comorbidades
        - Gestantes: Priorizar dor abdominal, sangramento, pressão alta
        
        FORMATO DE RESPOSTA OBRIGATÓRIO (JSON):
        {
          "priority": "EMERGENCY|VERY_URGENT|URGENT|LESS_URGENT|NON_URGENT",
          "recommendation": "Descrição detalhada da recomendação médica",
          "reasoning": "Justificativa técnica da classificação baseada nos sintomas",
          "confidence": 0.95
        }
        
        INSTRUÇÕES:
        - Analise TODOS os sintomas fornecidos
        - Considere idade e gênero do paciente
        - Use terminologia médica profissional
        - Seja conservador: em dúvida, priorize
        - Confidence deve ser entre 0.0 e 1.0
        - Responda APENAS em JSON válido
        """;


    public String buildTriagePrompt(Triage triage, Patient patient) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(SYSTEM_PROMPT).append("\n\n");
        prompt.append("DADOS DO PACIENTE:\n");
        prompt.append("- Idade: ").append(patient.getAge()).append(" anos\n");
        prompt.append("- Gênero: ").append(patient.getGender()).append("\n");

        // Adicionar características especiais do paciente
        if (patient.isChild()) {
            prompt.append("- ATENÇÃO: Paciente pediátrico\n");
        }
        if (patient.isElderly()) {
            prompt.append("- ATENÇÃO: Paciente idoso\n");
        }

        prompt.append("\nSINTOMAS REPORTADOS:\n");

        int symptomIndex = 1;
        for (Symptom symptom : triage.getSymptoms()) {
            prompt.append(symptomIndex++).append(". ");
            prompt.append("Descrição: ").append(symptom.getDescription());
            prompt.append(" | Intensidade: ").append(symptom.getIntensity()).append("/10");

            if (symptom.getLocation() != null && !symptom.getLocation().trim().isEmpty()) {
                prompt.append(" | Localização: ").append(symptom.getLocation());
            }

            // Adicionar classificação do sintoma
            if (symptom.isSevere()) {
                prompt.append(" [SINTOMA GRAVE]");
            } else if (symptom.isModerate()) {
                prompt.append(" [SINTOMA MODERADO]");
            }

            prompt.append("\n");
        }

        // Adicionar contexto adicional
        prompt.append("\nCONTEXTO ADICIONAL:\n");
        prompt.append("- Total de sintomas: ").append(triage.getSymptoms().size()).append("\n");
        prompt.append("- Sintomas graves: ").append(triage.countSevereSymptoms()).append("\n");
        prompt.append("- Sintomas moderados: ").append(triage.countModerateSymptoms()).append("\n");
        prompt.append("- Horário da triagem: ").append(triage.getCreatedAt()).append("\n");

        prompt.append("\nAnalise os sintomas e forneça a classificação de triagem em formato JSON:");

        return prompt.toString();
    }

    /**
     * Constrói prompt simplificado para testes

    public String buildSimplePrompt(String symptoms, int age, String gender) {
        return String.format("""
            %s
            
            DADOS DO PACIENTE:
            - Idade: %d anos
            - Gênero: %s
            
            SINTOMAS: %s
            
            Analise e forneça a classificação em JSON:
            """, SYSTEM_PROMPT, age, gender, symptoms);
    }
     */
}