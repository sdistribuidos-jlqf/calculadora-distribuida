# Etapa 4 — Java RMI (Remote Method Invocation)

En la Etapa 3 logramos enviar **objetos** entre cliente y servidor (gracias a Jackson + JSON), pero el código de aplicación todavía hace "construir mensaje, mandar bytes, recibir bytes, parsear". RMI elimina ese paso: invocar un método en otra JVM se ve **igual que una llamada local**.

## Estructura del proyecto

```
04-rmi/
├── common/    -> Interfaz remota CalculadoraRemota (compartida)
├── servidor/  -> Implementación + main que la registra
└── cliente/   -> Cliente que la consume
```

## Conceptos clave

- **Interfaz remota** (`CalculadoraRemota`): el contrato. Extiende `java.rmi.Remote` y todos sus métodos lanzan `RemoteException`.
- **Implementación** (`CalculadoraImpl`): extiende `UnicastRemoteObject`, queda exportada al instanciarla.
- **Registry** (`rmiregistry`): un servicio de nombres donde el servidor publica el objeto y el cliente lo busca por nombre lógico.
- **Stub**: el objeto que recibe el cliente al hacer `lookup()`. Aparenta ser el servicio, pero internamente reenvía las llamadas por la red.

## Cómo ejecutar

Java 11+ recomendado. Desde la raíz `04-rmi/`:

### 1. Compilar

```bash
mkdir -p build

# Compilar la interfaz común
javac -d build common/src/com/calculadora/common/CalculadoraRemota.java

# Compilar el servidor (depende de common)
javac -d build -cp build servidor/src/com/calculadora/servidor/*.java

# Compilar el cliente (depende de common)
javac -d build -cp build cliente/src/com/calculadora/cliente/*.java
```

### 2. Ejecutar el servidor

```bash
java -cp build com.calculadora.servidor.ServidorRMI
```

### 3. Ejecutar el cliente (en otra terminal)

```bash
java -cp build com.calculadora.cliente.ClienteRMI
```

## Qué ganamos respecto a la Etapa 3

En la Etapa 3 el cliente hacía:

```java
SolicitudOperacion req = new SolicitudOperacion("suma", 3, 4);
String json = mapper.writeValueAsString(req);
salida.println(json);
String respuesta = entrada.readLine();
RespuestaOperacion resp = mapper.readValue(respuesta, RespuestaOperacion.class);
if (resp.isExito()) {
    double resultado = resp.getResultado();
}
```

Con RMI todo eso se reduce a:

```java
double resultado = calc.sumar(3, 4);
```

Diferencias:

- **Sin serialización explícita**: el runtime de RMI la hace por nosotros.
- **Sin protocolo manual**: ya no diseñamos un formato de mensaje.
- **Tipado fuerte verificado en compilación**: un typo en el nombre del método no compila, no falla en runtime.
- **Excepciones que viajan**: `ArithmeticException` lanzada en el servidor llega al cliente con su tipo y stack trace.

## Qué seguimos perdiendo (motivación para la Etapa 5)

- **Acoplamiento al lenguaje**: RMI es Java-a-Java. Un cliente en Python, JavaScript o Go no puede consumir este servicio sin gateways.
- **Acoplamiento de versión**: cliente y servidor deben compartir la misma interfaz compilada. Cambiar la firma rompe a ambos.
- **Firewalls y NAT**: RMI usa puertos dinámicos para los stubs, lo que dificulta su despliegue en entornos modernos (cloud, contenedores).

La **Etapa 5 (FastAPI + JSON)** resuelve esto con HTTP y JSON: un protocolo universal que cualquier lenguaje entiende, y reutiliza lo que ya aprendimos en la Etapa 3 sobre Jackson.
