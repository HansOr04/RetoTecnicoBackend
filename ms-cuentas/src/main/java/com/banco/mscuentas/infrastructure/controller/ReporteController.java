package com.banco.mscuentas.infrastructure.controller;

import com.banco.mscuentas.application.service.IMovimientoService;
import com.banco.mscuentas.dto.ReporteDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final IMovimientoService movimientoService;

    public ReporteController(IMovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

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
