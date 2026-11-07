package com.aurora.api.json;

import java.util.List;
import java.util.Map;

/**
 * Serializador JSON minimalista, escrito a mano a proposito para NO agregar
 * ninguna dependencia externa (Jackson, Gson, etc.) al backend existente.
 *
 * Este es uno de los "cambios minimos necesarios en Java" para exponer una
 * API HTTP: no sustituye ni modifica la arquitectura de dominio de AURORA
 * (Strategy/Singleton/Observer/Factory siguen intactos en sus paquetes
 * originales); es una capa de presentacion nueva y aislada en el paquete
 * {@code com.aurora.api}.
 *
 * Soporta los tipos que realmente se usan en las respuestas de esta API:
 * {@link Map} (objeto JSON, se recorre en el orden de insercion si es un
 * {@link java.util.LinkedHashMap}), {@link List} (arreglo JSON), String,
 * Number, Boolean y null. No pretende ser un serializador JSON de proposito
 * general.
 */
public final class JsonWriter {

    private JsonWriter() {
        // Clase de utilidad: no se instancia.
    }

    public static String write(Object valor) {
        StringBuilder sb = new StringBuilder();
        escribirValor(valor, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void escribirValor(Object valor, StringBuilder sb) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof Map<?, ?> mapa) {
            escribirObjeto((Map<String, Object>) mapa, sb);
        } else if (valor instanceof List<?> lista) {
            escribirArreglo(lista, sb);
        } else if (valor instanceof String texto) {
            escribirString(texto, sb);
        } else if (valor instanceof Enum<?> enumValor) {
            escribirString(enumValor.name(), sb);
        } else if (valor instanceof Number || valor instanceof Boolean) {
            sb.append(valor);
        } else {
            // Fallback seguro: cualquier otro tipo se serializa como texto.
            escribirString(String.valueOf(valor), sb);
        }
    }

    private static void escribirObjeto(Map<String, Object> mapa, StringBuilder sb) {
        sb.append('{');
        boolean primero = true;
        for (Map.Entry<String, Object> entrada : mapa.entrySet()) {
            if (!primero) {
                sb.append(',');
            }
            primero = false;
            escribirString(entrada.getKey(), sb);
            sb.append(':');
            escribirValor(entrada.getValue(), sb);
        }
        sb.append('}');
    }

    private static void escribirArreglo(List<?> lista, StringBuilder sb) {
        sb.append('[');
        boolean primero = true;
        for (Object elemento : lista) {
            if (!primero) {
                sb.append(',');
            }
            primero = false;
            escribirValor(elemento, sb);
        }
        sb.append(']');
    }

    private static void escribirString(String texto, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }
}
