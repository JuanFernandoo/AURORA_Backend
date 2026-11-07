package com.aurora.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.model.EstadoTrafico;
import com.aurora.model.FaseSemaforica;
import com.aurora.model.ResultadoCiclo;
import com.aurora.observer.ObservadorMetricas;
import com.aurora.strategy.ControladorCicloFijo;

class MotorSemaforicoTest {

    private final ConfiguracionAurora configuracion = ConfiguracionAurora.getInstance();

    /**
     * Verifica, con numeros calculados a mano, el resultado exacto de dos
     * ciclos de simulacion con el controlador de ciclo fijo, partiendo de
     * 12 vehiculos Norte-Sur y 8 Este-Oeste (el escenario base del
     * enunciado). Esto confirma que la simulacion es determinista y que
     * los siete pasos del ciclo (leer estado, decidir, aplicar, actualizar
     * llegadas, calcular metricas, notificar) se ejecutan correctamente.
     */
    @Test
    void laSimulacionEsDeterministaYProduceLosValoresEsperados() {
        ControladorCicloFijo controlador = new ControladorCicloFijo(configuracion);
        MotorSemaforico motor = new MotorSemaforico(controlador, configuracion);
        EstadoTrafico estadoInicial = new EstadoTrafico(12, 8);

        List<ResultadoCiclo> historial = motor.ejecutarSimulacion(estadoInicial, 2);

        assertEquals(2, historial.size());

        ResultadoCiclo ciclo1 = historial.get(0);
        assertEquals(FaseSemaforica.NORTE_SUR, ciclo1.faseActiva());
        assertEquals(7, ciclo1.vehiculosDescargados(), "min(12, 15/2=7) = 7");
        assertEquals(8, ciclo1.vehiculosEsperaNorteSur(), "12 - 7 + 3 (llegada) = 8");
        assertEquals(10, ciclo1.vehiculosEsperaEsteOeste(), "8 + 2 (llegada, sin descarga) = 10");
        assertEquals(270.0, ciclo1.tiempoEsperaAcumuladoVehSeg(), 0.0001, "(8 + 10) * 15 = 270");

        ResultadoCiclo ciclo2 = historial.get(1);
        assertEquals(FaseSemaforica.ESTE_OESTE, ciclo2.faseActiva());
        assertEquals(7, ciclo2.vehiculosDescargados(), "min(10, 15/2=7) = 7");
        assertEquals(11, ciclo2.vehiculosEsperaNorteSur(), "8 + 3 (llegada, sin descarga) = 11");
        assertEquals(5, ciclo2.vehiculosEsperaEsteOeste(), "10 - 7 + 2 (llegada) = 5");
        assertEquals(510.0, ciclo2.tiempoEsperaAcumuladoVehSeg(), 0.0001, "270 + (11 + 5) * 15 = 510");
    }

    @Test
    void elMotorNotificaATodosLosObservadoresRegistrados() {
        ControladorCicloFijo controlador = new ControladorCicloFijo(configuracion);
        MotorSemaforico motor = new MotorSemaforico(controlador, configuracion);
        ObservadorMetricas metricasA = new ObservadorMetricas();
        ObservadorMetricas metricasB = new ObservadorMetricas();
        motor.registrarObservador(metricasA);
        motor.registrarObservador(metricasB);

        motor.ejecutarSimulacion(new EstadoTrafico(12, 8), 3);

        assertEquals(3, metricasA.getCiclosProcesados());
        assertEquals(3, metricasB.getCiclosProcesados());
        assertEquals(metricasA.getTiempoEsperaAcumuladoFinalVehSeg(), metricasB.getTiempoEsperaAcumuladoFinalVehSeg(), 0.0001,
            "Ambos observadores deben recibir exactamente los mismos eventos reales generados por la simulacion");
    }

    @Test
    void setControladorPermiteCambiarDeEstrategiaEnTiempoDeEjecucionSinModificarElMotor() {
        ControladorCicloFijo cicloFijo = new ControladorCicloFijo(configuracion);
        MotorSemaforico motor = new MotorSemaforico(cicloFijo, configuracion);

        assertEquals(cicloFijo, motor.getControlador());

        com.aurora.strategy.ControladorAdaptativo adaptativo = new com.aurora.strategy.ControladorAdaptativo(configuracion);
        motor.setControlador(adaptativo);

        assertTrue(motor.getControlador() == adaptativo, "El motor debe exponer la nueva estrategia inyectada");
    }
}
