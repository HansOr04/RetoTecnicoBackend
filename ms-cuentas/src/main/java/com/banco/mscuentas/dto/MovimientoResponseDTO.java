package com.banco.mscuentas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Resultado de un movimiento registrado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovimientoResponseDTO {

    @Schema(description = "ID del movimiento")
    private Long id;

    @Schema(description = "Fecha y hora del movimiento", example = "2024-01-10T10:00:00")
    private LocalDateTime fecha;

    @Schema(description = "Tipo de movimiento", example = "Retiro")
    private String tipoMovimiento;

    @Schema(description = "Monto del movimiento (positivo)", example = "575.00")
    private BigDecimal valor;

    @Schema(description = "Saldo resultante después del movimiento", example = "1425.00")
    private BigDecimal saldo;

    @Schema(description = "Número de cuenta asociada", example = "478758")
    private String numeroCuenta;
}
