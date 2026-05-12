# Calculadora Distribuida — Evolución de los Sistemas Distribuidos

Proyecto educativo que muestra, con una **calculadora básica** (sumar, restar, multiplicar, dividir) como hilo conductor, cómo han evolucionado los mecanismos de comunicación entre procesos distribuidos.

Cada carpeta es una etapa autocontenida con su propio README, código y motivación. Las etapas avanzan respondiendo a las limitaciones de la anterior. **No se usa Maven, Gradle ni ningún build system**: todo se compila con `javac` y `java`. Las dependencias externas (Jackson) se colocan como JARs en una carpeta `libs/`.

## Etapas

| # | Carpeta | Tecnología | Qué resuelve | Qué deja pendiente |
|---|---|---|---|---|
| 1 | [`01-sockets-basico`](./01-sockets-basico) | Sockets TCP en Java, single-thread | Comunicación cliente-servidor mínima | Atiende un cliente a la vez; protocolo de texto frágil |
| 2 | [`02-sockets-multihilo`](./02-sockets-multihilo) | Sockets TCP + `ExecutorService` | Concurrencia: N clientes en paralelo | Sigue siendo un protocolo de texto ad-hoc |
| 3 | [`03-sockets-jackson`](./03-sockets-jackson) | Sockets multihilo + Jackson (JSON) | Objetos tipados sobre el cable; herramienta externa que evita parseo manual | Aún es TCP crudo; cliente y servidor solo Java |
| 4 | [`04-rmi`](./04-rmi) | Java RMI | Llamadas remotas tipadas como si fueran locales | Solo Java; difícil de atravesar firewalls |
| 5 | [`05-fastapi`](./05-fastapi) | FastAPI (Python)  | Interoperabilidad total vía HTTP+JSON; documentación automática con Swagger | — (estado del arte para microservicios web) |

## Idea central

La calculadora es deliberadamente trivial: el foco no está en *qué* hace el servicio, sino en *cómo* se comunican el cliente y el servidor. Repetir la misma funcionalidad con cinco mecanismos distintos hace evidentes los trade-offs.

Una segunda idea que el proyecto busca transmitir: **a medida que los protocolos se vuelven más estándar y abiertos, aparecen herramientas externas que los hacen ergonómicos**. Jackson aparece por primera vez en la Etapa 3 (todavía con sockets) y reaparece en la Etapa 5 (ahora sobre HTTP). El transporte cambia, pero la herramienta de serialización permanece — una pista directa de cuáles son las piezas que sobreviven a los cambios de paradigma.

## Cómo recorrer el proyecto

Lea las etapas en orden. Cada README termina señalando las limitaciones que motivan la siguiente. Los archivos de código incluyen comentarios pensados para uso académico.

## Requisitos generales

- **Java 11 o superior** (compilador `javac` y runtime `java`). Probado con OpenJDK 21.
- **Python 3.9+** y `pip` para el servidor FastAPI de la etapa 5.
- **curl** (para los ejemplos REST de la etapa 5 y para descargar Jackson).

## Jackson (Etapas 3 y 5)

Los JARs de Jackson no se versionan en este repositorio. Antes de compilar las etapas 3 o 5, ejecute desde la raíz:

```bash
bash descargar-jackson.sh
```

Esto descarga tres JARs (`jackson-core`, `jackson-annotations`, `jackson-databind`) y los copia a `03-sockets-jackson/libs/` y `05-fastapi/cliente-java/libs/`. Tamaño total: ~2 MB.

Alternativamente, los detalles para descarga manual están en el README de cada etapa.

## Estructura del repositorio

```
calculadora-distribuida/
├── README.md                          (este archivo)
├── .gitignore
├── descargar-jackson.sh               (helper para bajar los JARs)
├── docs/
│   └── comparacion.md                 (tabla de trade-offs detallada)
├── 01-sockets-basico/
│   ├── README.md
│   ├── servidor/src/ServidorCalculadora.java
│   └── cliente/src/ClienteCalculadora.java
├── 02-sockets-multihilo/
│   ├── README.md
│   ├── servidor/src/ServidorCalculadora.java
│   ├── servidor/src/HiloCliente.java
│   └── cliente/src/ClienteCalculadora.java
├── 03-sockets-jackson/
│   ├── README.md
│   ├── libs/                          (vacío; agregue aquí los JARs de Jackson)
│   ├── common/src/com/calculadora/common/SolicitudOperacion.java
│   ├── common/src/com/calculadora/common/RespuestaOperacion.java
│   ├── servidor/src/com/calculadora/servidor/ServidorCalculadora.java
│   ├── servidor/src/com/calculadora/servidor/HiloCliente.java
│   └── cliente/src/com/calculadora/cliente/ClienteCalculadora.java
├── 04-rmi/
│   ├── README.md
│   ├── common/src/com/calculadora/common/CalculadoraRemota.java
│   ├── servidor/src/com/calculadora/servidor/CalculadoraImpl.java
│   ├── servidor/src/com/calculadora/servidor/ServidorRMI.java
│   └── cliente/src/com/calculadora/cliente/ClienteRMI.java
└── 05-fastapi/
    ├── README.md
    ├── servidor/main.py
    ├── servidor/requirements.txt
 
```

## Licencia

MIT — uso libre para fines académicos.
