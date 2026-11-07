package com.aurora.model;

/**
 * Estado observable del trafico en la interseccion en un instante dado:
 * cantidad de vehiculos en espera por cada direccion.
 *
 * Es un record porque es un dato inmutable que se pasa de un lado a otro
 * (del motor de simulacion al controlador) sin logica de negocio propia,
 * mas alla de un par de metodos de conveniencia de solo lectura.
 *
 * @param vehiculosEsperaNorteSur  vehiculos en espera en el flujo Norte-Sur
 * @param vehiculosEsperaEsteOeste vehiculos en espera en el flujo Este-Oeste
 */
public record EstadoTrafico(int vehiculosEsperaNorteSur, int vehiculosEsperaEsteOeste) {

    public EstadoTrafico {
        if (vehiculosEsperaNorteSur < 0 || vehiculosEsperaEsteOeste < 0) {
            throw new IllegalArgumentException(
                "La cantidad de vehiculos en espera no puede ser negativa");
        }
    }

    /** Vehiculos en espera para la direccion indicada. */
    public int vehiculosEn(FaseSemaforica fase) {
        return fase == FaseSemaforica.NORTE_SUR ? vehiculosEsperaNorteSur : vehiculosEsperaEsteOeste;
    }

    /** Longitud total de cola (suma de ambas direcciones). */
    public int longitudColaTotal() {
        return vehiculosEsperaNorteSur + vehiculosEsperaEsteOeste;
    }

    /** Direccion con mayor congestion actual. En empate, se prioriza Norte-Sur. */
    public FaseSemaforica direccionMasCongestionada() {
        return vehiculosEsperaNorteSur >= vehiculosEsperaEsteOeste
            ? FaseSemaforica.NORTE_SUR
            : FaseSemaforica.ESTE_OESTE;
    }
}
