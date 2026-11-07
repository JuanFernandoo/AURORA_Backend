package com.aurora.config;

/**
 * Configuracion centralizada y de solo lectura de los parametros de la
 * simulacion AURORA (patron SINGLETON).
 *
 * Por que Singleton aqui:
 * Varias clases independientes (ambos controladores, el motor de
 * simulacion, la fabrica) necesitan leer los MISMOS parametros de
 * configuracion (duracion del ciclo fijo, umbral de congestion, numero de
 * ciclos, etc.) durante toda la ejecucion. Inyectar manualmente un objeto de
 * configuracion en cada clase es valido tambien, pero para un prototipo de
 * consola con una unica fuente de verdad de configuracion, un punto de
 * acceso global simplifica el cableado sin introducir estado mutable
 * compartido: la instancia se construye una sola vez y sus valores no
 * cambian durante la ejecucion.
 *
 * Limitaciones reconocidas del patron (no se ocultan):
 * - Introduce estado global accesible desde cualquier punto del programa,
 *   lo que puede dificultar las pruebas unitarias si la configuracion
 *   cambiara en tiempo de ejecucion (aqui no cambia, por eso se tolera).
 * - Acopla a quien lo usa a una implementacion concreta en vez de a una
 *   abstraccion (no hay una interfaz "Configuracion").
 * - Si en el futuro se necesitaran varias configuraciones simultaneas
 *   (por ejemplo, para simular varias intersecciones con parametros
 *   distintos en paralelo), el Singleton clasico se vuelve una limitante y
 *   convendria reemplazarlo por inyeccion de dependencias explicita.
 *
 * Por eso este proyecto NO convierte todas sus clases en singletons: solo
 * la configuracion, que es el unico caso donde el acceso global compartido
 * es realmente una ventaja y no un atajo.
 */
public final class ConfiguracionAurora {

    private static ConfiguracionAurora instancia;

    // --- Parametros del controlador de ciclo fijo ---
    private final int duracionFaseFijaSegundos;

    // --- Parametros del controlador adaptativo (heuristica simulada) ---
    private final int duracionMinimaAdaptativaSegundos;
    private final int duracionMaximaAdaptativaSegundos;
    private final int umbralCongestionVehiculos;

    // --- Parametros de la simulacion determinista ---
    private final int numeroCiclosSimulacion;
    private final int segundosPorVehiculoDescarga;
    private final int llegadaVehiculosNorteSurPorCiclo;
    private final int llegadaVehiculosEsteOestePorCiclo;
    private final int vehiculosInicialesNorteSur;
    private final int vehiculosInicialesEsteOeste;

    private ConfiguracionAurora() {
        this.duracionFaseFijaSegundos = 15;

        this.duracionMinimaAdaptativaSegundos = 10;
        this.duracionMaximaAdaptativaSegundos = 40;
        this.umbralCongestionVehiculos = 8;

        this.numeroCiclosSimulacion = 15;
        this.segundosPorVehiculoDescarga = 2;
        this.llegadaVehiculosNorteSurPorCiclo = 3;
        this.llegadaVehiculosEsteOestePorCiclo = 2;
        this.vehiculosInicialesNorteSur = 12;
        this.vehiculosInicialesEsteOeste = 8;
    }

    /** Acceso controlado a la instancia unica (creacion perezosa, thread-safe por inicializacion estatica). */
    public static synchronized ConfiguracionAurora getInstance() {
        if (instancia == null) {
            instancia = new ConfiguracionAurora();
        }
        return instancia;
    }

    public int getDuracionFaseFijaSegundos() {
        return duracionFaseFijaSegundos;
    }

    public int getDuracionMinimaAdaptativaSegundos() {
        return duracionMinimaAdaptativaSegundos;
    }

    public int getDuracionMaximaAdaptativaSegundos() {
        return duracionMaximaAdaptativaSegundos;
    }

    public int getUmbralCongestionVehiculos() {
        return umbralCongestionVehiculos;
    }

    public int getNumeroCiclosSimulacion() {
        return numeroCiclosSimulacion;
    }

    public int getSegundosPorVehiculoDescarga() {
        return segundosPorVehiculoDescarga;
    }

    public int getLlegadaVehiculosNorteSurPorCiclo() {
        return llegadaVehiculosNorteSurPorCiclo;
    }

    public int getLlegadaVehiculosEsteOestePorCiclo() {
        return llegadaVehiculosEsteOestePorCiclo;
    }

    public int getVehiculosInicialesNorteSur() {
        return vehiculosInicialesNorteSur;
    }

    public int getVehiculosInicialesEsteOeste() {
        return vehiculosInicialesEsteOeste;
    }
}
