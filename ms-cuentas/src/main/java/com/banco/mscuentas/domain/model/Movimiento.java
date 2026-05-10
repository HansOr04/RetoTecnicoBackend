package com.banco.mscuentas.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "movimientos",
    indexes = {
        @Index(name = "idx_movimiento_cuenta_id", columnList = "cuenta_id"),
        @Index(name = "idx_movimiento_fecha", columnList = "fecha"),
        @Index(name = "idx_movimiento_cuenta_fecha", columnList = "cuenta_id,fecha")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Inmutable una vez registrado. Los movimientos bancarios son registros
     * históricos que no deben modificarse después de su creación.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    /**
     * Inmutable una vez registrado. Los movimientos bancarios son registros
     * históricos que no deben modificarse después de su creación.
     */
    @Column(updatable = false)
    private String tipoMovimiento;

    /**
     * Inmutable una vez registrado. Los movimientos bancarios son registros
     * históricos que no deben modificarse después de su creación.
     */
    @Column(nullable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(precision = 15, scale = 2)
    private BigDecimal saldo;

    @ManyToOne
    @JoinColumn(name = "cuenta_id")
    private Cuenta cuenta;
}
