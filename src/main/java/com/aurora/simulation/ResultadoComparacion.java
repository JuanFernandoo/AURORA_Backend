package com.aurora.simulation;

/**
 * Agrupa las metricas finales de una estrategia para poder compararlas
 * contra las de la otra estrategia al cierre de la simulacion.
 *
 * @param nombreEstrategia           nombre descriptivo de la estrategia
 * @param tiempoEsperaAcumuladoVehSeg tiempo de espera acumulado final (vehiculo-segundos)
 * @param longitudColaPromedio       longitud de cola promedio a lo largo de la simulacion
 * @param longitudColaMaxima         longitud de cola maxima observada
 * @param vehiculosPendientesFinal   vehiculos en espera al cierre de la simulacion
 */
public record ResultadoComparacion(
    String nombreEstrategia,
    double tiempoEsperaAcumuladoVehSeg,
    double longitudColaPromedio,
    int longitudColaMaxima,
    int vehiculosPendientesFinal
) {
}
