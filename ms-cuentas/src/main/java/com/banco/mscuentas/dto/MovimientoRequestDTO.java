package com.banco.mscuentas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Schema(description = "Datos para registrar un movimiento (depósito o retiro)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoRequestDTO {

    @Schema(description = "Número de cuenta", example = "478758")
    @NotBlank
    @Size(min = 3, max = 20)
    private String numeroCuenta;

    @Schema(description = "Tipo de movimiento", example = "Retiro", allowableValues = {"Deposito", "Retiro"})
    @NotBlank
    @Size(min = 3, max = 20)
    private String tipoMovimiento;

    @Schema(description = "Monto del movimiento (positivo)", example = "575.00")
    @NotNull
    @DecimalMin(value = "0.01", message = "El valor debe ser mayor a cero")
    @DecimalMax(value = "9999999.99", message = "El valor no puede exceder 9999999.99")
    private BigDecimal valor;
}
