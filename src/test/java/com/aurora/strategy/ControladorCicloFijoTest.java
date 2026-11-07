package com.aurora.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.model.AccionSemaforica;
import com.aurora.model.EstadoTrafico;
import com.aurora.model.FaseSemaforica;

class ControladorCicloFijoTest {

    private final ConfiguracionAurora configuracion = ConfiguracionAurora.getInstance();
    private final ControladorCicloFijo controlador = new ControladorCicloFijo(configuracion);

    @Test
    void alternaLaFaseEntreCiclosImparesYPares() {
        EstadoTrafico estado = new EstadoTrafico(5, 5);

        AccionSemaforica accionCiclo1 = controlador.decidirAccion(estado, 1);
        AccionSemaforica accionCiclo2 = controlador.decidirAccion(estado, 2);
        AccionSemaforica accionCiclo3 = controlador.decidirAccion(estado, 3);

        assertEquals(FaseSemaforica.NORTE_SUR, accionCiclo1.fase());
        assertEquals(FaseSemaforica.ESTE_OESTE, accionCiclo2.fase());
        assertEquals(FaseSemaforica.NORTE_SUR, accionCiclo3.fase());
    }

    @Test
    void ignoraElEstadoDelTraficoYUsaSiempreLaDuracionConfigurada() {
        EstadoTrafico estadoCongestionado = new EstadoTrafico(50, 1);
        EstadoTrafico estadoDescongestionado = new EstadoTrafico(1, 1);

        AccionSemaforica accionConCongestion = controlador.decidirAccion(estadoCongestionado, 1);
        AccionSemaforica accionSinCongestion = controlador.decidirAccion(estadoDescongestionado, 1);

        assertEquals(configuracion.getDuracionFaseFijaSegundos(), accionConCongestion.duracionSegundos());
        assertEquals(accionConCongestion.duracionSegundos(), accionSinCongestion.duracionSegundos(),
            "El ciclo fijo debe dar la misma duracion sin importar el estado del trafico");
    }
}
