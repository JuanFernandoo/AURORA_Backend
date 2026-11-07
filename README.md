# AURORA — Demostración arquitectónica en Java

Prototipo académico en **Java 17 + Maven** que demuestra, mediante una simulación
simplificada y determinista, la arquitectura de software con la que se
organizaría el controlador de semaforización adaptativa del proyecto **AURORA
(Adaptive Urban Road Optimization through Real-time Artificial Intelligence)**.

> **Alcance declarado:** esta entrega es una demostración arquitectónica
> independiente en Java. **No** se conecta a Eclipse SUMO, a la API TraCI ni a
> un modelo de aprendizaje por refuerzo profundo (DQN) real. El controlador
> "adaptativo" implementado aquí es una **heurística determinista** (compara
> colas y asigna tiempo de verde proporcional), no un agente entrenado. El
> propósito es mostrar **dónde y cómo** se integraría ese componente en el
> futuro, con una arquitectura que ya está preparada para recibirlo (ver
> `ControladorSemaforico` en el paquete `strategy`).

> **Actualización — API HTTP para el frontend:** este backend ahora tiene un
> segundo punto de entrada, `com.aurora.api.ApiMain`, que expone la misma
> lógica de dominio (sin duplicarla) como una API HTTP REST, para que la
> consuma el proyecto `../aurora-frontend` (React). El punto de entrada
> original, `com.aurora.Main` (la demo de consola), sigue intacto y sin
> cambios. Ver la sección 8 más abajo y `../TRAZABILIDAD.md` para el detalle
> completo de los endpoints y su verificación.

---

## 1. Patrones de diseño aplicados

| Patrón | Dónde | Propósito |
|---|---|---|
| **Strategy** | `strategy/ControladorSemaforico` + `ControladorCicloFijo` + `ControladorAdaptativo` | Intercambiar el algoritmo de decisión semafórica en tiempo de ejecución |
| **Singleton** | `config/ConfiguracionAurora` | Punto único de acceso a los parámetros de simulación |
| **Observer** | `observer/ObservadorTrafico` + `ObservadorMetricas` + `ObservadorConsola` | Publicar los resultados de cada ciclo sin acoplar el motor a quién los consume |
| **Factory Simple** | `factory/ControladorFactory` + `TipoControlador` | Centralizar la construcción de controladores, evitando condicionales dispersos |

---

## 2. Requisitos previos

- **JDK 17 o superior** (el proyecto se probó con JDK 21, retro-compatible vía `maven.compiler.release=17`)
- **Apache Maven 3.8+**
- **Visual Studio Code** con la extensión **Extension Pack for Java** (de Microsoft, `vscjava.vscode-java-pack`)

### Verificar la instalación desde la terminal integrada de VS Code

Abre la terminal integrada (`` Ctrl+` `` en Windows/Linux, `` Cmd+` `` en macOS) y ejecuta:

```bash
java -version
mvn -version
```

Debes ver Java 17+ y una versión de Maven 3.8 o superior. Si alguno de los dos
comandos no se reconoce, instala el JDK (por ejemplo, Eclipse Temurin 17) y
Apache Maven antes de continuar.

---

## 3. Abrir el proyecto en VS Code

1. Descomprime el proyecto en una carpeta local, por ejemplo `aurora-java/`.
2. Abre VS Code → **File → Open Folder…** → selecciona la carpeta `aurora-java`.
3. Si no la tienes instalada, VS Code sugerirá instalar el **Extension Pack
   for Java** (incluye soporte de depuración, ejecución de pruebas, Maven y
   el servidor de lenguaje de Java). Acepta la instalación.
4. Espera a que la extensión termine de indexar el proyecto (barra de estado
   inferior).

---

## 4. Compilar, probar y ejecutar

Todos los comandos se ejecutan desde la **terminal integrada de VS Code**,
ubicada en la raíz del proyecto (`aurora-java/`).

### Compilar

```bash
mvn compile
```

### Ejecutar las pruebas unitarias (JUnit 5)

```bash
mvn test
```

### Ejecutar la aplicación

```bash
mvn exec:java
```

Alternativamente, empaquetar y ejecutar el `.jar`:

```bash
mvn package
java -jar target/aurora-java.jar
```

> **Nota sobre la primera ejecución:** la primera vez que ejecutes cualquier
> comando `mvn`, Maven descargará automáticamente desde Maven Central los
> plugins y la dependencia de pruebas (JUnit 5 Jupiter). Esto requiere
> conexión a internet únicamente la primera vez; las siguientes ejecuciones
> usan la caché local (`~/.m2/repository`).

---

## 5. Salida de consola esperada (resumen)

```
============================================================
 AURORA - Demostracion arquitectonica en Java
============================================================
Estado inicial identico para ambas estrategias: Norte-Sur=12, Este-Oeste=8 | Ciclos a simular: 15

=== Simulando estrategia: Ciclo Fijo ===
[Ciclo Fijo] Ciclo  1 | Fase: NORTE_SUR   (15s) | Descargados:  7 | Espera NS= 8 EO=10 (total=18) | Acum. espera=270.0 veh-s
...
=== Simulando estrategia: Adaptativo (heuristica) ===
[Adaptativo (heuristica)] Ciclo  1 | Fase: NORTE_SUR   (24s) | Descargados: 12 | Espera NS= 3 EO=10 (total=13) | Acum. espera=312.0 veh-s
...
=== Comparacion final ===
Metrica                | Ciclo Fijo           | Adaptativo (heuristica)
----------------------------------------------------------------------
Espera acum. (veh-s)   | 2310.0               | 1632.0
Cola promedio (veh)    | 10.27                | 9.13
Cola maxima (veh)      | 18                   | 13
Pendientes al cierre   | 7                    | 9

Diferencia porcentual de Tiempo de espera acumulado: Adaptativo (heuristica)
redujo el valor de Tiempo de espera acumulado en un 29.4% respecto a Ciclo Fijo.
```

