package com.aurora;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.simulation.SimulacionComparativa;

/**
 * Punto de entrada de la aplicacion.
 *
 * Deliberadamente NO contiene logica de negocio: solo imprime el
 * encabezado explicativo del prototipo y delega la ejecucion real a
 * {@link SimulacionComparativa}. Mantener a Main "delgado" facilita
 * explicar, durante la exposicion, que la simulacion esta desacoplada del
 * arranque del programa.
 */
public final class Main {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println(" AURORA - Demostracion arquitectonica en Java");
        System.out.println(" Adaptive Urban Road Optimization through Real-time AI");
        System.out.println("============================================================");
        System.out.println(" Esta es una simulacion simplificada y determinista que NO");
        System.out.println(" se conecta a Eclipse SUMO, TraCI ni a un modelo DQN real.");
        System.out.println(" Su objetivo es demostrar la arquitectura de software con la");
        System.out.println(" que se organizarian esos componentes en el futuro, aplicando");
        System.out.println(" los patrones Strategy, Singleton, Observer y Factory.");
        System.out.println("============================================================");
        System.out.println();

        ConfiguracionAurora configuracion = ConfiguracionAurora.getInstance();
        SimulacionComparativa simulacion = new SimulacionComparativa(configuracion);
        simulacion.ejecutar();

        System.out.println();
        System.out.println("============================================================");
        System.out.println(" Fin de la simulacion.");
        System.out.println("============================================================");
    }
}
