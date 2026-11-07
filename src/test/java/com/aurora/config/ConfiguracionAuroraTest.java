package com.aurora.config;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConfiguracionAuroraTest {

    @Test
    void getInstanceDevuelveSiempreLaMismaInstancia() {
        ConfiguracionAurora primera = ConfiguracionAurora.getInstance();
        ConfiguracionAurora segunda = ConfiguracionAurora.getInstance();

        assertSame(primera, segunda, "El Singleton debe devolver siempre la misma instancia");
    }

    @Test
    void losParametrosBasicosSonValoresPositivos() {
        ConfiguracionAurora configuracion = ConfiguracionAurora.getInstance();

        assertTrue(configuracion.getDuracionFaseFijaSegundos() > 0);
        assertTrue(configuracion.getDuracionMinimaAdaptativaSegundos() > 0);
        assertTrue(configuracion.getDuracionMaximaAdaptativaSegundos() >= configuracion.getDuracionMinimaAdaptativaSegundos());
        assertTrue(configuracion.getNumeroCiclosSimulacion() > 0);
    }
}
