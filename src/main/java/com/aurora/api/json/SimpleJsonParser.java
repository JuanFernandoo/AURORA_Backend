package com.aurora.api.json;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parser JSON deliberadamente minimo: solo entiende objetos JSON PLANOS de
 * un solo nivel, con valores de tipo string, numero o booleano (por ejemplo,
 * {@code {"tipoControlador":"ADAPTATIVO","numeroCiclos":15}}).
 *
 * Es suficiente para los cuerpos de solicitud reales de esta API (ver
 * {@code POST /api/simulaciones} y {@code POST /api/simulaciones/comparar}
 * en {@link com.aurora.api.AuroraApiServer}), que no necesitan objetos
 * anidados ni arreglos. NO es un parser JSON de proposito general: si se
 * necesitara aceptar estructuras mas complejas en el futuro, se deberia
 * reemplazar por una libreria JSON real (por ejemplo, agregando Jackson al
 * {@code pom.xml}), lo cual queda fuera del alcance de este cambio minimo.
 */
public final class SimpleJsonParser {

    private SimpleJsonParser() {
        // Clase de utilidad: no se instancia.
    }

    /**
     * Parsea un objeto JSON plano a un mapa de String a String. Los valores
     * numericos y booleanos se devuelven tambien como su representacion en
     * texto; el llamador es responsable de convertirlos al tipo que
     * necesite (ver {@code DatoRequestUtil} en los handlers).
     *
     * @param json texto JSON de entrada; si es nulo o vacio, se devuelve un mapa vacio
     */
    public static Map<String, String> parseObjetoPlano(String json) {
        Map<String, String> resultado = new LinkedHashMap<>();
        if (json == null || json.isBlank()) {
            return resultado;
        }

        String contenido = json.trim();
        if (contenido.startsWith("{") && contenido.endsWith("}")) {
            contenido = contenido.substring(1, contenido.length() - 1);
        }
        if (contenido.isBlank()) {
            return resultado;
        }

        for (String parPlano : dividirPorComasDeNivelSuperior(contenido)) {
            int indiceDosPuntos = indiceDosPuntosDeNivelSuperior(parPlano);
            if (indiceDosPuntos < 0) {
                continue;
            }
            String clave = quitarComillas(parPlano.substring(0, indiceDosPuntos).trim());
            String valor = quitarComillas(parPlano.substring(indiceDosPuntos + 1).trim());
            resultado.put(clave, valor);
        }
        return resultado;
    }

    private static String quitarComillas(String texto) {
        if (texto.length() >= 2 && texto.startsWith("\"") && texto.endsWith("\"")) {
            return texto.substring(1, texto.length() - 1);
        }
        return texto;
    }

    private static int indiceDosPuntosDeNivelSuperior(String texto) {
        boolean dentroDeString = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '"') {
                dentroDeString = !dentroDeString;
            } else if (c == ':' && !dentroDeString) {
                return i;
            }
        }
        return -1;
    }

    private static java.util.List<String> dividirPorComasDeNivelSuperior(String texto) {
        java.util.List<String> partes = new java.util.ArrayList<>();
        boolean dentroDeString = false;
        int inicio = 0;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '"') {
                dentroDeString = !dentroDeString;
            } else if (c == ',' && !dentroDeString) {
                partes.add(texto.substring(inicio, i));
                inicio = i + 1;
            }
        }
        partes.add(texto.substring(inicio));
        return partes;
    }
}
