# Etapa 1 — Sockets básicos (single-thread)

Punto de partida histórico: comunicación cliente-servidor mediante **sockets TCP** crudos. El servidor atiende un solo cliente a la vez.

## Protocolo

Texto plano sobre TCP. El cliente envía una línea con el formato:

```
operacion,a,b
```

Operaciones válidas: `suma`, `resta`, `multiplicacion`, `division`. Para terminar la sesión, el cliente envía `salir`.

Ejemplo de intercambio:

```
Cliente -> suma,3,4
Servidor -> 7.0
Cliente -> division,10,0
Servidor -> ERROR: division entre cero
```

## Cómo ejecutar

Necesita Java 11 o superior.

En una terminal, compile y arranque el servidor:

```bash
cd servidor/src
javac ServidorCalculadora.java
java ServidorCalculadora
```

En otra terminal, compile y arranque el cliente:

```bash
cd cliente/src
javac ClienteCalculadora.java
java ClienteCalculadora
```

## Limitaciones (motivación para las siguientes etapas)

- **Un solo cliente a la vez.** Si un segundo cliente intenta conectarse mientras el primero está activo, queda en espera. Esto motiva la **Etapa 2: sockets multihilo**.
- **Protocolo frágil.** Cualquier cambio en el formato del mensaje rompe a todos los clientes. No hay tipado: todo es texto. Esto motiva **RMI** (Etapa 3) y **JSON con Jackson** (Etapa 4).
- **Acoplamiento al lenguaje implícito.** El cliente y el servidor deben acordar manualmente el formato. Si quisiéramos un cliente en Python, habría que reimplementar el parseo.
