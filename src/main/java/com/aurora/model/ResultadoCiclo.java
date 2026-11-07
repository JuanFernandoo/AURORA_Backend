package com.aurora.model;

/**
 * Resultado consolidado de un ciclo de simulacion: que fase estuvo activa,
 * cuantos vehiculos quedaron en espera por direccion despues de aplicar la
 * accion, y las metricas asociadas a ese ciclo. Este es el objeto que el
 * motor de simulacion publica a los {@link com.aurora.observer.ObservadorTrafico}
 * registrados (patron Observer).
 *
 * Unidades:
 * - Los vehiculos en espera son conteos enteros (vehiculos).
 * - El tiempo de espera acumulado esta expresado en "vehiculo-segundos"
 *   (suma, ciclo a ciclo, de vehiculosEnCola x duracionDelCiclo). Es una
 *   aproximacion simplificada y determinista del area bajo la curva de
 *   espera, analoga en espiritu al termino W_t de la funcion de recompensa
 *   conceptual de AURORA (R_t = -(alpha*Q_t + beta*W_t)), pero NO es el
 *   resultado de un agente DQN entrenado: aqui es aritmetica simple.
 *
 * @param numeroCiclo                  indice del ciclo (1-based)
 * @param faseActiva                   fase que recibio luz verde en este ciclo
 * @param duracionAccionSegundos       duracion de la accion aplicada
 * @param vehiculosDescargados         vehiculos que lograron cruzar en este ciclo
 * @param vehiculosEsperaNorteSur      vehiculos en espera NS al cierre del ciclo
 * @param vehiculosEsperaEsteOeste     vehiculos en espera EO al cierre del ciclo
 * @param tiempoEsperaAcumuladoVehSeg  tiempo de espera acumulado (vehiculo-segundos) hasta este ciclo, inclusive
 */
public record ResultadoCiclo(
    int numeroCiclo,
    FaseSemaforica faseActiva,
    int duracionAccionSegundos,
    int vehiculosDescargados,
    int vehiculosEsperaNorteSur,
    int vehiculosEsperaEsteOeste,
    double tiempoEsperaAcumuladoVehSeg
) {

    /** Longitud total de la cola (ambas direcciones) al cierre del ciclo. */
    public int longitudColaTotal() {
        return vehiculosEsperaNorteSur + vehiculosEsperaEsteOeste;
    }
}
