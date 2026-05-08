package com.banco.mscuentas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoRequestDTO {

    @NotBlank
    private String numeroCuenta;

    @NotBlank
    private String tipoMovimiento;

    @NotNull
    @Positive
    private Double valor;
}
