package com.banco.mscuentas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Schema(description = "Datos de una cuenta bancaria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CuentaResponseDTO {

    @Schema(description = "ID interno en base de datos")
    private Long id;

    @Schema(description = "Número de cuenta", example = "478758")
    private String numeroCuenta;

    @Schema(description = "Tipo de cuenta", example = "Ahorro")
    private String tipoCuenta;

    @Schema(description = "Saldo inicial registrado al crear la cuenta", example = "2000.00")
    private BigDecimal saldoInicial;

    @Schema(description = "Saldo disponible actual (actualizado por movimientos)", example = "1425.00")
    private BigDecimal saldoDisponible;

    @Schema(description = "Estado activo/inactivo", example = "true")
    private Boolean estado;

    @Schema(description = "ID del cliente propietario", example = "jose.lema")
    private String clienteId;
}
