package com.banco.msclientes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequestDTO {

    @NotBlank
    @Size(min = 3, max = 20, message = "El clienteId debe tener entre 3 y 20 caracteres")
    private String clienteId;

    @NotBlank
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank
    @Size(min = 1, max = 20)
    private String genero;

    @NotNull
    @Min(0)
    private Integer edad;

    @NotBlank
    @Size(min = 5, max = 20, message = "La identificación debe tener entre 5 y 20 caracteres")
    private String identificacion;

    @NotBlank
    @Size(min = 5, max = 200)
    private String direccion;

    @NotBlank
    @Size(min = 7, max = 15, message = "El teléfono debe tener entre 7 y 15 caracteres")
    private String telefono;

    @NotBlank
    @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
    private String contrasena;

    @NotNull
    private Boolean estado;
}
