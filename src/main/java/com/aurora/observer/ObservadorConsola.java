package com.aurora.observer;

import com.aurora.model.ResultadoCiclo;

/**
 * Observador concreto que imprime en consola un resumen legible de cada
 * ciclo de la simulacion, a medida que ocurre. Util para mostrar en vivo,
 * durante la exposicion, como evoluciona la cola de vehiculos ciclo a ciclo.
 */
public final class ObservadorConsola implements ObservadorTrafico {

    private final String etiquetaEstrategia;

    public ObservadorConsola(String etiquetaEstrategia) {
        this.etiquetaEstrategia = etiquetaEstrategia;
    }

    @Override
    public void onCicloCompletado(ResultadoCiclo resultado) {
        System.out.printf(
            "[%s] Ciclo %2d | Fase: %-11s (%2ds) | Descargados: %2d | Espera NS=%2d EO=%2d (total=%2d) | Acum. espera=%.1f veh-s%n",
            etiquetaEstrategia,
            resultado.numeroCiclo(),
            resultado.faseActiva(),
            resultado.duracionAccionSegundos(),
            resultado.vehiculosDescargados(),
            resultado.vehiculosEsperaNorteSur(),
            resultado.vehiculosEsperaEsteOeste(),
            resultado.longitudColaTotal(),
            resultado.tiempoEsperaAcumuladoVehSeg()
        );
    }
}
