package com.aurora.factory;

/**
 * Enum que identifica los tipos de {@link com.aurora.strategy.ControladorSemaforico}
 * que la fabrica es capaz de construir. Se usa en lugar de una cadena de
 * texto para que el compilador detecte valores invalidos.
 */
public enum TipoControlador {
    CICLO_FIJO,
    ADAPTATIVO
}
