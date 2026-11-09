package com.aurora.api;

import java.io.IOException;

/**
 * Punto de entrada DEDICADO para levantar la API HTTP de AURORA.
 *
 * Este archivo es el unico "cambio minimo necesario en Java" que agrega
 * este cambio al backend ya existente: antes de esta clase, el paquete
 * {@code com.aurora.api} (servidor, servicio de orquestacion, lector/escritor
 * JSON) ya estaba completamente implementado en el codigo fuente, pero
 * NINGUN punto de entrada lo arrancaba — ni {@link com.aurora.Main}
 * (que sigue, intacto, ejecutando solo la demo de consola) ni el
 * {@code pom.xml} (que solo declara {@code com.aurora.Main} como clase
 * principal). Sin esta clase, la API nunca se pone a escuchar y no hay
 * nada que un frontend pueda consumir.
 *
 * Deliberadamente NO se modifico {@link com.aurora.Main} ni el
 * comportamiento de la demo de consola: se agrega este segundo punto de
 * entrada, independiente, para no alterar el comportamiento ya validado
 * (y usado en la sustentacion) del programa original.
 *
 * Uso:
 *   mvn compile exec:java -Dexec.mainClass=com.aurora.api.ApiMain
 *   (o, tras `mvn package`): java -cp target/classes com.aurora.api.ApiMain
 *
 * El puerto por defecto es 8080; se puede sobreescribir con el primer argumento,
 * la variable AURORA_API_PORT o la variable PORT (hosting en la nube).
 */
public final class ApiMain {

    private static final int PUERTO_POR_DEFECTO = 8080;

    public static void main(String[] args) throws IOException {
        int puerto = resolverPuerto(args);

        AuroraApiServer servidor = new AuroraApiServer(puerto);
        servidor.start();

        System.out.println("============================================================");
        System.out.println(" AURORA API - backend HTTP para el frontend React");
        System.out.println("============================================================");
        System.out.println(" Endpoints disponibles:");
        System.out.println("   GET  /api/health");
        System.out.println("   GET  /api/config");
        System.out.println("   GET  /api/controladores");
        System.out.println("   POST /api/simulaciones");
        System.out.println("   POST /api/simulaciones/comparar");
        System.out.println("============================================================");
        System.out.println(" Presiona Ctrl+C para detener el servidor.");

        // El HttpServer usa un pool de hilos NO daemon (Executors.newFixedThreadPool),
        // por lo que la JVM permanece viva aunque main() retorne. No se necesita
        // un bucle de espera adicional.
    }

    private static int resolverPuerto(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0].trim());
            } catch (NumberFormatException ignored) {
                // Cae al siguiente intento (variable de entorno / valor por defecto).
            }
        }
        // AURORA_API_PORT tiene prioridad; PORT es la variable que asignan
        // plataformas de hosting como Render, Railway o Fly.io.
        for (String nombre : new String[] {"AURORA_API_PORT", "PORT"}) {
            String variableEntorno = System.getenv(nombre);
            if (variableEntorno != null && !variableEntorno.isBlank()) {
                try {
                    return Integer.parseInt(variableEntorno.trim());
                } catch (NumberFormatException ignored) {
                    // Prueba la siguiente variable o usa el valor por defecto.
                }
            }
        }
        return PUERTO_POR_DEFECTO;
    }
}