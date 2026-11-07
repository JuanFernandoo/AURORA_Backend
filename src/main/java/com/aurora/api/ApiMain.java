package com.aurora.api;

import java.io.IOException;

public final class ApiMain {

    private static final int PUERTO_POR_DEFECTO = 8080;

    public static void main(String[] args) throws IOException, InterruptedException {
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

        Thread.currentThread().join();
    }
    
    private static int resolverPuerto(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0].trim());
            } catch (NumberFormatException ignored) {
            }
        }
        String variableEntorno = System.getenv("AURORA_API_PORT");
        if (variableEntorno != null && !variableEntorno.isBlank()) {
            try {
                return Integer.parseInt(variableEntorno.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return PUERTO_POR_DEFECTO;
    }
}