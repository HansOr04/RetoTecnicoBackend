package com.banco.mscuentas.infrastructure.controller;

import com.banco.mscuentas.domain.exception.CuentaConMovimientosException;
import com.banco.mscuentas.domain.exception.CuentaDuplicadaException;
import com.banco.mscuentas.domain.exception.CuentaNoEncontradaException;
import com.banco.mscuentas.domain.exception.SaldoInsuficienteException;
import com.banco.mscuentas.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validación fallida: {}", fieldErrors);
        return ResponseEntity.badRequest().body(buildBody(HttpStatus.BAD_REQUEST.value(),
                "Errores de validación", fieldErrors));
    }

    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaNoEncontrada(CuentaNoEncontradaException ex) {
        log.warn("Cuenta no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildBody(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(CuentaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaDuplicada(CuentaDuplicadaException ex) {
        log.warn("Intento de duplicado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(CuentaConMovimientosException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaConMovimientos(CuentaConMovimientosException ex) {
        log.warn("Intento de eliminar cuenta con movimientos: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleSaldoInsuficiente(SaldoInsuficienteException ex) {
        log.warn("Saldo insuficiente: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildBody(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildBody(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, Object>> handleDateTimeParse(DateTimeParseException ex) {
        log.warn("Error de formato de fecha: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildBody(HttpStatus.BAD_REQUEST.value(),
                        "Formato de fecha inválido. Use el formato: yyyy-MM-dd", null));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.warn("Conflicto de concurrencia detectado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .status(409)
                        .error("Conflict")
                        .message("La operación no pudo completarse por modificación concurrente. Intente nuevamente.")
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Error interno no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Error interno del servidor", null));
    }

    private Map<String, Object> buildBody(int status, String message, Map<String, String> errors) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("message", message);
        body.put("errors", errors != null ? errors : Map.of());
        return body;
    }
}
