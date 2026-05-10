package com.banco.msclientes.domain.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Persona {

    private String nombre;
    private String genero;
    @Max(150)
    private Integer edad;
    private String identificacion;
    private String direccion;
    private String telefono;
}
