package com.banco.mscuentas.infrastructure.controller;

import com.banco.mscuentas.application.service.IMovimientoService;
import com.banco.mscuentas.dto.ApiErrorResponse;
import com.banco.mscuentas.dto.ReporteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Tag(name = "Reportes", description = "Estado de cuenta por cliente y rango de fechas")
@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final IMovimientoService movimientoService;

    public ReporteController(IMovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @Operation(summary = "Generar reporte de movimientos por cliente y rango de fechas",
            description = "Retorna todos los movimientos del cliente en el rango [fechaInicio, fechaFin]. "
                    + "Los retiros aparecen con valor negativo y los depósitos con valor positivo.")
    @ApiResponse(responseCode = "200", description = "Reporte generado")
    @ApiResponse(responseCode = "400", description = "Fechas inválidas o rango incorrecto",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping
    public ResponseEntity<List<ReporteDTO>> generarReporte(
            @RequestParam String clienteId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {

        LocalDateTime inicio = LocalDate.parse(fechaInicio).atStartOfDay();
        LocalDateTime fin = LocalDate.parse(fechaFin).atTime(LocalTime.MAX);

        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("fechaInicio no puede ser posterior a fechaFin");
        }

        return ResponseEntity.ok(movimientoService.generarReporte(clienteId, inicio, fin));
    }
}
