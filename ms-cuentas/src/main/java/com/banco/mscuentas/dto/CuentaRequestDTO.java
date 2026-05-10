package com.banco.mscuentas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Schema(description = "Datos para crear o actualizar una cuenta bancaria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaRequestDTO {

    @Schema(description = "Número de cuenta", example = "478758")
    @NotBlank
    @Size(min = 3, max = 20, message = "El número de cuenta debe tener entre 3 y 20 caracteres")
    private String numeroCuenta;

    @Schema(description = "Tipo de cuenta", example = "Ahorro", allowableValues = {"Ahorro", "Corriente"})
    @NotBlank(message = "El tipo de cuenta es requerido")
    @Pattern(
        regexp = "^(Ahorro|Corriente)$",
        message = "tipoCuenta debe ser 'Ahorro' o 'Corriente'"
    )
    private String tipoCuenta;

    @Schema(description = "Saldo inicial de la cuenta", example = "2000.00")
    @NotNull
    @DecimalMin(value = "0.00", message = "El saldo inicial no puede ser negativo")
    private BigDecimal saldoInicial;

    @Schema(description = "Estado activo/inactivo", example = "true")
    @NotNull
    private Boolean estado;

    @Schema(description = "ID del cliente propietario de la cuenta", example = "jose.lema")
    @NotBlank
    @Size(min = 3, max = 20)
    private String clienteId;
}
