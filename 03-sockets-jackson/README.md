# Etapa 3 — Sockets multihilo + Jackson (JSON)

Misma infraestructura de sockets multihilo de la Etapa 2, pero el protocolo de aplicación deja de ser texto ad-hoc y pasa a ser **objetos Java serializados a JSON** con [Jackson](https://github.com/FasterXML/jackson).

## Estructura

```
03-sockets-jackson/
├── common/    -> DTOs compartidos (SolicitudOperacion, RespuestaOperacion)
├── servidor/  -> Servidor multihilo que deserializa peticiones
├── cliente/   -> Cliente que serializa peticiones y deserializa respuestas
└── libs/      -> JARs de Jackson (no versionados; ver más abajo)
```

## Protocolo de aplicación

Cada mensaje es **un objeto JSON por línea** (delimitado por `\n`).

Solicitud del cliente:
```json
{"operacion":"suma","a":3,"b":4}
```

Respuesta del servidor (éxito):
```json
{"exito":true,"resultado":7.0,"error":null}
```

Respuesta del servidor (error):
```json
{"exito":false,"resultado":0.0,"error":"Division entre cero"}
```

Para cerrar la sesión el cliente envía la línea `salir` (texto plano, no JSON).

## Qué cambió respecto a la Etapa 2

| Aspecto | Etapa 2 (texto plano) | Etapa 3 (JSON con Jackson) |
|---|---|---|
| Mensaje en el cable | `suma,3,4` | `{"operacion":"suma","a":3,"b":4}` |
| Construcción en el cliente | concatenar strings a mano | `mapper.writeValueAsString(req)` |
| Parseo en el servidor | `split(",")` + `Double.parseDouble` | `mapper.readValue(json, Solicitud.class)` |
| Tipos | todo es `String` | tipados (double, String, boolean) |
| Errores del cliente | prefijo `"ERROR: ..."` mezclado en el canal | campo dedicado `error` en el JSON |
| Si añadimos un campo nuevo | rompe a los clientes viejos | clientes viejos lo ignoran |
| Cliente no-Java | reimplementar el parseo | hablar JSON, que es estándar |

El modelo de concurrencia (pool de hilos) **no cambia**. La etapa demuestra una idea importante: concurrencia y protocolo son dimensiones independientes.

## Por qué Jackson

Java no trae soporte de JSON en la biblioteca estándar. Sin Jackson tendríamos que armar el JSON con concatenación de strings — exactamente la fragilidad de las Etapas 1 y 2, solo que con llaves y comillas. Jackson resuelve esto con dos llamadas:

```java
ObjectMapper mapper = new ObjectMapper();

// POJO -> JSON
String json = mapper.writeValueAsString(new SolicitudOperacion("suma", 3, 4));

// JSON -> POJO
SolicitudOperacion req = mapper.readValue(json, SolicitudOperacion.class);
```

`ObjectMapper` es **thread-safe**: se instancia una sola vez y se comparte entre todos los hilos del servidor.

## Requisitos

- Java 11+
- Tres JARs de Jackson colocados en la carpeta `libs/`:
  - `jackson-core-2.17.0.jar`
  - `jackson-annotations-2.17.0.jar`
  - `jackson-databind-2.17.0.jar`

## Obtener los JARs de Jackson

Los JARs binarios no se versionan en este repositorio (buena práctica). Hay dos formas de obtenerlos sin usar Maven:

### Opción A — descargar manualmente con `curl` o el navegador

```bash
cd 03-sockets-jackson/libs
curl -LO https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.0/jackson-core-2.17.0.jar
curl -LO https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.0/jackson-annotations-2.17.0.jar
curl -LO https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.17.0/jackson-databind-2.17.0.jar
```

### Opción B — usar el script `descargar-jackson.sh` incluido

Desde la raíz del repositorio:

```bash
bash descargar-jackson.sh
```

El script descarga los tres JARs y los copia a `03-sockets-jackson/libs/` y `05-fastapi/cliente-java/libs/`.

## Cómo ejecutar

Desde la raíz `03-sockets-jackson/`:

### 1. Compilar

```bash
mkdir -p build

# Compilar los DTOs comunes (dependen de Jackson)
javac -d build -cp "libs/*" common/src/com/calculadora/common/*.java

# Compilar el servidor (depende de common y de Jackson)
javac -d build -cp "build:libs/*" servidor/src/com/calculadora/servidor/*.java

# Compilar el cliente
javac -d build -cp "build:libs/*" cliente/src/com/calculadora/cliente/*.java
```

**Nota Windows:** en PowerShell o `cmd`, el separador de classpath es `;` en lugar de `:`. Use `"build;libs/*"`.

### 2. Ejecutar el servidor

```bash
java -cp "build:libs/*" com.calculadora.servidor.ServidorCalculadora
```

### 3. Ejecutar el cliente (en otra terminal)

```bash
java -cp "build:libs/*" com.calculadora.cliente.ClienteCalculadora
```

Sesión de ejemplo:

```
> suma 3 4
  enviado:  {"operacion":"suma","a":3.0,"b":4.0}
  recibido: {"exito":true,"resultado":7.0,"error":null}
Resultado: 7.0
> division 8 0
  enviado:  {"operacion":"division","a":8.0,"b":0.0}
  recibido: {"exito":false,"resultado":0.0,"error":"Division entre cero"}
Error del servidor: Division entre cero
> salir
```

## Limitaciones que persisten

Hicimos el protocolo mucho más robusto, pero:

- Seguimos transportando sobre **sockets TCP crudos**. No hay códigos de estado, no hay rutas, no hay headers. Cada vez que añadamos un caso de uso (ejemplo: una segunda operación distinta de "operar"), tendremos que extender el JSON a mano.
- El **modelo de comunicación sigue siendo "enviar bytes y parsear"**. Conceptualmente, llamar a `sumar(3, 4)` no se ve como una llamada a método, se ve como construir un JSON y mandarlo.

La **Etapa 4 (RMI)** ataca este último punto: deja que la sintaxis del código sea idéntica a una llamada local. La **Etapa 5 (FastAPI + Jackson)** sube un escalón más: cambia el transporte de TCP crudo a HTTP, que aporta semántica estándar (verbos, rutas, códigos de estado) y herramientas.
