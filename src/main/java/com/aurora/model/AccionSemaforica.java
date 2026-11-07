package com.aurora.model;

/**
 * Decision tomada por un {@link com.aurora.strategy.ControladorSemaforico}
 * para un ciclo de la simulacion: que fase se habilita con luz verde y
 * durante cuantos segundos.
 *
 * @param fase               fase que recibe luz verde en este ciclo
 * @param duracionSegundos   duracion de la accion, en segundos simulados
 */
public record AccionSemaforica(FaseSemaforica fase, int duracionSegundos) {

    public AccionSemaforica {
        if (duracionSegundos <= 0) {
            throw new IllegalArgumentException(
                "La duracion de la accion semaforica debe ser mayor que cero");
        }
    }
}
