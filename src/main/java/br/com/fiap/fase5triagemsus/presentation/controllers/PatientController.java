package br.com.fiap.fase5triagemsus.presentation.controllers;


import br.com.fiap.fase5triagemsus.domain.entities.Patient;
import br.com.fiap.fase5triagemsus.presentation.dto.request.PatientRequestDto;
import br.com.fiap.fase5triagemsus.presentation.dto.response.ApiResponseDto;
import br.com.fiap.fase5triagemsus.presentation.dto.response.PatientResponseDto;
import br.com.fiap.fase5triagemsus.usecases.CreatePatientUseCase;
import br.com.fiap.fase5triagemsus.usecases.FindPatientUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "Operações relacionadas aos pacientes")
public class PatientController {

    private final CreatePatientUseCase createPatientUseCase;
    private final FindPatientUseCase findPatientUseCase;


    @PostMapping
    @Operation(summary = "Criar paciente", description = "Cria um novo paciente no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Paciente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Paciente já existe")
    })
    public ResponseEntity<ApiResponseDto<PatientResponseDto>> createPatient(
            @Valid @RequestBody PatientRequestDto request,
            HttpServletRequest httpRequest) {
        try {
            CreatePatientUseCase.CreatePatientCommand command = new CreatePatientUseCase.CreatePatientCommand(
                    request.getName(),
                    request.getCleanCpf(),
                    request.getBirthDate(),
                    request.getNormalizedGender(),
                    request.getCleanPhone(),
                    request.getEmail()
            );


            Patient createdPatient = createPatientUseCase.execute(command);
            PatientResponseDto responseDto = PatientResponseDto.fromDomain(createdPatient);
            ApiResponseDto<PatientResponseDto> response = ApiResponseDto.created(
                    responseDto,
                    "Paciente criado com sucesso"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (CreatePatientUseCase.PatientAlreadyExistsException e) {
            ApiResponseDto<PatientResponseDto> response = ApiResponseDto.error(
                    e.getMessage(),
                    HttpStatus.CONFLICT,
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (IllegalArgumentException e) {
            ApiResponseDto<PatientResponseDto> response = ApiResponseDto.error(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            ApiResponseDto<PatientResponseDto> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar paciente por ID", description = "Busca um paciente específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public ResponseEntity<ApiResponseDto<PatientResponseDto>> getPatientById(
            @Parameter(description = "ID do paciente") @PathVariable String id,
            HttpServletRequest httpRequest) {

        try {
            Patient patient = findPatientUseCase.findById(id);
            PatientResponseDto responseDto = PatientResponseDto.fromDomain(patient);

            ApiResponseDto<PatientResponseDto> response = ApiResponseDto.success(
                    responseDto,
                    "Paciente encontrado"
            );

            return ResponseEntity.ok(response);

        } catch (FindPatientUseCase.PatientNotFoundException e) {
            ApiResponseDto<PatientResponseDto> response = ApiResponseDto.error(
                    e.getMessage(),
                    HttpStatus.NOT_FOUND,
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            ApiResponseDto<PatientResponseDto> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar paciente por CPF", description = "Busca um paciente pelo CPF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public ResponseEntity<ApiResponseDto<PatientResponseDto>> getPatientByCpf(
            @Parameter(description = "CPF do paciente") @PathVariable String cpf,
            HttpServletRequest httpRequest) {
        try {
            Patient patient = findPatientUseCase.findByCpf(cpf);
            PatientResponseDto responseDto = PatientResponseDto.fromDomain(patient);

            ApiResponseDto<PatientResponseDto> response = ApiResponseDto.success(
                    responseDto,
                    "Paciente encontrado"
            );
            return ResponseEntity.ok(response);

        } catch (FindPatientUseCase.PatientNotFoundException e) {
            ApiResponseDto<PatientResponseDto> response = ApiResponseDto.error(
                    "Paciente não encontrado",
                    HttpStatus.NOT_FOUND,
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            ApiResponseDto<PatientResponseDto> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/exists/cpf/{cpf}")
    @Operation(summary = "Verificar existência por CPF", description = "Verifica se existe paciente com o CPF informado")
    public ResponseEntity<ApiResponseDto<Boolean>> checkPatientExistsByCpf(
            @Parameter(description = "CPF do paciente") @PathVariable String cpf) {
        try {
            boolean exists = findPatientUseCase.existsByCpf(cpf);

            ApiResponseDto<Boolean> response = ApiResponseDto.success(
                    exists,
                    exists ? "Paciente encontrado" : "Paciente não encontrado"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponseDto<Boolean> response = ApiResponseDto.error(
                    "Erro interno do servidor",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "/patients/exists/cpf/" + cpf
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}