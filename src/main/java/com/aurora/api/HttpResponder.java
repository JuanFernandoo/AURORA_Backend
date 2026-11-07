package com.aurora.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import com.aurora.api.json.JsonWriter;

/**
 * Utilidades compartidas por los handlers HTTP: lectura del cuerpo de la
 * solicitud, cabeceras CORS (necesarias porque el frontend React corre en
 * otro origen/puerto durante el desarrollo, p. ej. http://localhost:5173) y
 * escritura de respuestas JSON con el codigo de estado correcto.
 *
 * Clase nueva y aislada en el paquete {@code com.aurora.api}; no modifica
 * ninguna clase del dominio original de AURORA.
 */
final class HttpResponder {

    private HttpResponder() {
    }

    static String leerCuerpo(HttpExchange exchange) throws IOException {
        try (InputStream entrada = exchange.getRequestBody();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            entrada.transferTo(buffer);
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }

    static void aplicarCabecerasCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    static void responderJson(HttpExchange exchange, int codigoEstado, Object cuerpo) throws IOException {
        byte[] bytes = JsonWriter.write(cuerpo).getBytes(StandardCharsets.UTF_8);
        aplicarCabecerasCors(exchange);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(codigoEstado, bytes.length);
        try (OutputStream salida = exchange.getResponseBody()) {
            salida.write(bytes);
        }
    }

    static void responderError(HttpExchange exchange, int codigoEstado, String mensaje) throws IOException {
        responderJson(exchange, codigoEstado, Map.of("error", mensaje));
    }
}
