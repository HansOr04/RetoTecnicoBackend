package com.banco.mscuentas.domain.model;

/**
 * Tipos de movimiento válidos para transacciones bancarias.
 * Usar este enum evita strings mágicos y hace extensible la lógica de negocio.
 */
public enum TipoMovimiento {

    DEPOSITO("Deposito"),
    RETIRO("Retiro");

    private final String descripcion;

    TipoMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Convierte un String al enum correspondiente (case-insensitive).
     *
     * @param valor texto recibido del cliente ("Deposito", "Retiro")
     * @return el enum correspondiente
     * @throws IllegalArgumentException si el valor no corresponde a ningún tipo válido
     */
    public static TipoMovimiento fromString(String valor) {
        for (TipoMovimiento tipo : values()) {
            if (tipo.descripcion.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException(
                "Tipo de movimiento inválido: '" + valor + "'. Use 'Deposito' o 'Retiro'");
    }
}
