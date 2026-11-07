package com.aurora.simulation;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.engine.MotorSemaforico;
import com.aurora.factory.ControladorFactory;
import com.aurora.factory.TipoControlador;
import com.aurora.model.EstadoTrafico;
import com.aurora.observer.ObservadorConsola;
import com.aurora.observer.ObservadorMetricas;
import com.aurora.strategy.ControladorSemaforico;

/**
 * Orquesta la demostracion completa: ejecuta la simulacion con el
 * controlador de ciclo fijo y con el controlador adaptativo, bajo
 * EXACTAMENTE las mismas condiciones iniciales (mismo numero de vehiculos
 * de partida, mismas llegadas deterministas por ciclo, mismo numero de
 * ciclos), y presenta una comparacion final.
 *
 * Esta clase mantiene a {@code Main} libre de logica de negocio: Main solo
 * imprime el encabezado del programa y delega aqui la ejecucion real.
 */
public final class SimulacionComparativa {

    private final ConfiguracionAurora configuracion;

    public SimulacionComparativa(ConfiguracionAurora configuracion) {
        this.configuracion = configuracion;
    }

    /** Ejecuta ambas estrategias bajo las mismas condiciones y muestra la comparacion. */
    public void ejecutar() {
        EstadoTrafico estadoInicial = new EstadoTrafico(
            configuracion.getVehiculosInicialesNorteSur(),
            configuracion.getVehiculosInicialesEsteOeste()
        );
        int numeroCiclos = configuracion.getNumeroCiclosSimulacion();

        System.out.println("Estado inicial identico para ambas estrategias: "
            + "Norte-Sur=" + estadoInicial.vehiculosEsperaNorteSur()
            + ", Este-Oeste=" + estadoInicial.vehiculosEsperaEsteOeste()
            + " | Ciclos a simular: " + numeroCiclos);
        System.out.println();

        ResultadoComparacion resultadoCicloFijo = ejecutarEstrategia(TipoControlador.CICLO_FIJO, estadoInicial, numeroCiclos);
        System.out.println();
        ResultadoComparacion resultadoAdaptativo = ejecutarEstrategia(TipoControlador.ADAPTATIVO, estadoInicial, numeroCiclos);
        System.out.println();

        imprimirComparacion(resultadoCicloFijo, resultadoAdaptativo);
    }

    private ResultadoComparacion ejecutarEstrategia(TipoControlador tipo, EstadoTrafico estadoInicial, int numeroCiclos) {
        // FACTORY: se construye el controlador solicitado sin que esta clase
        // conozca el "new" concreto de cada estrategia.
        ControladorSemaforico controlador = ControladorFactory.crear(tipo, configuracion);

        System.out.println("=== Simulando estrategia: " + controlador.getNombre() + " ===");

        // STRATEGY: el motor recibe el controlador por inyeccion de
        // dependencias; no conoce su tipo concreto.
        MotorSemaforico motor = new MotorSemaforico(controlador, configuracion);

        // OBSERVER: se registran dos observadores independientes sin que el
        // motor necesite saber que hacen con los datos.
        ObservadorMetricas metricas = new ObservadorMetricas();
        motor.registrarObservador(new ObservadorConsola(controlador.getNombre()));
        motor.registrarObservador(metricas);

        motor.ejecutarSimulacion(estadoInicial, numeroCiclos);

        return new ResultadoComparacion(
            controlador.getNombre(),
            metricas.getTiempoEsperaAcumuladoFinalVehSeg(),
            metricas.getLongitudColaPromedio(),
            metricas.getLongitudColaMaxima(),
            metricas.getVehiculosPendientesFinal()
        );
    }

    private void imprimirComparacion(ResultadoComparacion a, ResultadoComparacion b) {
        System.out.println("=== Comparacion final ===");
        System.out.printf("%-22s | %-20s | %-20s%n", "Metrica", a.nombreEstrategia(), b.nombreEstrategia());
        System.out.println("-".repeat(70));
        System.out.printf("%-22s | %-20.1f | %-20.1f%n", "Espera acum. (veh-s)",
            a.tiempoEsperaAcumuladoVehSeg(), b.tiempoEsperaAcumuladoVehSeg());
        System.out.printf("%-22s | %-20.2f | %-20.2f%n", "Cola promedio (veh)",
            a.longitudColaPromedio(), b.longitudColaPromedio());
        System.out.printf("%-22s | %-20d | %-20d%n", "Cola maxima (veh)",
            a.longitudColaMaxima(), b.longitudColaMaxima());
        System.out.printf("%-22s | %-20d | %-20d%n", "Pendientes al cierre",
            a.vehiculosPendientesFinal(), b.vehiculosPendientesFinal());
        System.out.println();

        imprimirDiferenciaPorcentual(
            "Tiempo de espera acumulado",
            a.tiempoEsperaAcumuladoVehSeg(),
            b.tiempoEsperaAcumuladoVehSeg(),
            a.nombreEstrategia(),
            b.nombreEstrategia()
        );
    }

    /**
     * Calcula y muestra la diferencia porcentual entre ambas estrategias
     * SOLO cuando es matematicamente valida (referencia distinta de cero).
     * No se asume de antemano cual estrategia "gana": se reporta el
     * resultado real, sea cual sea.
     */
    private void imprimirDiferenciaPorcentual(String metrica, double valorA, double valorB, String nombreA, String nombreB) {
        if (valorA == 0.0) {
            System.out.println("Diferencia porcentual de " + metrica + ": no calculable (referencia de "
                + nombreA + " es 0).");
            return;
        }
        double diferenciaPorcentual = ((valorA - valorB) / valorA) * 100.0;
        if (diferenciaPorcentual > 0) {
            System.out.printf(
                "Diferencia porcentual de %s: %s redujo el valor de %s en un %.1f%% respecto a %s.%n",
                metrica, nombreB, metrica, diferenciaPorcentual, nombreA
            );
        } else if (diferenciaPorcentual < 0) {
            System.out.printf(
                "Diferencia porcentual de %s: en este escenario, %s obtuvo un valor %.1f%% MAYOR que %s "
                    + "(no siempre la estrategia adaptativa obtiene mejor resultado; depende de la heuristica "
                    + "y del escenario de trafico simulado).%n",
                metrica, nombreB, Math.abs(diferenciaPorcentual), nombreA
            );
        } else {
            System.out.printf("Diferencia porcentual de %s: 0%% (ambas estrategias obtuvieron el mismo resultado).%n", metrica);
        }
    }
}
