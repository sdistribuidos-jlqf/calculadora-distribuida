# Etapa 2 — Sockets multihilo

Misma idea que la Etapa 1, pero el servidor ahora atiende **múltiples clientes en paralelo** usando un `ExecutorService` con un pool de hilos.

## Qué cambió

- **Servidor**: por cada `accept()`, en lugar de procesar al cliente en el hilo principal, se delega a un `Runnable` (`HiloCliente`) que corre en un hilo del pool. El hilo principal vuelve inmediatamente a escuchar nuevas conexiones.
- **Cliente**: no cambia. La concurrencia es una decisión del servidor, transparente para el cliente.

## Por qué un pool y no un hilo por cliente

Crear un `Thread` nuevo por cada conexión funciona en clases pequeñas pero no escala: cada hilo consume memoria de pila y obliga al sistema operativo a hacer context switches. Un `ExecutorService` con tamaño fijo:

- Reutiliza hilos entre conexiones.
- Acota el paralelismo (en este ejemplo, 10 hilos simultáneos).
- Encola peticiones cuando todos los hilos están ocupados.

## Cómo ejecutar

```bash
# Terminal 1 — servidor
cd servidor/src
javac *.java
java ServidorCalculadora

# Terminales 2, 3, 4… — varios clientes en paralelo
cd cliente/src
javac ClienteCalculadora.java
java ClienteCalculadora
```

Para verificar que la concurrencia funciona, observe los logs del servidor: debería ver mensajes con nombres de hilo distintos (`pool-1-thread-1`, `pool-1-thread-2`, …) procesando al mismo tiempo.

## Limitaciones que persisten

La multihilo resolvió la escalabilidad, pero el protocolo sigue siendo texto crudo `operacion,a,b`. El cliente y el servidor todavía dependen de un acuerdo manual sobre el formato. La **Etapa 3 (RMI)** ataca ese problema: en lugar de enviar texto, llamamos métodos remotos como si fueran locales.
