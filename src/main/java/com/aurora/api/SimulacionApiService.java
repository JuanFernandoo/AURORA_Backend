package com.aurora.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.engine.MotorSemaforico;
import com.aurora.factory.ControladorFactory;
import com.aurora.factory.TipoControlador;
import com.aurora.model.EstadoTrafico;
import com.aurora.model.ResultadoCiclo;
import com.aurora.observer.ObservadorMetricas;
import com.aurora.strategy.ControladorSemaforico;

/**
 * Capa de orquestacion que el API HTTP usa para ejecutar simulaciones.
 *
 * IMPORTANTE: esta clase NO reimplementa logica de negocio. Unicamente
 * reutiliza, tal cual, las clases ya existentes del backend de consola:
 * {@link ConfiguracionAurora} (Singleton), {@link ControladorFactory}
 * (Factory Simple), {@link MotorSemaforico} (consumidor de Strategy y
 * sujeto de Observer) y {@link ObservadorMetricas} (Observer). La unica
 * responsabilidad nueva aqui es traducir sus resultados a estructuras
 * {@code Map}/{@code List} serializables a JSON para que el frontend las
 * consuma: es exactamente la misma informacion que ya se imprimia en
 * consola a traves de {@code ObservadorConsola}, solo que ahora tambien se
 * expone como datos.
 */
final class SimulacionApiService {

    private final ConfiguracionAurora configuracion;

    SimulacionApiService(ConfiguracionAurora configuracion) {
        this.configuracion = configuracion;
    }

    /** Ejecuta una unica estrategia y devuelve su resultado como estructura serializable. */
    Map<String, Object> ejecutar(TipoControlador tipo, int numeroCiclos) {
        EstadoTrafico estadoInicial = new EstadoTrafico(
            configuracion.getVehiculosInicialesNorteSur(),
            configuracion.getVehiculosInicialesEsteOeste()
        );

        // FACTORY: construccion del controlador solicitado.
        ControladorSemaforico controlador = ControladorFactory.crear(tipo, configuracion);

        // STRATEGY + OBSERVER: el motor recibe el controlador por inyeccion
        // de dependencias y se le registra un observador de metricas real.
        MotorSemaforico motor = new MotorSemaforico(controlador, configuracion);
        ObservadorMetricas metricas = new ObservadorMetricas();
        motor.registrarObservador(metricas);

        List<ResultadoCiclo> historial = motor.ejecutarSimulacion(estadoInicial, numeroCiclos);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("tipoControlador", tipo.name());
        respuesta.put("nombreEstrategia", controlador.getNombre());
        respuesta.put("estadoInicial", Map.of(
            "vehiculosEsperaNorteSur", estadoInicial.vehiculosEsperaNorteSur(),
            "vehiculosEsperaEsteOeste", estadoInicial.vehiculosEsperaEsteOeste()
        ));
        respuesta.put("ciclos", mapearCiclos(historial));
        respuesta.put("metricas", mapearMetricas(metricas));
        return respuesta;
    }

    /** Ejecuta ambas estrategias bajo las mismas condiciones y calcula la diferencia porcentual. */
    Map<String, Object> comparar(int numeroCiclos) {
        Map<String, Object> resultadoCicloFijo = ejecutar(TipoControlador.CICLO_FIJO, numeroCiclos);
        Map<String, Object> resultadoAdaptativo = ejecutar(TipoControlador.ADAPTATIVO, numeroCiclos);

        double esperaCicloFijo = extraerEsperaAcumulada(resultadoCicloFijo);
        double esperaAdaptativo = extraerEsperaAcumulada(resultadoAdaptativo);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("cicloFijo", resultadoCicloFijo);
        respuesta.put("adaptativo", resultadoAdaptativo);

        if (esperaCicloFijo == 0.0) {
            respuesta.put("diferenciaPorcentualEsperaAcumulada", (Object) null);
            respuesta.put("diferenciaCalculable", false);
        } else {
            double diferencia = ((esperaCicloFijo - esperaAdaptativo) / esperaCicloFijo) * 100.0;
            respuesta.put("diferenciaPorcentualEsperaAcumulada", Math.round(diferencia * 10.0) / 10.0);
            respuesta.put("diferenciaCalculable", true);
        }
        return respuesta;
    }

    @SuppressWarnings("unchecked")
    private double extraerEsperaAcumulada(Map<String, Object> resultado) {
        Map<String, Object> metricas = (Map<String, Object>) resultado.get("metricas");
        return ((Number) metricas.get("tiempoEsperaAcumuladoVehSeg")).doubleValue();
    }

    private List<Object> mapearCiclos(List<ResultadoCiclo> historial) {
        List<Object> ciclos = new ArrayList<>();
        for (ResultadoCiclo r : historial) {
            Map<String, Object> cicloDto = new LinkedHashMap<>();
            cicloDto.put("numeroCiclo", r.numeroCiclo());
            cicloDto.put("faseActiva", r.faseActiva().name());
            cicloDto.put("duracionAccionSegundos", r.duracionAccionSegundos());
            cicloDto.put("vehiculosDescargados", r.vehiculosDescargados());
            cicloDto.put("vehiculosEsperaNorteSur", r.vehiculosEsperaNorteSur());
            cicloDto.put("vehiculosEsperaEsteOeste", r.vehiculosEsperaEsteOeste());
            cicloDto.put("longitudColaTotal", r.longitudColaTotal());
            cicloDto.put("tiempoEsperaAcumuladoVehSeg", r.tiempoEsperaAcumuladoVehSeg());
            ciclos.add(cicloDto);
        }
        return ciclos;
    }

    private Map<String, Object> mapearMetricas(ObservadorMetricas metricas) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("ciclosProcesados", metricas.getCiclosProcesados());
        dto.put("longitudColaPromedio", metricas.getLongitudColaPromedio());
        dto.put("longitudColaMaxima", metricas.getLongitudColaMaxima());
        dto.put("tiempoEsperaAcumuladoVehSeg", metricas.getTiempoEsperaAcumuladoFinalVehSeg());
        dto.put("vehiculosPendientesFinal", metricas.getVehiculosPendientesFinal());
        return dto;
    }
}
