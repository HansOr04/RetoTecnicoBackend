package com.banco.mscuentas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Línea de estado de cuenta en el reporte por rango de fechas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReporteDTO {

    @Schema(description = "Fecha y hora del movimiento", example = "2024-01-10T10:00:00")
    private LocalDateTime fecha;

    @Schema(description = "clienteId del propietario de la cuenta", example = "jose.lema")
    private String cliente;

    @Schema(description = "Número de cuenta", example = "478758")
    private String numeroCuenta;

    @Schema(description = "Tipo de cuenta", example = "Ahorro")
    private String tipoCuenta;

    @Schema(description = "Saldo inicial con que se abrió la cuenta", example = "2000.00")
    private BigDecimal saldoInicial;

    @Schema(description = "Estado activo/inactivo de la cuenta", example = "true")
    private Boolean estado;

    @Schema(description = "Monto del movimiento con signo (negativo=retiro, positivo=depósito)", example = "-575.00")
    private BigDecimal movimiento;

    @Schema(description = "Saldo disponible después del movimiento", example = "1425.00")
    private BigDecimal saldoDisponible;
}
