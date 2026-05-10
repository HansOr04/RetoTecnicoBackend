package com.banco.mscuentas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoRequestDTO {

    @NotBlank
    @Size(min = 3, max = 20)
    private String numeroCuenta;

    @NotBlank
    @Size(min = 3, max = 20)
    private String tipoMovimiento;

    @NotNull
    @DecimalMin(value = "0.01", message = "El valor debe ser mayor a cero")
    private BigDecimal valor;
}
