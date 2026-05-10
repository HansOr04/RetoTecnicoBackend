package com.banco.mscuentas.application.service;

import com.banco.mscuentas.domain.exception.CuentaConMovimientosException;
import com.banco.mscuentas.domain.exception.CuentaDuplicadaException;
import com.banco.mscuentas.domain.exception.CuentaNoEncontradaException;
import com.banco.mscuentas.domain.model.Cuenta;
import com.banco.mscuentas.domain.repository.CuentaRepository;
import com.banco.mscuentas.domain.repository.MovimientoRepository;
import com.banco.mscuentas.dto.CuentaRequestDTO;
import com.banco.mscuentas.dto.CuentaResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private CuentaService cuentaService;

    private Cuenta cuentaJoseLema;
    private CuentaRequestDTO requestJoseLema;

    @BeforeEach
    void setUp() {
        cuentaJoseLema = new Cuenta();
        cuentaJoseLema.setId(1L);
        cuentaJoseLema.setNumeroCuenta("478758");
        cuentaJoseLema.setTipoCuenta("Ahorro");
        cuentaJoseLema.setSaldoInicial(BigDecimal.valueOf(2000.0));
        cuentaJoseLema.setSaldoDisponible(BigDecimal.valueOf(2000.0));
        cuentaJoseLema.setEstado(true);
        cuentaJoseLema.setClienteId("jose.lema");

        requestJoseLema = CuentaRequestDTO.builder()
                .numeroCuenta("478758")
                .tipoCuenta("Ahorro")
                .saldoInicial(BigDecimal.valueOf(2000.0))
                .estado(true)
                .clienteId("jose.lema")
                .build();
    }

    @Test
    void listarTodas_retornaListaConTodasLasCuentas() {
        Cuenta cuentaMarianela = new Cuenta();
        cuentaMarianela.setId(2L);
        cuentaMarianela.setNumeroCuenta("225487");
        cuentaMarianela.setTipoCuenta("Corriente");
        cuentaMarianela.setSaldoInicial(BigDecimal.valueOf(100.0));
        cuentaMarianela.setSaldoDisponible(BigDecimal.valueOf(100.0));
        cuentaMarianela.setEstado(true);
        cuentaMarianela.setClienteId("marianela.montalvo");

        when(cuentaRepository.findAll()).thenReturn(List.of(cuentaJoseLema, cuentaMarianela));

        List<CuentaResponseDTO> resultado = cuentaService.listarTodas();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void listarTodas_repositorioVacio_retornaListaVacia() {
        when(cuentaRepository.findAll()).thenReturn(List.of());

        List<CuentaResponseDTO> resultado = cuentaService.listarTodas();

        assertThat(resultado).isNotNull().isEmpty();
    }

    @Test
    void obtenerPorNumeroCuenta_cuentaExiste_retornaDTO() {
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));

        CuentaResponseDTO resultado = cuentaService.obtenerPorNumeroCuenta("478758");

        assertThat(resultado.getNumeroCuenta()).isEqualTo("478758");
        assertThat(resultado.getTipoCuenta()).isEqualTo("Ahorro");
        assertThat(resultado.getSaldoDisponible()).isEqualByComparingTo(BigDecimal.valueOf(2000.0));
    }

    @Test
    void obtenerPorNumeroCuenta_cuentaNoExiste_lanzaCuentaNoEncontradaException() {
        when(cuentaRepository.findByNumeroCuenta("000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaService.obtenerPorNumeroCuenta("000000"))
                .isInstanceOf(CuentaNoEncontradaException.class)
                .hasMessageContaining("000000");
    }

    @Test
    void obtenerPorClienteId_retornaListaDeCuentas() {
        when(cuentaRepository.findByClienteId("jose.lema")).thenReturn(List.of(cuentaJoseLema));

        List<CuentaResponseDTO> resultado = cuentaService.obtenerPorClienteId("jose.lema");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getClienteId()).isEqualTo("jose.lema");
    }

    @Test
    void crear_numeroCuentaNuevo_retornaDTOConNumeroCorrecto() {
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.empty());
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaJoseLema);

        CuentaResponseDTO resultado = cuentaService.crear(requestJoseLema);

        assertThat(resultado.getNumeroCuenta()).isEqualTo("478758");

        ArgumentCaptor<Cuenta> captor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).save(captor.capture());
        assertThat(captor.getValue().getSaldoDisponible())
                .isEqualByComparingTo(captor.getValue().getSaldoInicial());
    }

    @Test
    void crear_numeroCuentaDuplicado_lanzaCuentaDuplicadaException() {
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));

        assertThatThrownBy(() -> cuentaService.crear(requestJoseLema))
                .isInstanceOf(CuentaDuplicadaException.class)
                .hasMessageContaining("478758");

        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void actualizar_cuentaExistente_actualizaCamposModificables() {
        CuentaRequestDTO dtoActualizado = CuentaRequestDTO.builder()
                .numeroCuenta("478758")
                .tipoCuenta("Corriente")
                .saldoInicial(BigDecimal.valueOf(2000.0))
                .estado(false)
                .clienteId("jose.lema")
                .build();

        Cuenta cuentaActualizada = new Cuenta();
        cuentaActualizada.setId(1L);
        cuentaActualizada.setNumeroCuenta("478758");
        cuentaActualizada.setTipoCuenta("Corriente");
        cuentaActualizada.setEstado(false);
        cuentaActualizada.setSaldoInicial(BigDecimal.valueOf(2000.0));
        cuentaActualizada.setSaldoDisponible(BigDecimal.valueOf(2000.0));
        cuentaActualizada.setClienteId("jose.lema");

        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuentaActualizada);

        CuentaResponseDTO resultado = cuentaService.actualizar("478758", dtoActualizado);

        assertThat(resultado.getTipoCuenta()).isEqualTo("Corriente");
        assertThat(resultado.getEstado()).isFalse();
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
    }

    @Test
    void actualizar_cuentaNoExistente_lanzaCuentaNoEncontradaException() {
        when(cuentaRepository.findByNumeroCuenta("000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaService.actualizar("000000", requestJoseLema))
                .isInstanceOf(CuentaNoEncontradaException.class)
                .hasMessageContaining("000000");
    }

    @Test
    void eliminar_cuentaSinMovimientos_eliminaCorrectamente() {
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));
        when(movimientoRepository.existsByCuenta(cuentaJoseLema)).thenReturn(false);

        cuentaService.eliminar("478758");

        verify(cuentaRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuentaNoExistente_lanzaCuentaNoEncontradaException() {
        when(cuentaRepository.findByNumeroCuenta("000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaService.eliminar("000000"))
                .isInstanceOf(CuentaNoEncontradaException.class)
                .hasMessageContaining("000000");

        verify(cuentaRepository, never()).deleteById(any());
    }

    @Test
    void eliminar_cuentaConMovimientos_lanzaCuentaConMovimientosException() {
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuentaJoseLema));
        when(movimientoRepository.existsByCuenta(cuentaJoseLema)).thenReturn(true);

        assertThatThrownBy(() -> cuentaService.eliminar("478758"))
                .isInstanceOf(CuentaConMovimientosException.class)
                .hasMessageContaining("478758");

        verify(cuentaRepository, never()).deleteById(any());
    }
}
