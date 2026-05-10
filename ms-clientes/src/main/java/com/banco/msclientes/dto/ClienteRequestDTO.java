package com.banco.msclientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Schema(description = "Datos para crear o actualizar un cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequestDTO {

    @Schema(description = "Identificador único del cliente", example = "jose.lema")
    @NotBlank
    @Size(min = 3, max = 20, message = "El clienteId debe tener entre 3 y 20 caracteres")
    private String clienteId;

    @Schema(description = "Nombre completo", example = "Jose Lema")
    @NotBlank
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Schema(description = "Género", example = "M")
    @NotBlank
    @Size(min = 1, max = 20)
    private String genero;

    @Schema(description = "Edad en años", example = "35")
    @NotNull
    @Min(0)
    private Integer edad;

    @Schema(description = "Número de identificación", example = "1234567890")
    @NotBlank
    @Size(min = 5, max = 20, message = "La identificación debe tener entre 5 y 20 caracteres")
    private String identificacion;

    @Schema(description = "Dirección domiciliaria", example = "Otavalo sn y principal")
    @NotBlank
    @Size(min = 5, max = 200)
    private String direccion;

    @Schema(description = "Número de teléfono", example = "098254785")
    @NotBlank
    @Size(min = 7, max = 15, message = "El teléfono debe tener entre 7 y 15 caracteres")
    private String telefono;

    @Schema(description = "Contraseña (se almacena con BCrypt)", example = "1234")
    @NotBlank
    @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
    private String contrasena;

    @Schema(description = "Estado activo/inactivo", example = "true")
    @NotNull
    private Boolean estado;
}
