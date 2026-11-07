package com.aurora.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.aurora.config.ConfiguracionAurora;
import com.aurora.factory.TipoControlador;
import com.aurora.api.json.SimpleJsonParser;

/**
 * API HTTP REST de AURORA.
 *
 * Este es el UNICO cambio minimo propuesto para exponer el backend de
 * consola existente a un frontend web: un paquete nuevo y autocontenido
 * ({@code com.aurora.api}) que envuelve las clases de dominio ya
 * implementadas (Strategy, Singleton, Observer, Factory) sin modificarlas
 * ni duplicar su logica. Se construye sobre {@code com.sun.net.httpserver}
 * (incluido en el JDK) deliberadamente, para no agregar NINGUNA dependencia
 * nueva al {@code pom.xml} del proyecto original.
 *
 * Endpoints expuestos:
 * <ul>
 *   <li>GET  /api/health                     -&gt; estado del servicio</li>
 *   <li>GET  /api/config                     -&gt; parametros de {@link ConfiguracionAurora} (Singleton)</li>
 *   <li>GET  /api/controladores              -&gt; tipos de controlador disponibles (para el Factory)</li>
 *   <li>POST /api/simulaciones               -&gt; ejecuta UNA estrategia. Body: {"tipoControlador":"CICLO_FIJO"|"ADAPTATIVO","numeroCiclos"?:number}</li>
 *   <li>POST /api/simulaciones/comparar      -&gt; ejecuta AMBAS estrategias bajo las mismas condiciones. Body: {"numeroCiclos"?:number}</li>
 * </ul>
 *
 * Todas las respuestas son JSON reales, construidas a partir de la
 * ejecucion real de {@link com.aurora.engine.MotorSemaforico}; no hay datos
 * simulados en el servidor (el "modo DEMO" vive exclusivamente del lado del
 * frontend, para cuando este backend no esta disponible).
 */
public final class AuroraApiServer {

    private final HttpServer servidor;
    private final SimulacionApiService simulacionApiService;
    private final ConfiguracionAurora configuracion;

    public AuroraApiServer(int puerto) throws IOException {
        this.configuracion = ConfiguracionAurora.getInstance();
        this.simulacionApiService = new SimulacionApiService(configuracion);
        this.servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        this.servidor.setExecutor(Executors.newFixedThreadPool(4));
        registrarRutas();
    }

    private void registrarRutas() {
        servidor.createContext("/api/health", this::manejarHealth);
        servidor.createContext("/api/config", this::manejarConfig);
        servidor.createContext("/api/controladores", this::manejarControladores);
        servidor.createContext("/api/simulaciones/comparar", this::manejarComparar);
        // Nota de enrutamiento: com.sun.net.httpserver hace match por
        // PREFIJO de path, por lo que "/api/simulaciones/comparar" debe
        // registrarse ANTES de "/api/simulaciones" para que no sea
        // interceptado por el handler mas generico.
        servidor.createContext("/api/simulaciones", this::manejarSimulaciones);
    }

    public void start() {
        servidor.start();
        System.out.println("AURORA API escuchando en http://localhost:" + servidor.getAddress().getPort());
    }

    public void stop() {
        servidor.stop(0);
    }

