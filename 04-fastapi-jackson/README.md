# Etapa 4 — FastAPI + cliente Java con Jackson

Última etapa de la evolución: el servidor habla **HTTP + JSON**, un estándar universal. Cualquier cliente —Java, Python, JavaScript, curl— puede consumirlo.

## Estructura

```
04-fastapi-jackson/
├── servidor/         -> FastAPI (Python)
└── cliente-java/     -> Cliente Java con Jackson (Maven)
```

## Servidor FastAPI

### Requisitos

- Python 3.9+
- pip

### Instalación y ejecución

```bash
cd servidor
python -m venv venv
source venv/bin/activate          # En Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload
```

El servidor queda disponible en `http://localhost:8000`.

Bonus de FastAPI: visite `http://localhost:8000/docs` para ver la documentación interactiva (Swagger) generada automáticamente. Puede probar el endpoint desde el navegador.

### Probar con curl

```bash
curl -X POST http://localhost:8000/operar \
     -H "Content-Type: application/json" \
     -d '{"operacion": "suma", "a": 3, "b": 4}'
# {"operacion":"suma","a":3.0,"b":4.0,"resultado":7.0}
```

## Cliente Java con Jackson

### Requisitos

- Java 11+
- Maven 3.6+

### Compilación y ejecución

```bash
cd cliente-java
mvn compile
mvn exec:java
```

(O bien empacar como JAR: `mvn package` y ejecutar el resultado.)

## Por qué Jackson

Java no trae soporte de JSON en la biblioteca estándar. Sin una herramienta como Jackson tendríamos que **construir el JSON a mano** con concatenación de strings:

```java
// Sin Jackson — frágil, repetitivo, propenso a errores
String json = "{\"operacion\":\"" + op + "\",\"a\":" + a + ",\"b\":" + b + "}";
```

Con Jackson:

```java
// Con Jackson — declarativo, seguro, mantenible
String json = mapper.writeValueAsString(new SolicitudOperacion(op, a, b));
```

Y para deserializar la respuesta:

```java
RespuestaOperacion r = mapper.readValue(jsonRespuesta, RespuestaOperacion.class);
double resultado = r.getResultado();
```

Jackson cuida los detalles: escapado de caracteres especiales, formato de números, manejo de campos opcionales, tolerancia a campos desconocidos (`@JsonIgnoreProperties(ignoreUnknown = true)`), etc.

## Por qué este enfoque ganó

| Dimensión | Sockets | RMI | FastAPI + JSON |
|---|---|---|---|
| Independencia de lenguaje | manual | no | **sí** |
| Atravesabilidad de red | sí | difícil | **sí** |
| Documentación automática | no | no | **sí (Swagger)** |
| Tipado en el contrato | no | sí | sí (con OpenAPI) |
| Ecosistema de herramientas | bajo | bajo | **enorme** |

El precio es que perdemos algo del confort de RMI (la llamada deja de "parecer local"), pero ganamos interoperabilidad total. Hoy es el patrón dominante: prácticamente toda integración entre servicios web pasa por HTTP+JSON o por su evolución (gRPC, GraphQL).
