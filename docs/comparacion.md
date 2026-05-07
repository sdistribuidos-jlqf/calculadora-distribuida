# Comparación detallada de las cuatro etapas

Este documento profundiza en los trade-offs de cada mecanismo, más allá del resumen del README principal.

## Dimensiones de comparación

### 1. Protocolo

- **Sockets básicos / multihilo**: protocolo *ad-hoc* en texto plano (`operacion,a,b`). Lo definimos nosotros, lo parseamos nosotros. Cada cambio rompe a todos los clientes.
- **RMI**: el protocolo es *implícito*: la firma de la interfaz Java compilada. No vemos JSON ni texto, vemos llamadas a métodos.
- **FastAPI + JSON**: protocolo estándar (HTTP) + formato estándar (JSON), descrito formalmente con OpenAPI. Cualquier herramienta del ecosistema web puede consumirlo.

### 2. Concurrencia

- **Sockets básicos**: ninguna; un cliente bloquea a los demás.
- **Sockets multihilo**: explícita, con `ExecutorService`. Nosotros decidimos el tamaño del pool.
- **RMI**: implícita. El runtime de Java reserva hilos para atender invocaciones entrantes.
- **FastAPI**: implícita y asíncrona (Uvicorn + asyncio). FastAPI puede atender miles de conexiones concurrentes con pocos hilos del SO.

### 3. Tipado

- **Sockets**: ninguno. Todo es `String`. Si el cliente envía `"sumA"` (con typo) el servidor lo descubre en runtime.
- **RMI**: tipado fuerte verificado en compilación. Si la firma cambia, el cliente no compila.
- **FastAPI + Jackson**: tipado en ambos extremos. Pydantic valida el JSON entrante; Jackson valida el JSON saliente y entrante en el cliente Java.

### 4. Interoperabilidad entre lenguajes

- **Sockets**: posible pero costoso. El cliente Python tendría que reimplementar el parseo de `operacion,a,b`.
- **RMI**: solo Java-a-Java. Existen puentes (CORBA, JNI) pero son frágiles.
- **FastAPI + JSON**: total. El servidor habla con cualquier cliente HTTP; el cliente Java podría consumir cualquier API REST sin cambios estructurales.

### 5. Manejo de errores

- **Sockets**: cadenas convencionales (`"ERROR: ..."`). El cliente debe parsear el prefijo.
- **RMI**: excepciones Java. Una `ArithmeticException` en el servidor llega al cliente con su tipo y stack trace.
- **FastAPI**: códigos HTTP estándar (400, 404, 500…) más cuerpo JSON con detalle. Cliente Java traduce el código a una excepción local.

### 6. Documentación

- **Sockets**: 100 % manual. Hay que escribir un documento que describa el protocolo.
- **RMI**: la interfaz Java sirve como contrato, pero hay que documentar el comportamiento aparte (Javadoc).
- **FastAPI**: documentación interactiva automática en `/docs` (Swagger) y `/redoc`. Pydantic + decoradores generan el esquema OpenAPI sin esfuerzo extra.

### 7. Observabilidad y herramientas externas

- **Sockets**: prácticamente nulas. `tcpdump` o `wireshark` para depurar.
- **RMI**: limitadas; herramientas específicas de Java (JMX, jconsole).
- **FastAPI + JSON**: ecosistema enorme — Postman, Insomnia, curl, OpenAPI Generator (para generar SDKs en cualquier lenguaje), middlewares de logging, métricas Prometheus, tracing distribuido, etc.

## El papel de las herramientas externas

Una observación que vale la pena explicitar: cada salto evolutivo viene acompañado de un ecosistema de herramientas que hace ergonómico el nuevo enfoque.

- En **sockets**, la herramienta externa más útil es probablemente la documentación... o un buen test de integración.
- En **RMI**, el `rmiregistry` y herramientas como JMX.
- En **HTTP+JSON**, la lista es larguísima:
  - **Jackson / Gson** para JSON en Java.
  - **OkHttp / Retrofit / Apache HttpClient** como alternativas al HttpClient del JDK.
  - **Pydantic** para validación en Python.
  - **OpenAPI Generator** para generar clientes en decenas de lenguajes a partir del esquema.
  - **Postman / Insomnia** para pruebas manuales.
  - **Swagger UI / ReDoc** para documentación.

El proyecto incluye Jackson explícitamente para mostrar este punto: sin Jackson, el cliente Java de la Etapa 4 tendría que construir JSON con concatenación de strings — el mismo problema de fragilidad que las Etapas 1 y 2. Las herramientas externas no son un detalle: son lo que hace viable el patrón.

## Más allá de FastAPI: hacia dónde va la evolución

La Etapa 4 representa el estado del arte *del REST clásico*, pero la evolución continúa:

- **gRPC** (Google): vuelve a un modelo parecido a RMI (llamadas tipadas a métodos remotos) pero con un IDL portable (Protocol Buffers) y soporte multilenguaje. Más rápido y compacto que JSON, a costa de no ser legible por humanos.
- **GraphQL**: el cliente decide qué campos quiere, evitando el problema de over/under-fetching de REST.
- **Streaming y mensajería asíncrona** (Kafka, NATS, RabbitMQ): para arquitecturas event-driven donde no hay petición-respuesta sino flujos continuos de eventos.
- **WebSockets / Server-Sent Events**: comunicación bidireccional o push desde servidor, sobre HTTP.

Cada uno responde a limitaciones que ni siquiera REST resuelve bien. La historia, lejos de cerrarse, sigue abierta.
