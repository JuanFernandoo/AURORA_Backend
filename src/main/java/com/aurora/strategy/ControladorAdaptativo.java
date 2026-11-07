package com.aurora.strategy;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.model.AccionSemaforica;
import com.aurora.model.EstadoTrafico;
import com.aurora.model.FaseSemaforica;

/**
 * Estrategia de control ADAPTATIVO (heuristica simulada).
 *
 * IMPORTANTE (alcance declarado): esta clase NO implementa aprendizaje por
 * refuerzo profundo (DQN) ni ningun modelo entrenado. Es una heuristica
 * explicita y determinista: le da luz verde a la direccion con mayor
 * cantidad de vehiculos en espera, y calcula la duracion de la fase en
 * funcion de esa cola, dentro de limites configurados. El objetivo de esta
 * clase es demostrar, de forma simple y verificable, el PUNTO EXACTO donde
 * la arquitectura dejaria espacio a un algoritmo de decision mas
 * sofisticado.
 *
 * Evolucion futura prevista: esta clase podria sustituirse por un
 * "ControladorDQN" que, implementando la misma interfaz
 * {@link ControladorSemaforico}, delegue la decision a un servicio externo
 * en Python (por ejemplo, vía una llamada HTTP o un proceso que exponga el
 * agente DQN entrenado sobre Eclipse SUMO con la API TraCI). Gracias al
 * patron Strategy, el {@link com.aurora.engine.MotorSemaforico} no
 * necesitaria ningun cambio para usar ese nuevo controlador: bastaria con
 * inyectarlo en lugar de este.
 */
public final class ControladorAdaptativo implements ControladorSemaforico {

    private final int duracionMinima;
    private final int duracionMaxima;
    private final int segundosPorVehiculoDescarga;
    private final int umbralCongestion;

    public ControladorAdaptativo(ConfiguracionAurora configuracion) {
        this.duracionMinima = configuracion.getDuracionMinimaAdaptativaSegundos();
        this.duracionMaxima = configuracion.getDuracionMaximaAdaptativaSegundos();
        this.segundosPorVehiculoDescarga = configuracion.getSegundosPorVehiculoDescarga();
        this.umbralCongestion = configuracion.getUmbralCongestionVehiculos();
    }

    @Override
    public AccionSemaforica decidirAccion(EstadoTrafico estado, int cicloActual) {
        // 1. Se prioriza la direccion con mayor congestion actual.
        FaseSemaforica faseElegida = estado.direccionMasCongestionada();
        int colaDeLaFaseElegida = estado.vehiculosEn(faseElegida);

        // 2. La duracion se calcula para intentar descargar la cola
        //    observada, acotada entre un minimo y un maximo configurados.
        //    Esta es la heuristica: a mayor cola, mayor tiempo de verde,
        //    hasta un tope razonable que evita dejar "muerta" la otra via.
        int duracionEstimada = segundosPorVehiculoDescarga * colaDeLaFaseElegida;
        int duracion = Math.max(duracionMinima, Math.min(duracionMaxima, duracionEstimada));

        // 3. Si ninguna de las dos colas supera el umbral de congestion
        //    configurado, no tiene sentido extender la fase: se usa la
        //    duracion minima para repartir el tiempo de forma mas agil.
        if (estado.longitudColaTotal() > 0 && colaDeLaFaseElegida < umbralCongestion) {
            duracion = duracionMinima;
        }

        return new AccionSemaforica(faseElegida, duracion);
    }

    @Override
    public String getNombre() {
        return "Adaptativo (heuristica)";
    }
}
