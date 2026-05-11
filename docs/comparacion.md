# Comparación detallada de las cinco etapas

Este documento profundiza en los trade-offs de cada mecanismo, más allá del resumen del README principal.

## Dimensiones de comparación

### 1. Protocolo de aplicación

- **Etapas 1 y 2 — sockets básicos**: protocolo *ad-hoc* en texto plano (`operacion,a,b`). Lo definimos nosotros, lo parseamos nosotros. Cada cambio rompe a todos los clientes.
- **Etapa 3 — sockets + Jackson**: el mensaje sigue siendo una línea, pero su contenido es **JSON estructurado**. Los nombres de los campos viajan con el mensaje, así que el orden ya no importa y un campo nuevo puede ignorarse sin romper a los clientes viejos.
- **Etapa 4 — RMI**: el protocolo es *implícito*: la firma de la interfaz Java compilada. No vemos JSON ni texto, vemos llamadas a métodos.
- **Etapa 5 — FastAPI + JSON**: protocolo estándar (HTTP) + formato estándar (JSON), descrito formalmente con OpenAPI. Cualquier herramienta del ecosistema web puede consumirlo.

### 2. Concurrencia

- **Etapa 1**: ninguna; un cliente bloquea a los demás.
- **Etapas 2 y 3**: explícita, con `ExecutorService`. Nosotros decidimos el tamaño del pool.
- **Etapa 4 — RMI**: implícita. El runtime de Java reserva hilos para atender invocaciones entrantes.
- **Etapa 5 — FastAPI**: implícita y asíncrona (Uvicorn + asyncio). FastAPI puede atender miles de conexiones concurrentes con pocos hilos del SO.

### 3. Tipado del mensaje

- **Etapas 1 y 2**: ninguno. Todo es `String`. Si el cliente envía `"sumA"` (con typo) el servidor lo descubre en runtime.
- **Etapa 3**: tipado en ambos extremos a través de los DTOs (`SolicitudOperacion`, `RespuestaOperacion`). Jackson valida tipos al deserializar.
- **Etapa 4 — RMI**: tipado fuerte verificado en compilación. Si la firma cambia, el cliente no compila.
- **Etapa 5 — FastAPI + Jackson**: tipado en ambos extremos. Pydantic valida el JSON entrante en el servidor; Jackson valida en el cliente Java.

### 4. Interoperabilidad entre lenguajes

- **Etapas 1 y 2 — sockets crudos**: posible pero costoso. El cliente Python tendría que reimplementar el parseo de `operacion,a,b`.
- **Etapa 3 — sockets + JSON**: mejora intermedia. Como el formato es JSON estándar, un cliente Python o JavaScript podría hablar con este servidor — pero seguiría dependiendo de TCP crudo y de la convención "una línea por mensaje".
- **Etapa 4 — RMI**: solo Java-a-Java. Existen puentes (CORBA, JNI) pero son frágiles.
- **Etapa 5 — FastAPI + JSON sobre HTTP**: total. El servidor habla con cualquier cliente HTTP; el cliente Java podría consumir cualquier API REST sin cambios estructurales.

### 5. Manejo de errores

- **Etapas 1 y 2**: cadenas convencionales (`"ERROR: ..."`). El cliente debe parsear el prefijo. El canal de éxito y el canal de error están mezclados.
- **Etapa 3**: campo `exito` (boolean) + campo `error` (string) dentro del JSON. Canal separado, estructurado.
- **Etapa 4 — RMI**: excepciones Java. Una `ArithmeticException` en el servidor llega al cliente con su tipo y stack trace.
- **Etapa 5 — FastAPI**: códigos HTTP estándar (400, 404, 500…) más cuerpo JSON con detalle. Cliente Java traduce el código a una excepción local.

### 6. Documentación

- **Etapas 1, 2, 3**: 100% manual. Hay que escribir un documento que describa el protocolo.
- **Etapa 4 — RMI**: la interfaz Java sirve como contrato, pero hay que documentar el comportamiento aparte (Javadoc).
- **Etapa 5 — FastAPI**: documentación interactiva automática en `/docs` (Swagger) y `/redoc`. Pydantic + decoradores generan el esquema OpenAPI sin esfuerzo extra.

### 7. Observabilidad y herramientas externas

- **Etapas 1 y 2**: prácticamente nulas. `tcpdump` o `wireshark` para depurar.
- **Etapa 3**: aún limitada en transporte (sigue siendo TCP), pero como el contenido es JSON legible, depurar con `wireshark` se vuelve trivial.
- **Etapa 4 — RMI**: limitadas; herramientas específicas de Java (JMX, jconsole).
- **Etapa 5 — HTTP+JSON**: ecosistema enorme — Postman, Insomnia, curl, OpenAPI Generator (para generar SDKs en cualquier lenguaje), middlewares de logging, métricas Prometheus, tracing distribuido, etc.

## El papel de las herramientas externas

Una observación que vale la pena explicitar: cada salto evolutivo viene acompañado de un ecosistema de herramientas que hace ergonómico el nuevo enfoque.

- En **sockets crudos**, la herramienta externa más útil es probablemente la documentación del protocolo... o un buen test de integración.
- En **sockets + JSON (Etapa 3)** entra en escena **Jackson**. Sin él, hablar JSON en Java sería tan frágil como hablar texto crudo en las etapas anteriores.
- En **RMI**, el `rmiregistry` y herramientas como JMX.
- En **HTTP+JSON (Etapa 5)**, la lista es larguísima:
  - **Jackson / Gson** para JSON en Java (lo seguimos usando — la pieza sobrevive desde la Etapa 3).
  - **OkHttp / Retrofit / Apache HttpClient** como alternativas al `HttpClient` del JDK.
  - **Pydantic** para validación en Python.
  - **OpenAPI Generator** para generar clientes en decenas de lenguajes a partir del esquema.
  - **Postman / Insomnia** para pruebas manuales.
  - **Swagger UI / ReDoc** para documentación.

El hecho de que Jackson aparezca en la Etapa 3 (sobre sockets) y vuelva a aparecer en la Etapa 5 (sobre HTTP) ilustra bien la lección: **el transporte y el formato son piezas separables**. JSON+Jackson sobreviven a un cambio de transporte radical (de sockets crudos a HTTP), mientras que la propia idea de un "protocolo en texto plano" no sobrevive el paso entre las Etapas 2 y 3.

## Más allá de FastAPI: hacia dónde va la evolución

La Etapa 5 representa el estado del arte *del REST clásico*, pero la evolución continúa:

- **gRPC** (Google): vuelve a un modelo parecido a RMI (llamadas tipadas a métodos remotos) pero con un IDL portable (Protocol Buffers) y soporte multilenguaje. Más rápido y compacto que JSON, a costa de no ser legible por humanos.
- **GraphQL**: el cliente decide qué campos quiere, evitando el problema de over/under-fetching de REST.
- **Streaming y mensajería asíncrona** (Kafka, NATS, RabbitMQ): para arquitecturas event-driven donde no hay petición-respuesta sino flujos continuos de eventos.
- **WebSockets / Server-Sent Events**: comunicación bidireccional o push desde servidor, sobre HTTP.

Cada uno responde a limitaciones que ni siquiera REST resuelve bien. La historia, lejos de cerrarse, sigue abierta.
