package com.banco.mscuentas.infrastructure.controller;

import com.banco.mscuentas.domain.exception.CuentaConMovimientosException;
import com.banco.mscuentas.domain.exception.CuentaDuplicadaException;
import com.banco.mscuentas.domain.exception.CuentaInactivaException;
import com.banco.mscuentas.domain.exception.CuentaNoEncontradaException;
import com.banco.mscuentas.domain.exception.MovimientoNoEncontradoException;
import com.banco.mscuentas.domain.exception.SaldoInsuficienteException;
import com.banco.mscuentas.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
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
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validación fallida: {}", fieldErrors);
        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, "Errores de validación", fieldErrors));
    }

    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleCuentaNoEncontrada(CuentaNoEncontradaException ex) {
        log.warn("Cuenta no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null));
    }

    @ExceptionHandler(MovimientoNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleMovimientoNoEncontrado(MovimientoNoEncontradoException ex) {
        log.warn("Movimiento no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null));
    }

    @ExceptionHandler(CuentaDuplicadaException.class)
    public ResponseEntity<ApiErrorResponse> handleCuentaDuplicada(CuentaDuplicadaException ex) {
        log.warn("Intento de duplicado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, ex.getMessage(), null));
    }

    @ExceptionHandler(CuentaConMovimientosException.class)
    public ResponseEntity<ApiErrorResponse> handleCuentaConMovimientos(CuentaConMovimientosException ex) {
        log.warn("Intento de eliminar cuenta con movimientos: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, ex.getMessage(), null));
    }

    @ExceptionHandler(CuentaInactivaException.class)
    public ResponseEntity<ApiErrorResponse> handleCuentaInactiva(CuentaInactivaException ex) {
        log.warn("Operación en cuenta inactiva: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(buildError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null));
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ApiErrorResponse> handleSaldoInsuficiente(SaldoInsuficienteException ex) {
        log.warn("Saldo insuficiente: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedOperation(UnsupportedOperationException ex) {
        log.warn("Operación no soportada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(buildError(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null));
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiErrorResponse> handleDateTimeParse(DateTimeParseException ex) {
        log.warn("Error de formato de fecha: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST,
                        "Formato de fecha inválido. Use el formato: yyyy-MM-dd", null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Body no legible: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST,
                        "El cuerpo de la petición no es válido o está malformado", null));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        log.warn("Tipo de contenido no soportado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(buildError(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "Content-Type no soportado. Use application/json", null));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.warn("Conflicto de concurrencia detectado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT,
                        "La operación no pudo completarse por modificación concurrente. Intente nuevamente.",
                        null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        log.error("Error interno no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null));
    }

    private ApiErrorResponse buildError(HttpStatus status, String message, Map<String, String> errors) {
        return ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .errors(errors)
                .build();
    }
}