    private void manejarHealth(HttpExchange exchange) throws IOException {
        if (manejarPreflight(exchange)) return;
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponder.responderError(exchange, 405, "Metodo no permitido. Use GET.");
            return;
        }
        HttpResponder.responderJson(exchange, 200, Map.of(
            "status", "ok",
            "servicio", "aurora-java-api",
            "version", "1.0.0"
        ));
    }

    private void manejarConfig(HttpExchange exchange) throws IOException {
        if (manejarPreflight(exchange)) return;
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponder.responderError(exchange, 405, "Metodo no permitido. Use GET.");
            return;
        }
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("duracionFaseFijaSegundos", configuracion.getDuracionFaseFijaSegundos());
        cuerpo.put("duracionMinimaAdaptativaSegundos", configuracion.getDuracionMinimaAdaptativaSegundos());
        cuerpo.put("duracionMaximaAdaptativaSegundos", configuracion.getDuracionMaximaAdaptativaSegundos());
        cuerpo.put("umbralCongestionVehiculos", configuracion.getUmbralCongestionVehiculos());
        cuerpo.put("numeroCiclosSimulacion", configuracion.getNumeroCiclosSimulacion());
        cuerpo.put("segundosPorVehiculoDescarga", configuracion.getSegundosPorVehiculoDescarga());
        cuerpo.put("llegadaVehiculosNorteSurPorCiclo", configuracion.getLlegadaVehiculosNorteSurPorCiclo());
        cuerpo.put("llegadaVehiculosEsteOestePorCiclo", configuracion.getLlegadaVehiculosEsteOestePorCiclo());
        cuerpo.put("vehiculosInicialesNorteSur", configuracion.getVehiculosInicialesNorteSur());
        cuerpo.put("vehiculosInicialesEsteOeste", configuracion.getVehiculosInicialesEsteOeste());
        HttpResponder.responderJson(exchange, 200, cuerpo);
    }

    private void manejarControladores(HttpExchange exchange) throws IOException {
        if (manejarPreflight(exchange)) return;
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponder.responderError(exchange, 405, "Metodo no permitido. Use GET.");
            return;
        }
        List<String> tipos = List.of(TipoControlador.CICLO_FIJO.name(), TipoControlador.ADAPTATIVO.name());
        HttpResponder.responderJson(exchange, 200, Map.of("tipos", tipos));
    }

    private void manejarSimulaciones(HttpExchange exchange) throws IOException {
        if (manejarPreflight(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponder.responderError(exchange, 405, "Metodo no permitido. Use POST.");
            return;
        }

        Map<String, String> body = SimpleJsonParser.parseObjetoPlano(HttpResponder.leerCuerpo(exchange));
        String tipoTexto = body.get("tipoControlador");
        if (tipoTexto == null || tipoTexto.isBlank()) {
            HttpResponder.responderError(exchange, 400,
                "El campo \"tipoControlador\" es obligatorio (CICLO_FIJO o ADAPTATIVO).");
            return;
        }

        TipoControlador tipo;
        try {
            tipo = TipoControlador.valueOf(tipoTexto.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            HttpResponder.responderError(exchange, 400,
                "tipoControlador invalido: \"" + tipoTexto + "\". Valores validos: CICLO_FIJO, ADAPTATIVO.");
            return;
        }

        int numeroCiclos = leerNumeroCiclos(body);

        try {
            Map<String, Object> resultado = simulacionApiService.ejecutar(tipo, numeroCiclos);
            HttpResponder.responderJson(exchange, 200, resultado);
        } catch (IllegalArgumentException ex) {
            HttpResponder.responderError(exchange, 400, ex.getMessage());
        }
    }

    private void manejarComparar(HttpExchange exchange) throws IOException {
        if (manejarPreflight(exchange)) return;
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponder.responderError(exchange, 405, "Metodo no permitido. Use POST.");
            return;
        }

        Map<String, String> body = SimpleJsonParser.parseObjetoPlano(HttpResponder.leerCuerpo(exchange));
        int numeroCiclos = leerNumeroCiclos(body);

        try {
            Map<String, Object> resultado = simulacionApiService.comparar(numeroCiclos);
            HttpResponder.responderJson(exchange, 200, resultado);
        } catch (IllegalArgumentException ex) {
            HttpResponder.responderError(exchange, 400, ex.getMessage());
        }
    }

    private int leerNumeroCiclos(Map<String, String> body) {
        String texto = body.get("numeroCiclos");
        if (texto == null || texto.isBlank()) {
            return configuracion.getNumeroCiclosSimulacion();
        }
        try {
            int valor = Integer.parseInt(texto.trim());
            return valor > 0 ? valor : configuracion.getNumeroCiclosSimulacion();
        } catch (NumberFormatException ex) {
            return configuracion.getNumeroCiclosSimulacion();
        }
    }

    /** Responde a las solicitudes de pre-vuelo CORS (OPTIONS) que el navegador envia antes de un POST con JSON. */
    private boolean manejarPreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponder.aplicarCabecerasCors(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }
}
