package com.banco.msclientes.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Datos del cliente (contraseña no incluida)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClienteResponseDTO {

    @Schema(description = "ID interno en base de datos")
    private Long id;

    @Schema(description = "Identificador único del cliente", example = "jose.lema")
    private String clienteId;

    @Schema(description = "Nombre completo", example = "Jose Lema")
    private String nombre;

    @Schema(description = "Género", example = "M")
    private String genero;

    @Schema(description = "Edad en años", example = "35")
    private Integer edad;

    @Schema(description = "Número de identificación", example = "1234567890")
    private String identificacion;

    @Schema(description = "Dirección domiciliaria", example = "Otavalo sn y principal")
    private String direccion;

    @Schema(description = "Número de teléfono", example = "098254785")
    private String telefono;

    @Schema(description = "Estado activo/inactivo", example = "true")
    private Boolean estado;
}
