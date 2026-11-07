package com.aurora.strategy;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.model.AccionSemaforica;
import com.aurora.model.EstadoTrafico;
import com.aurora.model.FaseSemaforica;

/**
 * Estrategia de control por CICLO FIJO: representa el comportamiento de un
 * semaforo tradicional de tiempos preconfigurados. Alterna la fase activa
 * cada ciclo (Norte-Sur, Este-Oeste, Norte-Sur, ...) con una duracion fija,
 * sin considerar el estado real del trafico. Es la linea base contra la
 * cual se compara el controlador adaptativo.
 */
public final class ControladorCicloFijo implements ControladorSemaforico {

    private final int duracionSegundos;

    public ControladorCicloFijo(ConfiguracionAurora configuracion) {
        this.duracionSegundos = configuracion.getDuracionFaseFijaSegundos();
    }

    @Override
    public AccionSemaforica decidirAccion(EstadoTrafico estado, int cicloActual) {
        // Regla fija: en ciclos impares, Norte-Sur; en ciclos pares, Este-Oeste.
        // No se consulta "estado" en absoluto: esa es precisamente la
        // limitacion que AURORA busca superar con el controlador adaptativo.
        FaseSemaforica fase = (cicloActual % 2 != 0) ? FaseSemaforica.NORTE_SUR : FaseSemaforica.ESTE_OESTE;
        return new AccionSemaforica(fase, duracionSegundos);
    }

    @Override
    public String getNombre() {
        return "Ciclo Fijo";
    }
}
