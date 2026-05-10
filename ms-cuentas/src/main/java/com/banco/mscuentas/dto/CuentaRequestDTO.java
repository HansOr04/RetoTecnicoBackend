package com.banco.mscuentas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaRequestDTO {

    @NotBlank
    @Size(min = 3, max = 20, message = "El número de cuenta debe tener entre 3 y 20 caracteres")
    private String numeroCuenta;

    @NotBlank(message = "El tipo de cuenta es requerido")
    @Pattern(
        regexp = "^(Ahorro|Corriente)$",
        message = "tipoCuenta debe ser 'Ahorro' o 'Corriente'"
    )
    private String tipoCuenta;

    @NotNull
    @DecimalMin(value = "0.00", message = "El saldo inicial no puede ser negativo")
    private BigDecimal saldoInicial;

    @NotNull
    private Boolean estado;

    @NotBlank
    @Size(min = 3, max = 20)
    private String clienteId;
}
