package com.aurora.observer;

import com.aurora.model.ResultadoCiclo;

/**
 * PATRON OBSERVER.
 *
 * Contrato que deben implementar los observadores interesados en conocer el
 * resultado de cada ciclo de la simulacion. El {@link com.aurora.engine.MotorSemaforico}
 * (el "sujeto") notifica a todos los observadores registrados cada vez que
 * completa un ciclo, sin necesidad de conocer que hacen con esa informacion
 * (recolectar metricas, imprimir en consola, en el futuro enviarla a un
 * dashboard web, etc.). Esto permite agregar o quitar observadores sin
 * modificar la logica principal del motor.
 */
public interface ObservadorTrafico {

    /**
     * Invocado por el sujeto cada vez que se completa un ciclo de
     * simulacion.
     *
     * @param resultado resultado consolidado del ciclo recien completado
     */
    void onCicloCompletado(ResultadoCiclo resultado);
}
