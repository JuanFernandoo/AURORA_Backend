package com.aurora.strategy;

import com.aurora.model.AccionSemaforica;
import com.aurora.model.EstadoTrafico;

/**
 * PATRON STRATEGY (patron principal de este proyecto).
 *
 * Abstrae el algoritmo de decision que determina la proxima accion del
 * semaforo a partir del estado actual del trafico. Las implementaciones
 * concretas ({@link ControladorCicloFijo}, {@link ControladorAdaptativo})
 * son intercambiables en tiempo de ejecucion sin modificar al cliente que
 * las usa ({@link com.aurora.engine.MotorSemaforico}).
 *
 * Esta interfaz es tambien el punto de extension pensado para la evolucion
 * futura del proyecto: una tercera implementacion, por ejemplo
 * "ControladorDQN" (no incluida en esta entrega), podria delegar esta
 * decision a un servicio externo en Python que ejecute un agente de
 * aprendizaje por refuerzo profundo (DQN) entrenado sobre Eclipse SUMO via
 * TraCI, sin que el resto del sistema Java tenga que cambiar: bastaria con
 * implementar este mismo contrato.
 */
public interface ControladorSemaforico {

    /**
     * Decide la proxima accion semaforica a partir del estado de trafico
     * observado y del numero de ciclo actual (algunas estrategias, como el
     * ciclo fijo, usan el numero de ciclo para alternar de forma
     * determinista; otras, como la adaptativa, lo ignoran).
     *
     * @param estado       estado de trafico observado en el ciclo actual
     * @param cicloActual  indice del ciclo (1-based)
     * @return la accion semaforica a aplicar
     */
    AccionSemaforica decidirAccion(EstadoTrafico estado, int cicloActual);

    /** Nombre descriptivo de la estrategia, usado en reportes y consola. */
    String getNombre();
}
