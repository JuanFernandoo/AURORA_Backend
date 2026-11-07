package com.aurora.factory;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.strategy.ControladorAdaptativo;
import com.aurora.strategy.ControladorCicloFijo;
import com.aurora.strategy.ControladorSemaforico;

/**
 * PATRON FACTORY SIMPLE (Simple Factory).
 *
 * Nota de nomenclatura: esta clase implementa el patron "Factory Simple" (un
 * unico metodo estatico que construye el objeto solicitado a partir de un
 * enum), NO el patron "Factory Method" de GoF, que requiere una jerarquia de
 * clases creadoras con un metodo de fabricacion sobreescribible por cada
 * subclase. Se deja esta distincion explicita para no afirmar algo que la
 * implementacion no cumple.
 *
 * Proposito: centraliza la logica de construccion de cada
 * {@link ControladorSemaforico} concreto en un unico lugar, evitando que
 * condicionales "if / switch" sobre el tipo de controlador se dispersen por
 * distintas partes del codigo (por ejemplo, en la clase de simulacion o en
 * Main).
 */
public final class ControladorFactory {

    private ControladorFactory() {
        // Clase de utilidad: no se instancia.
    }

    /**
     * Construye el controlador semaforico solicitado.
     *
     * @param tipo          tipo de controlador a construir
     * @param configuracion configuracion centralizada (Singleton) de donde se
     *                      leen los parametros necesarios para cada estrategia
     * @return una nueva instancia de {@link ControladorSemaforico}
     */
    public static ControladorSemaforico crear(TipoControlador tipo, ConfiguracionAurora configuracion) {
        return switch (tipo) {
            case CICLO_FIJO -> new ControladorCicloFijo(configuracion);
            case ADAPTATIVO -> new ControladorAdaptativo(configuracion);
        };
    }
}
