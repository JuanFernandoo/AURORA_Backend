package com.aurora.factory;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.strategy.ControladorAdaptativo;
import com.aurora.strategy.ControladorCicloFijo;
import com.aurora.strategy.ControladorSemaforico;

class ControladorFactoryTest {

    private final ConfiguracionAurora configuracion = ConfiguracionAurora.getInstance();

    @Test
    void construyeUnControladorCicloFijoParaElTipoCorrespondiente() {
        ControladorSemaforico controlador = ControladorFactory.crear(TipoControlador.CICLO_FIJO, configuracion);

        assertInstanceOf(ControladorCicloFijo.class, controlador);
    }

    @Test
    void construyeUnControladorAdaptativoParaElTipoCorrespondiente() {
        ControladorSemaforico controlador = ControladorFactory.crear(TipoControlador.ADAPTATIVO, configuracion);

        assertInstanceOf(ControladorAdaptativo.class, controlador);
    }
}
