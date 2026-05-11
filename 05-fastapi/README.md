# Etapa 5 — FastAPI + cliente Java con Jackson

Última etapa de la evolución: el servidor habla **HTTP + JSON**, un estándar universal. Cualquier cliente —Java, Python, JavaScript, curl, Postman— puede consumirlo. Es lo que hoy llamamos un servicio REST.

Respecto a la Etapa 3 (sockets multihilo + Jackson), aquí cambia el **transporte**: en lugar de TCP crudo usamos HTTP, lo que aporta verbos (`POST`), rutas (`/operar`), códigos de estado (200, 400, 500) y un ecosistema gigante de herramientas (curl, Swagger, Postman, etc.).

## Estructura

```
05-fastapi/
├── servidor/         -> FastAPI (Python)
└── cliente-java/     -> Cliente Java con Jackson (sin Maven, javac puro)
    ├── libs/         -> JARs de Jackson (no versionados)
    └── src/com/calculadora/...
```

## Servidor FastAPI

### Requisitos

- Python 3.9+
- pip

### Instalación

```bash
cd 05-fastapi/servidor
python -m venv venv
source venv/bin/activate          # En Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### Ejecución

```bash
uvicorn main:app --reload
```

El servidor queda disponible en `http://localhost:8000`.

### Documentación interactiva (Swagger)

FastAPI genera documentación OpenAPI **automáticamente**. Visite:

- `http://localhost:8000/docs` — Swagger UI: probar el endpoint desde el navegador.
- `http://localhost:8000/redoc` — ReDoc: documentación más legible.

Esta es una ventaja enorme respecto a las etapas anteriores: no escribimos una línea de documentación y ya tenemos un cliente web funcional.

### Probar con curl

```bash
# Suma
curl -X POST http://localhost:8000/operar \
     -H "Content-Type: application/json" \
     -d '{"operacion":"suma","a":3,"b":4}'
# {"operacion":"suma","a":3.0,"b":4.0,"resultado":7.0}

# División entre cero (devuelve HTTP 400)
curl -i -X POST http://localhost:8000/operar \
     -H "Content-Type: application/json" \
     -d '{"operacion":"division","a":10,"b":0}'
# HTTP/1.1 400 Bad Request
# {"detail":"Division entre cero"}

# Operación inválida (validación automática de Pydantic)
curl -i -X POST http://localhost:8000/operar \
     -H "Content-Type: application/json" \
     -d '{"operacion":"raiz","a":9,"b":0}'
# HTTP/1.1 422 Unprocessable Entity
# {"detail":[{"loc":["body","operacion"],"msg":"...","type":"..."}]}
```

## Cliente Java con Jackson — sin Maven

El cliente Java se compila y ejecuta con **`javac` y `java` puros**. Los JARs de Jackson se colocan en `cliente-java/libs/`.

### Obtener los JARs de Jackson

Si ya descargó Jackson para la Etapa 3, los JARs ya están en `05-fastapi/cliente-java/libs/` (el script `descargar-jackson.sh` los copia a ambas etapas).

Si no, desde la raíz del repositorio:

```bash
bash descargar-jackson.sh
```

O bájelos a mano:

```bash
cd 05-fastapi/cliente-java/libs
curl -LO https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.0/jackson-core-2.17.0.jar
curl -LO https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.0/jackson-annotations-2.17.0.jar
curl -LO https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.17.0/jackson-databind-2.17.0.jar
```

### Compilar

Desde `05-fastapi/cliente-java/`:

```bash
mkdir -p build
javac -d build -cp "libs/*" src/com/calculadora/modelo/*.java
javac -d build -cp "build:libs/*" src/com/calculadora/cliente/*.java
```

**Nota Windows:** use `";"` como separador en el classpath, no `":"`. Es decir, `"build;libs/*"`.

### Ejecutar

```bash
java -cp "build:libs/*" com.calculadora.cliente.ClienteFastAPI
```

(En Windows: `java -cp "build;libs/*" com.calculadora.cliente.ClienteFastAPI`.)

Sesión de ejemplo:

```
> suma 3 4
Resultado: 7.0
> multiplicacion 6 7
Resultado: 42.0
> division 8 0
Error: HTTP 400 - {"detail":"Division entre cero"}
> salir
```

## Por qué Jackson sigue siendo necesario

El JDK incluye `java.net.http.HttpClient` desde Java 11, así que ya no necesitamos una librería externa para hablar HTTP. Pero el JDK **no incluye nada para JSON**. Sin Jackson, el cliente tendría que armar a mano el cuerpo:

```java
// Sin Jackson — frágil, sin escapado, propenso a errores
String json = "{\"operacion\":\"" + op + "\",\"a\":" + a + ",\"b\":" + b + "}";
```

Con Jackson, basta con:

```java
String json = mapper.writeValueAsString(new SolicitudOperacion(op, a, b));
RespuestaOperacion r = mapper.readValue(jsonRespuesta, RespuestaOperacion.class);
```

Esto es exactamente lo mismo que vimos en la Etapa 3, pero ahora viajando sobre HTTP en vez de sobre un socket TCP crudo. La continuidad es deliberada: **Jackson es la pieza que sobrevive desde la Etapa 3 hasta hoy**.

## Por qué este enfoque ganó

| Dimensión | Sockets (1-3) | RMI (4) | FastAPI + JSON (5) |
|---|---|---|---|
| Independencia de lenguaje | manual | no | **sí** |
| Atravesabilidad de red | sí | difícil | **sí** |
| Documentación automática | no | no | **sí (Swagger)** |
| Tipado en el contrato | no/sí (Etapa 3 con DTOs) | sí | sí (con OpenAPI) |
| Herramientas externas (curl, Postman, generadores de SDK) | no | no | **enorme** |

El precio es que perdemos algo del confort de RMI (la llamada deja de "parecer local"). Lo ganado: interoperabilidad total. Hoy es el patrón dominante para integración entre servicios web.
