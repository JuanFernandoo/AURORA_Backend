package com.aurora.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.model.AccionSemaforica;
import com.aurora.model.EstadoTrafico;
import com.aurora.model.FaseSemaforica;

class ControladorAdaptativoTest {

    private final ConfiguracionAurora configuracion = ConfiguracionAurora.getInstance();
    private final ControladorAdaptativo controlador = new ControladorAdaptativo(configuracion);

    @Test
    void priorizaLaDireccionConMayorColaDeVehiculos() {
        EstadoTrafico estadoNorteSurCongestionado = new EstadoTrafico(20, 3);
        AccionSemaforica accion = controlador.decidirAccion(estadoNorteSurCongestionado, 1);

        assertEquals(FaseSemaforica.NORTE_SUR, accion.fase());
    }

    @Test
    void priorizaEsteOesteCuandoTieneMasVehiculosEnEspera() {
        EstadoTrafico estadoEsteOesteCongestionado = new EstadoTrafico(2, 18);
        AccionSemaforica accion = controlador.decidirAccion(estadoEsteOesteCongestionado, 1);

        assertEquals(FaseSemaforica.ESTE_OESTE, accion.fase());
    }

    @Test
    void laDuracionNuncaSuperaElMaximoConfigurado() {
        EstadoTrafico estadoMuyCongestionado = new EstadoTrafico(500, 0);
        AccionSemaforica accion = controlador.decidirAccion(estadoMuyCongestionado, 1);

        assertTrue(accion.duracionSegundos() <= configuracion.getDuracionMaximaAdaptativaSegundos());
    }

    @Test
    void laDuracionNuncaEsMenorAlMinimoConfigurado() {
        EstadoTrafico estadoCasiVacio = new EstadoTrafico(1, 0);
        AccionSemaforica accion = controlador.decidirAccion(estadoCasiVacio, 1);

        assertTrue(accion.duracionSegundos() >= configuracion.getDuracionMinimaAdaptativaSegundos());
    }

    @Test
    void ambasEstrategiasRecibenElMismoEstadoInicialYProducenDecisionesComparables() {
        EstadoTrafico estadoInicial = new EstadoTrafico(12, 8);
        ControladorCicloFijo cicloFijo = new ControladorCicloFijo(configuracion);

        AccionSemaforica accionAdaptativa = controlador.decidirAccion(estadoInicial, 1);
        AccionSemaforica accionCicloFijo = cicloFijo.decidirAccion(estadoInicial, 1);

        // Ambas estrategias deben poder evaluarse sobre el mismo estado y
        // producir una accion valida y comparable (misma clase de objeto,
        // fase valida, duracion positiva), aunque su logica interna difiera.
        assertTrue(accionAdaptativa.duracionSegundos() > 0);
        assertTrue(accionCicloFijo.duracionSegundos() > 0);
    }
}
