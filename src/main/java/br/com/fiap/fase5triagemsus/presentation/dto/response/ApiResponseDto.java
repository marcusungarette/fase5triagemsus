package br.com.fiap.fase5triagemsus.presentation.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    private Boolean success;
    private String message;
    private T data;
    private ErrorDetails error;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;
    private Integer statusCode;


    public static <T> ApiResponseDto<T> success(T data, String message) {
        return new ApiResponseDto<T>(
                true,
                message,
                data,
                null,
                LocalDateTime.now(),
                null,
                HttpStatus.OK.value()
        );
    }


    public static <T> ApiResponseDto<T> success(T data) {
        return success(data, "Operação realizada com sucesso");
    }

    public static <T> ApiResponseDto<T> created(T data, String message) {
        return new ApiResponseDto<T>(
                true,
                message,
                data,
                null,
                LocalDateTime.now(),
                null,
                HttpStatus.CREATED.value()
        );
    }

    public static <T> ApiResponseDto<T> error(String message, HttpStatus status, String path) {
        ErrorDetails errorDetails = new ErrorDetails(status.name(), message, null);

        return new ApiResponseDto<T>(
                false,
                message,
                null,
                errorDetails,
                LocalDateTime.now(),
                path,
                status.value()
        );
    }

    public static <T> ApiResponseDto<T> validationError(List<String> validationErrors, String path) {
        ErrorDetails errorDetails = new ErrorDetails(
                "VALIDATION_ERROR",
                "Dados inválidos fornecidos",
                validationErrors
        );

        return new ApiResponseDto<T>(
                false,
                "Erro de validação",
                null,
                errorDetails,
                LocalDateTime.now(),
                path,
                HttpStatus.BAD_REQUEST.value()
        );
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String code;
        private String message;
        private List<String> details;
    }
}