package com.banco.mscuentas.domain.repository;

import com.banco.mscuentas.domain.model.Cuenta;
import com.banco.mscuentas.domain.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByCuentaAndFechaBetween(Cuenta cuenta, LocalDateTime inicio, LocalDateTime fin);

    List<Movimiento> findByCuenta_ClienteIdAndFechaBetween(String clienteId, LocalDateTime inicio, LocalDateTime fin);

    boolean existsByCuenta(Cuenta cuenta);
}
