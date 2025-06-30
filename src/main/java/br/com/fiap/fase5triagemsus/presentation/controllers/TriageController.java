package br.com.fiap.fase5triagemsus.presentation.controllers;

import br.com.fiap.fase5triagemsus.domain.entities.Triage;
import br.com.fiap.fase5triagemsus.domain.enums.PriorityLevel;
import br.com.fiap.fase5triagemsus.domain.repositories.TriageRepository;
import br.com.fiap.fase5triagemsus.infrastructure.services.queue.QueueService;
import br.com.fiap.fase5triagemsus.presentation.dto.request.TriageRequestDto;
import br.com.fiap.fase5triagemsus.presentation.dto.response.ApiResponseDto;
import br.com.fiap.fase5triagemsus.presentation.dto.response.QueueStatusDto;
import br.com.fiap.fase5triagemsus.presentation.dto.response.TriageResponseDto;
import br.com.fiap.fase5triagemsus.presentation.dto.response.TriageStatusResponseDto;
import br.com.fiap.fase5triagemsus.usecases.CreateTriageUseCase;
import br.com.fiap.fase5triagemsus.usecases.FindTriageUseCase;
import br.com.fiap.fase5triagemsus.usecases.UpdateTriageStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/triages")
@RequiredArgsConstructor
@Tag(name = "Triagens", description = "Operações relacionadas às triagens médicas")
public class TriageController {

    private final CreateTriageUseCase createTriageUseCase;
    private final FindTriageUseCase findTriageUseCase;
    private final TriageRepository triageRepository;
    private final QueueService queueService;

