# Etapa 3 — Java RMI (Remote Method Invocation)

RMI permite invocar métodos en un objeto que vive en otra JVM **como si fuera local**. El runtime de Java se encarga de serializar los argumentos, transportarlos por la red y devolver el resultado.

## Estructura del proyecto

```
03-rmi/
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

Java 11+ recomendado. Desde la raíz `03-rmi/`:

### 1. Compilar

```bash
# Carpeta de salida común
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

## Qué ganamos respecto a sockets

- **Sin protocolo manual**: ya no escribimos `"suma,3,4"` ni parseamos texto. Llamamos `calc.sumar(3, 4)`.
- **Tipado fuerte**: el compilador detecta errores de firma. En sockets, un typo en `"sumA,3,4"` solo se descubría en tiempo de ejecución.
- **Excepciones que viajan**: `ArithmeticException` lanzada en el servidor llega al cliente.

## Qué seguimos perdiendo (motivación para la Etapa 4)

- **Acoplamiento al lenguaje**: RMI es Java-a-Java. Un cliente en Python, JavaScript o Go no puede consumir este servicio sin gateways.
- **Acoplamiento de versión**: cliente y servidor deben compartir la misma interfaz compilada. Cambiar la firma rompe a ambos.
- **Firewalls y NAT**: RMI usa puertos dinámicos para los stubs, lo que dificulta su despliegue en entornos modernos (cloud, contenedores).

La **Etapa 4 (FastAPI + JSON)** resuelve esto con HTTP y JSON: un protocolo universal que cualquier lenguaje entiende.
