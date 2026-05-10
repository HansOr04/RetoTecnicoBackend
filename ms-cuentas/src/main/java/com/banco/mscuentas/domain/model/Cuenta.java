package com.banco.mscuentas.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "cuentas",
    indexes = {
        @Index(name = "idx_cuenta_numero_cuenta", columnList = "numero_cuenta"),
        @Index(name = "idx_cuenta_cliente_id", columnList = "cliente_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "numeroCuenta")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "numero_cuenta")
    private String numeroCuenta;

    @Column(nullable = false)
    private String tipoCuenta;

    /**
     * El saldo inicial es histórico y no cambia. El saldo disponible
     * es el único campo que se actualiza, exclusivamente a través de movimientos.
     */
    @Column(nullable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoDisponible;

    private Boolean estado;

    @Column(nullable = false, name = "cliente_id")
    private String clienteId;

    /**
     * Campo de versión para optimistic locking.
     * Previene condiciones de carrera en retiros simultáneos sobre la misma cuenta.
     * Hibernate lanzará OptimisticLockException si dos transacciones
     * concurrentes modifican el mismo registro.
     */
    @Version
    private Long version;
}
