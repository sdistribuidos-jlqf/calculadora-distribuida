# Calculadora Distribuida — Evolución de los Sistemas Distribuidos

Proyecto educativo que muestra, con una **calculadora básica** (sumar, restar, multiplicar, dividir) como hilo conductor, cómo han evolucionado los mecanismos de comunicación entre procesos distribuidos.

Cada carpeta es una etapa autocontenida con su propio README, código y motivación. Las etapas avanzan respondiendo a las limitaciones de la anterior.

## Etapas

| # | Carpeta | Tecnología | Qué resuelve | Qué deja pendiente |
|---|---|---|---|---|
| 1 | [`01-sockets-basico`](./01-sockets-basico) | Sockets TCP en Java, single-thread | Comunicación cliente-servidor mínima | Atiende un cliente a la vez; protocolo de texto frágil |
| 2 | [`02-sockets-multihilo`](./02-sockets-multihilo) | Sockets TCP + `ExecutorService` | Concurrencia: N clientes en paralelo | Sigue siendo un protocolo de texto ad-hoc |
| 3 | [`03-rmi`](./03-rmi) | Java RMI | Llamadas remotas tipadas como si fueran locales | Solo Java; difícil de atravesar firewalls |
| 4 | [`04-fastapi-jackson`](./04-fastapi-jackson) | FastAPI (Python) + cliente Java con Jackson | Interoperabilidad total vía HTTP+JSON; documentación automática | — (estado del arte para microservicios web) |

## Idea central

La calculadora es deliberadamente trivial: el foco no está en *qué* hace el servicio, sino en *cómo* se comunican el cliente y el servidor. Repetir la misma funcionalidad con cuatro mecanismos distintos hace evidentes los trade-offs.

Una segunda idea que el proyecto busca transmitir: **a medida que los protocolos se vuelven más estándar y abiertos, aparecen herramientas externas que los hacen ergonómicos**. Por ejemplo, en la Etapa 4 usamos Jackson en Java porque el JDK no trae soporte nativo de JSON; sin Jackson tendríamos que construir el JSON a mano y volveríamos al mismo problema de fragilidad de la Etapa 1.

## Cómo recorrer el proyecto

Recomendamos leer las etapas en orden. Cada README termina señalando las limitaciones que motivan la siguiente. Los archivos de código incluyen comentarios explicativos pensados para uso académico.

## Requisitos generales

- **Java 11 o superior** para las etapas 1, 2, 3 y el cliente de la 4.
- **Python 3.9+** y `pip` para el servidor FastAPI de la etapa 4.
- **Maven 3.6+** para el cliente Java de la etapa 4.

Cada etapa tiene sus propias instrucciones de compilación y ejecución en su README.

## Estructura del repositorio

```
calculadora-distribuida/
├── README.md                          (este archivo)
├── .gitignore
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
├── 03-rmi/
│   ├── README.md
│   ├── common/src/com/calculadora/common/CalculadoraRemota.java
│   ├── servidor/src/com/calculadora/servidor/CalculadoraImpl.java
│   ├── servidor/src/com/calculadora/servidor/ServidorRMI.java
│   └── cliente/src/com/calculadora/cliente/ClienteRMI.java
└── 04-fastapi-jackson/
    ├── README.md
    ├── servidor/main.py
    ├── servidor/requirements.txt
    └── cliente-java/
        ├── pom.xml
        └── src/main/java/com/calculadora/...
```

## Licencia

MIT — uso libre para fines académicos.
