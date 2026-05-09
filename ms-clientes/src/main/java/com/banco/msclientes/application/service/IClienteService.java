package com.banco.msclientes.application.service;

import com.banco.msclientes.dto.ClienteRequestDTO;
import com.banco.msclientes.dto.ClienteResponseDTO;

import java.util.List;

/**
 * Contrato de casos de uso para la gestión de clientes.
 * Las implementaciones deben garantizar que la contraseña nunca se exponga en respuestas.
 */
public interface IClienteService {

    /**
     * Retorna todos los clientes registrados.
     *
     * @return lista de clientes; vacía si no hay registros
     */
    List<ClienteResponseDTO> listarTodos();

    /**
     * Busca un cliente por su identificador de negocio.
     *
     * @param clienteId identificador de negocio único
     * @return DTO del cliente encontrado
     * @throws com.banco.msclientes.domain.exception.ClienteNoEncontradoException si no existe
     */
    ClienteResponseDTO obtenerPorClienteId(String clienteId);

    /**
     * Registra un nuevo cliente. La contraseña se almacena con hash BCrypt.
     *
     * @param dto datos del cliente a crear
     * @return DTO del cliente creado (sin contraseña)
     * @throws com.banco.msclientes.domain.exception.ClienteDuplicadoException si el clienteId ya existe
     */
    ClienteResponseDTO crear(ClienteRequestDTO dto);

    /**
     * Actualiza los datos de un cliente existente. La contraseña se re-hashea.
     *
     * @param clienteId identificador del cliente a actualizar
     * @param dto       nuevos datos
     * @return DTO con los datos actualizados
     * @throws com.banco.msclientes.domain.exception.ClienteNoEncontradoException si no existe
     */
    ClienteResponseDTO actualizar(String clienteId, ClienteRequestDTO dto);

    /**
     * Elimina un cliente por su identificador de negocio.
     *
     * @param clienteId identificador del cliente a eliminar
     * @throws com.banco.msclientes.domain.exception.ClienteNoEncontradoException si no existe
     */
    void eliminar(String clienteId);
}