Esta salida es **real y reproducible**: ejecutar el programas las veces que
sean necesarias produce siempre los mismos números, porque no se usa ningún
generador aleatorio (los arribos de vehículos por ciclo son constantes
definidas en `ConfiguracionAurora`).

---

## 6. Estructura del proyecto

```
aurora-java/
  pom.xml
  README.md
  src/
    main/java/com/aurora/
      Main.java                          # Punto de entrada (sin lógica de negocio)
      config/
        ConfiguracionAurora.java         # SINGLETON
      model/
        EstadoTrafico.java
        FaseSemaforica.java
        AccionSemaforica.java
        ResultadoCiclo.java
      strategy/
        ControladorSemaforico.java       # STRATEGY (interfaz)
        ControladorCicloFijo.java        # STRATEGY (implementación 1)
        ControladorAdaptativo.java       # STRATEGY (implementación 2, heurística)
      factory/
        TipoControlador.java
        ControladorFactory.java          # FACTORY SIMPLE
      observer/
        ObservadorTrafico.java           # OBSERVER (interfaz)
        ObservadorMetricas.java          # OBSERVER (recolecta métricas)
        ObservadorConsola.java           # OBSERVER (imprime en consola)
      engine/
        MotorSemaforico.java             # Orquesta Strategy + Observer
      simulation/
        SimulacionComparativa.java       # Ejecuta y compara ambas estrategias
        ResultadoComparacion.java
    test/java/com/aurora/
      config/ConfiguracionAuroraTest.java
      strategy/ControladorCicloFijoTest.java
      strategy/ControladorAdaptativoTest.java
      factory/ControladorFactoryTest.java
      observer/ObservadorMetricasTest.java
      engine/MotorSemaforicoTest.java
```

---

## 7. Unidades y supuestos de las métricas (importante para la sustentación)

- **Vehículos en espera**: conteo entero de vehículos detenidos en cada
  dirección al cierre de cada ciclo.
- **Tiempo de espera acumulado**: expresado en **vehículo-segundos**
  (`Σ cola_total_ciclo × duración_ciclo`). Es una aproximación simplificada y
  determinista — NO es una medición real de tránsito ni el resultado de un
  modelo entrenado.
- **Llegadas de vehículos**: constantes fijas por ciclo (no aleatorias), para
  que la demostración sea reproducible, tal como exige el enunciado.
- **Los resultados sintéticos no representan datos reales de Bogotá.** El
  prototipo no garantiza que la estrategia adaptativa siempre sea mejor: en
  esta ejecución concreta, con este escenario y esta heurística, el
  controlador adaptativo redujo el tiempo de espera acumulado en ~29%, pero
  ese resultado depende de los parámetros configurados en
  `ConfiguracionAurora` y podría no sostenerse con otro escenario inicial.

---

## 8. API HTTP para el frontend (`com.aurora.api`)

Paquete nuevo y autocontenido que **envuelve** las clases de dominio ya
existentes (no las modifica ni duplica su lógica) y las expone como JSON
sobre HTTP, usando únicamente `com.sun.net.httpserver` (incluido en el JDK)
— **no se agregó ninguna dependencia nueva al `pom.xml`**.

### Arrancar la API

```bash
mvn compile
mvn exec:java -Dexec.mainClass=com.aurora.api.ApiMain
# o, en un puerto distinto al 8080 por defecto:
AURORA_API_PORT=8099 mvn exec:java -Dexec.mainClass=com.aurora.api.ApiMain
```

### Endpoints (verificados con `curl` — ver `../TRAZABILIDAD.md` para la evidencia completa)

| Método | Ruta | Body | Qué hace |
|---|---|---|---|
| GET | `/api/health` | — | Estado del servicio |
| GET | `/api/config` | — | Parámetros reales de `ConfiguracionAurora` (Singleton) |
| GET | `/api/controladores` | — | Tipos de `TipoControlador` que acepta el Factory |
| POST | `/api/simulaciones` | `{"tipoControlador":"CICLO_FIJO"\|"ADAPTATIVO","numeroCiclos"?:n}` | Construye el controlador (Factory), lo inyecta en `MotorSemaforico` (Strategy), registra un `ObservadorMetricas` real (Observer) y devuelve el historial de ciclos + métricas |
| POST | `/api/simulaciones/comparar` | `{"numeroCiclos"?:n}` | Ejecuta ambas estrategias bajo las mismas condiciones y devuelve la diferencia porcentual, calculada en el backend |

El consumidor real de esta API es el proyecto `../aurora-frontend` (React +
TypeScript + Vite). `com.aurora.Main` (la demo de consola) sigue siendo un
punto de entrada independiente y no se ve afectado por este paquete.
