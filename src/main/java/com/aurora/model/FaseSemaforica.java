package com.aurora.model;

/**
 * Representa las dos fases semaforicas posibles en la interseccion simulada.
 * En el prototipo solo se modela una interseccion de dos flujos (Norte-Sur y
 * Este-Oeste); un controlador real de ciudad manejaria ademas fases de giro,
 * fases peatonales, etc., pero esas no son necesarias para demostrar los
 * patrones de diseno solicitados.
 */
public enum FaseSemaforica {
    NORTE_SUR,
    ESTE_OESTE;

    /**
     * Devuelve la fase contraria. Se usa, por ejemplo, en el controlador de
     * ciclo fijo para alternar las fases de forma determinista.
     */
    public FaseSemaforica opuesta() {
        return this == NORTE_SUR ? ESTE_OESTE : NORTE_SUR;
    }
}
