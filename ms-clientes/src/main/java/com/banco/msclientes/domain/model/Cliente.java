package com.banco.msclientes.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "clientes",
    indexes = {
        @Index(name = "idx_cliente_cliente_id", columnList = "cliente_id"),
        @Index(name = "idx_cliente_identificacion", columnList = "identificacion")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = "contrasena")
@EqualsAndHashCode(of = "clienteId", callSuper = false)
public class Cliente extends Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "cliente_id")
    private String clienteId;

    private String contrasena;

    private Boolean estado;
}
