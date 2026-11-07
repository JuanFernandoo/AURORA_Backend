package com.aurora.observer;

import com.aurora.model.ResultadoCiclo;

/**
 * Observador concreto que recopila metricas agregadas de la simulacion:
 * tiempo de espera acumulado final, longitud de cola promedio y maxima, y
 * total de vehiculos que quedaron pendientes al cierre. No imprime nada en
 * consola: solo acumula datos para que otra clase (por ejemplo, la
 * simulacion comparativa) los consulte al finalizar.
 */
public final class ObservadorMetricas implements ObservadorTrafico {

    private int ciclosProcesados = 0;
    private long sumaLongitudColas = 0;
    private int longitudColaMaxima = 0;
    private double tiempoEsperaAcumuladoFinalVehSeg = 0.0;
    private int vehiculosPendientesFinal = 0;

    @Override
    public void onCicloCompletado(ResultadoCiclo resultado) {
        ciclosProcesados++;
        int colaTotal = resultado.longitudColaTotal();
        sumaLongitudColas += colaTotal;
        longitudColaMaxima = Math.max(longitudColaMaxima, colaTotal);
        // El motor notifica los ciclos en orden, por lo que el ultimo valor
        // recibido es, por definicion, el estado final de la simulacion.
        tiempoEsperaAcumuladoFinalVehSeg = resultado.tiempoEsperaAcumuladoVehSeg();
        vehiculosPendientesFinal = colaTotal;
    }

    public int getCiclosProcesados() {
        return ciclosProcesados;
    }

    public double getLongitudColaPromedio() {
        return ciclosProcesados == 0 ? 0.0 : (double) sumaLongitudColas / ciclosProcesados;
    }

    public int getLongitudColaMaxima() {
        return longitudColaMaxima;
    }

    public double getTiempoEsperaAcumuladoFinalVehSeg() {
        return tiempoEsperaAcumuladoFinalVehSeg;
    }

    public int getVehiculosPendientesFinal() {
        return vehiculosPendientesFinal;
    }
}
