package com.banco.msclientes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequestDTO {

    @NotBlank
    private String clienteId;

    @NotBlank
    private String nombre;

    @NotBlank
    private String genero;

    @NotNull
    @Min(0)
    private Integer edad;

    @NotBlank
    private String identificacion;

    @NotBlank
    private String direccion;

    @NotBlank
    private String telefono;

    @NotBlank
    private String contrasena;

    @NotNull
    private Boolean estado;
}