    @PostMapping
    @Operation(summary = "Criar triagem", description = "Cria uma nova triagem para processamento assíncrono")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Triagem criada e enviada para processamento"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public ResponseEntity<ApiResponseDto<TriageResponseDto>> createTriage(
            @Valid @RequestBody TriageRequestDto request,
            HttpServletRequest httpRequest) {

        try {
            List<CreateTriageUseCase.SymptomDto> symptomDtos = request.getSymptoms().stream()
                    .map(s -> new CreateTriageUseCase.SymptomDto(
                            s.getNormalizedDescription(),
                            s.getIntensity(),
                            s.getNormalizedLocation()
                    ))
                    .toList();

            CreateTriageUseCase.CreateTriageCommand command = new CreateTriageUseCase.CreateTriageCommand(
                    request.getPatientId(),
                    symptomDtos
            );

            Triage createdTriage = createTriageUseCase.execute(command);
            TriageResponseDto responseDto = TriageResponseDto.fromDomain(createdTriage);

            ApiResponseDto<TriageResponseDto> response = ApiResponseDto.created(
                    responseDto,
                    "Triagem criada e enviada para processamento"
            );

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (CreateTriageUseCase.PatientNotFoundException e) {
            ApiResponseDto<TriageResponseDto> response = ApiResponseDto.error(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND,
                    httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (IllegalArgumentException e) {
            ApiResponseDto<TriageResponseDto> response = ApiResponseDto.error(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    httpRequest.getRequestURI()
            );
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            ApiResponseDto<TriageResponseDto> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Status da triagem", description = "Consulta o status de processamento de uma triagem")
    public ResponseEntity<ApiResponseDto<TriageStatusResponseDto>> getTriageStatus(
            @Parameter(description = "ID da triagem") @PathVariable String id) {

        try {
            Triage triage = findTriageUseCase.findById(id);
            TriageStatusResponseDto responseDto = TriageStatusResponseDto.fromDomain(triage);

            ApiResponseDto<TriageStatusResponseDto> response = ApiResponseDto.success(
                    responseDto,
                    "Status da triagem"
            );

            return ResponseEntity.ok(response);

        } catch (FindTriageUseCase.TriageNotFoundException e) {
            ApiResponseDto<TriageStatusResponseDto> response = ApiResponseDto.error(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND,
                    "/triages/" + id + "/status"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            ApiResponseDto<TriageStatusResponseDto> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "/triages/" + id + "/status"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar triagem", description = "Cancela uma triagem pendente")
    public ResponseEntity<ApiResponseDto<TriageStatusResponseDto>> cancelTriage(
            @Parameter(description = "ID da triagem") @PathVariable String id) {

        try {
            UpdateTriageStatusUseCase updateUseCase = new UpdateTriageStatusUseCase(triageRepository);
            Triage cancelledTriage = updateUseCase.cancelTriage(id);
            TriageStatusResponseDto responseDto = TriageStatusResponseDto.fromDomain(cancelledTriage);

            ApiResponseDto<TriageStatusResponseDto> response = ApiResponseDto.success(
                    responseDto,
                    "Triagem cancelada com sucesso"
            );

            return ResponseEntity.ok(response);

        } catch (UpdateTriageStatusUseCase.TriageNotFoundException e) {
            ApiResponseDto<TriageStatusResponseDto> response = ApiResponseDto.error(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND,
                    "/triages/" + id + "/cancel"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (UpdateTriageStatusUseCase.TriageCannotBeCancelledException e) {
            ApiResponseDto<TriageStatusResponseDto> response = ApiResponseDto.error(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    "/triages/" + id + "/cancel"
            );
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            ApiResponseDto<TriageStatusResponseDto> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "/triages/" + id + "/cancel"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/queue/status")
    @Operation(summary = "Status da fila", description = "Estatísticas da fila de processamento")
    public ResponseEntity<ApiResponseDto<QueueStatusDto>> getQueueStatus() {
        try {
            QueueService.QueueStats stats = queueService.getQueueStats();
            QueueStatusDto responseDto = QueueStatusDto.fromQueueStats(stats);

            ApiResponseDto<QueueStatusDto> response = ApiResponseDto.success(
                    responseDto,
                    "Status da fila de processamento"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponseDto<QueueStatusDto> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "/triages/queue/status"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar triagem por ID", description = "Busca uma triagem específica pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Triagem encontrada"),
            @ApiResponse(responseCode = "404", description = "Triagem não encontrada")
    })
    public ResponseEntity<ApiResponseDto<TriageResponseDto>> getTriageById(
            @Parameter(description = "ID da triagem") @PathVariable String id,
            HttpServletRequest httpRequest) {

        try {
            Triage triage = findTriageUseCase.findById(id);
            TriageResponseDto responseDto = TriageResponseDto.fromDomain(triage);

            ApiResponseDto<TriageResponseDto> response = ApiResponseDto.success(
                    responseDto,
                    "Triagem encontrada"
            );

            return ResponseEntity.ok(response);

        } catch (FindTriageUseCase.TriageNotFoundException e) {
            ApiResponseDto<TriageResponseDto> response = ApiResponseDto.error(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND,
                    httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            ApiResponseDto<TriageResponseDto> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Buscar triagens do paciente", description = "Lista todas as triagens de um paciente")
    public ResponseEntity<ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>>> getTriagesByPatient(
            @Parameter(description = "ID do paciente") @PathVariable String patientId) {

        try {
            List<Triage> triages = findTriageUseCase.findByPatientId(patientId);

            List<TriageResponseDto.TriageSummaryDto> responseDtos = triages.stream()
                    .map(TriageResponseDto.TriageSummaryDto::fromDomain)
                    .toList();

            ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>> response = ApiResponseDto.success(
                    responseDtos,
                    String.format("Encontradas %d triagens", responseDtos.size())
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "/triages/patient/" + patientId
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/priority/{priority}")
    @Operation(summary = "Buscar triagens por prioridade", description = "Lista triagens de uma prioridade específica")
    public ResponseEntity<ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>>> getTriagesByPriority(
            @Parameter(description = "Nível de prioridade") @PathVariable PriorityLevel priority) {

        try {
            List<Triage> triages = findTriageUseCase.findByPriority(priority);

            List<TriageResponseDto.TriageSummaryDto> responseDtos = triages.stream()
                    .map(TriageResponseDto.TriageSummaryDto::fromDomain)
                    .toList();

            ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>> response = ApiResponseDto.success(
                    responseDtos,
                    String.format("Encontradas %d triagens com prioridade %s", responseDtos.size(), priority)
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "/triages/priority/" + priority
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/critical")
    @Operation(summary = "Buscar triagens críticas", description = "Lista triagens críticas (Emergency e Very Urgent)")
    public ResponseEntity<ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>>> getCriticalTriages() {

        try {
            List<Triage> triages = findTriageUseCase.findCriticalTriages();

            List<TriageResponseDto.TriageSummaryDto> responseDtos = triages.stream()
                    .map(TriageResponseDto.TriageSummaryDto::fromDomain)
                    .toList();

            ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>> response = ApiResponseDto.success(
                    responseDtos,
                    String.format("Encontradas %d triagens críticas", responseDtos.size())
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "/triages/critical"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/pending")
    @Operation(summary = "Buscar triagens pendentes", description = "Lista triagens aguardando processamento")
    public ResponseEntity<ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>>> getPendingTriages() {

        try {
            List<Triage> triages = findTriageUseCase.findPendingTriagesByStatus();

            List<TriageResponseDto.TriageSummaryDto> responseDtos = triages.stream()
                    .map(TriageResponseDto.TriageSummaryDto::fromDomain)
                    .toList();

            ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>> response = ApiResponseDto.success(
                    responseDtos,
                    String.format("Encontradas %d triagens pendentes", responseDtos.size())
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "/triages/pending"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Buscar triagens por data", description = "Lista triagens de uma data específica")
    public ResponseEntity<ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>>> getTriagesByDate(
            @Parameter(description = "Data no formato yyyy-MM-dd")
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        try {
            List<Triage> triages = findTriageUseCase.findByDate(date);

            List<TriageResponseDto.TriageSummaryDto> responseDtos = triages.stream()
                    .map(TriageResponseDto.TriageSummaryDto::fromDomain)
                    .toList();

            ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>> response = ApiResponseDto.success(
                    responseDtos,
                    String.format("Encontradas %d triagens na data %s", responseDtos.size(), date)
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponseDto<List<TriageResponseDto.TriageSummaryDto>> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "/triages/date/" + date
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/statistics/{date}")
    @Operation(summary = "Estatísticas por data", description = "Retorna estatísticas de triagens de uma data")
    public ResponseEntity<ApiResponseDto<FindTriageUseCase.TriageStatistics>> getTriageStatistics(
            @Parameter(description = "Data no formato yyyy-MM-dd")
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        try {
            FindTriageUseCase.TriageStatistics statistics = findTriageUseCase.getStatisticsByDate(date);

            ApiResponseDto<FindTriageUseCase.TriageStatistics> response = ApiResponseDto.success(
                    statistics,
                    "Estatísticas calculadas com sucesso"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponseDto<FindTriageUseCase.TriageStatistics> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "/triages/statistics/" + date
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}