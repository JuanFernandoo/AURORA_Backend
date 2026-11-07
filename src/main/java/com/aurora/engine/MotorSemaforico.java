package com.aurora.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.model.AccionSemaforica;
import com.aurora.model.EstadoTrafico;
import com.aurora.model.FaseSemaforica;
import com.aurora.model.ResultadoCiclo;
import com.aurora.observer.ObservadorTrafico;
import com.aurora.strategy.ControladorSemaforico;

/**
 * Motor de la simulacion. Es el "cliente" del patron Strategy (recibe el
 * {@link ControladorSemaforico} por inyeccion de dependencias y puede
 * cambiarlo en tiempo de ejecucion sin conocer su implementacion concreta)
 * y tambien el "sujeto" del patron Observer (mantiene la lista de
 * observadores y los notifica al cierre de cada ciclo).
 *
 * Por cada ciclo de simulacion ejecuta, en orden, los siete pasos pedidos:
 * 1. Lee el estado del trafico.
 * 2. Pide al controlador una accion.
 * 3. Aplica la accion (descarga de vehiculos en la fase activa).
 * 4. Actualiza los vehiculos pendientes con llegadas deterministas.
 * 5. Calcula las metricas del ciclo.
 * 6. Notifica a los observadores.
 * 7. (La impresion de resultados queda a cargo de los observadores, p. ej.
 *     {@code ObservadorConsola}, para no mezclar logica de negocio con
 *     presentacion dentro del motor).
 *
 * El motor NO usa numeros aleatorios: las llegadas de vehiculos por ciclo
 * son constantes definidas en {@link ConfiguracionAurora}, por lo que, dado
 * el mismo estado inicial y la misma estrategia, la simulacion siempre
 * produce el mismo resultado (reproducibilidad exigida por el enunciado).
 */
public final class MotorSemaforico {

    private final ConfiguracionAurora configuracion;
    private final List<ObservadorTrafico> observadores = new ArrayList<>();
    private ControladorSemaforico controlador;

    public MotorSemaforico(ControladorSemaforico controladorInicial, ConfiguracionAurora configuracion) {
        this.controlador = Objects.requireNonNull(controladorInicial, "El controlador no puede ser nulo");
        this.configuracion = Objects.requireNonNull(configuracion, "La configuracion no puede ser nula");
    }

    /**
     * Permite cambiar de estrategia en tiempo de ejecucion sin modificar la
     * logica interna del motor: es la demostracion directa del patron
     * Strategy.
     */
    public void setControlador(ControladorSemaforico nuevoControlador) {
        this.controlador = Objects.requireNonNull(nuevoControlador, "El controlador no puede ser nulo");
    }

    public ControladorSemaforico getControlador() {
        return controlador;
    }

    /** Registra un observador que sera notificado al final de cada ciclo. */
    public void registrarObservador(ObservadorTrafico observador) {
        observadores.add(Objects.requireNonNull(observador, "El observador no puede ser nulo"));
    }

    /**
     * Ejecuta la simulacion completa a partir de un estado inicial dado,
     * durante el numero de ciclos indicado, y devuelve la lista de
     * resultados (uno por ciclo), en orden.
     */
    public List<ResultadoCiclo> ejecutarSimulacion(EstadoTrafico estadoInicial, int numeroCiclos) {
        List<ResultadoCiclo> historial = new ArrayList<>();

        EstadoTrafico estadoActual = estadoInicial;
        double tiempoEsperaAcumulado = 0.0;

        for (int ciclo = 1; ciclo <= numeroCiclos; ciclo++) {
            // Paso 1: leer el estado del trafico (estadoActual, ya disponible).
            // Paso 2: pedir al controlador una accion.
            AccionSemaforica accion = controlador.decidirAccion(estadoActual, ciclo);

            // Paso 3: aplicar la accion a la simulacion (descarga de vehiculos
            // en la fase que recibio luz verde, a una tasa fija y determinista).
            int colaEnFaseActiva = estadoActual.vehiculosEn(accion.fase());
            int capacidadDescarga = accion.duracionSegundos() / configuracion.getSegundosPorVehiculoDescarga();
            int vehiculosDescargados = Math.min(colaEnFaseActiva, capacidadDescarga);

            int nuevaColaNorteSur = estadoActual.vehiculosEsperaNorteSur();
            int nuevaColaEsteOeste = estadoActual.vehiculosEsperaEsteOeste();
            if (accion.fase() == FaseSemaforica.NORTE_SUR) {
                nuevaColaNorteSur -= vehiculosDescargados;
            } else {
                nuevaColaEsteOeste -= vehiculosDescargados;
            }

            // Paso 4: actualizar los vehiculos pendientes mediante reglas
            // deterministas sencillas (llegadas fijas por ciclo, sin azar).
            nuevaColaNorteSur += configuracion.getLlegadaVehiculosNorteSurPorCiclo();
            nuevaColaEsteOeste += configuracion.getLlegadaVehiculosEsteOestePorCiclo();

            EstadoTrafico estadoSiguiente = new EstadoTrafico(nuevaColaNorteSur, nuevaColaEsteOeste);

            // Paso 5: calcular las metricas del ciclo. Se aproxima el tiempo
            // de espera del ciclo como "cola resultante x duracion de la
            // accion" (vehiculo-segundos), y se acumula.
            double tiempoEsperaCiclo = estadoSiguiente.longitudColaTotal() * (double) accion.duracionSegundos();
            tiempoEsperaAcumulado += tiempoEsperaCiclo;

            ResultadoCiclo resultado = new ResultadoCiclo(
                ciclo,
                accion.fase(),
                accion.duracionSegundos(),
                vehiculosDescargados,
                estadoSiguiente.vehiculosEsperaNorteSur(),
                estadoSiguiente.vehiculosEsperaEsteOeste(),
                tiempoEsperaAcumulado
            );

            // Paso 6: notificar a los observadores registrados.
            for (ObservadorTrafico observador : observadores) {
                observador.onCicloCompletado(resultado);
            }

            historial.add(resultado);
            estadoActual = estadoSiguiente;
        }

        return historial;
    }
}
