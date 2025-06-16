package br.com.fiap.fase5triagemsus.presentation.exception;


import br.com.fiap.fase5triagemsus.presentation.dto.response.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Erro de validação na requisição: {}", request.getRequestURI());

        List<String> errors = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String errorMessage = String.format("%s: %s", error.getField(), error.getDefaultMessage());
            errors.add(errorMessage);
            log.debug("Campo inválido: {}", errorMessage);
        }

        ApiResponseDto<Object> response = ApiResponseDto.validationError(errors, request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Argumento ilegal: {} - URI: {}", ex.getMessage(), request.getRequestURI());

        ApiResponseDto<Object> response = ApiResponseDto.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.warn("Erro de conversão de tipo: {} - URI: {}", ex.getMessage(), request.getRequestURI());

        String message = String.format("Valor inválido para o parâmetro '%s': %s",
                ex.getName(), ex.getValue());

        ApiResponseDto<Object> response = ApiResponseDto.error(
                message,
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleInvalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("JSON inválido na requisição: {} - URI: {}", ex.getMessage(), request.getRequestURI());

        ApiResponseDto<Object> response = ApiResponseDto.error(
                "Formato JSON inválido",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        log.warn("Método HTTP não suportado: {} - URI: {}", ex.getMethod(), request.getRequestURI());

        String message = String.format("Método %s não é suportado para este endpoint", ex.getMethod());

        ApiResponseDto<Object> response = ApiResponseDto.error(
                message,
                HttpStatus.METHOD_NOT_ALLOWED,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleNotFound(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        log.warn("Endpoint não encontrado: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        ApiResponseDto<Object> response = ApiResponseDto.error(
                "Endpoint não encontrado",
                HttpStatus.NOT_FOUND,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        log.error("Erro runtime não tratado: {} - URI: {}", ex.getMessage(), request.getRequestURI(), ex);

        String message = "Erro interno do servidor";

        ApiResponseDto<Object> response = ApiResponseDto.error(
                message,
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Erro não previsto: {} - URI: {}", ex.getMessage(), request.getRequestURI(), ex);

        ApiResponseDto<Object> response = ApiResponseDto.error(
                "Erro interno do servidor",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
