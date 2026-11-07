package com.aurora.observer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.aurora.model.FaseSemaforica;
import com.aurora.model.ResultadoCiclo;

class ObservadorMetricasTest {

    @Test
    void acumulaCorrectamenteLasMetricasDeVariosCiclos() {
        ObservadorMetricas observador = new ObservadorMetricas();

        // Ciclo 1: cola total = 10, acumulado = 150.0
        observador.onCicloCompletado(new ResultadoCiclo(1, FaseSemaforica.NORTE_SUR, 15, 5, 6, 4, 150.0));
        // Ciclo 2: cola total = 6, acumulado = 240.0 (valor final esperado)
        observador.onCicloCompletado(new ResultadoCiclo(2, FaseSemaforica.ESTE_OESTE, 15, 4, 3, 3, 240.0));

        assertEquals(2, observador.getCiclosProcesados());
        assertEquals(8.0, observador.getLongitudColaPromedio(), 0.0001, "Promedio de (10 + 6) / 2 = 8");
        assertEquals(10, observador.getLongitudColaMaxima());
        assertEquals(240.0, observador.getTiempoEsperaAcumuladoFinalVehSeg(), 0.0001,
            "Debe quedarse con el valor acumulado del ULTIMO ciclo notificado");
        assertEquals(6, observador.getVehiculosPendientesFinal());
    }

    @Test
    void sinCiclosProcesadosElPromedioEsCero() {
        ObservadorMetricas observador = new ObservadorMetricas();

        assertEquals(0, observador.getCiclosProcesados());
        assertEquals(0.0, observador.getLongitudColaPromedio(), 0.0001);
    }
}
