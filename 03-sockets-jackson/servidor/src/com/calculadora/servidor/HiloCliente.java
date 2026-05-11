package com.calculadora.servidor;

import com.calculadora.common.RespuestaOperacion;
import com.calculadora.common.SolicitudOperacion;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Worker que atiende a un cliente concreto.
 *
 * Protocolo de aplicación:
 *  - Una línea JSON por mensaje (delimitada por '\n').
 *  - Cliente envía:   {"operacion":"suma","a":3,"b":4}
 *  - Servidor responde: {"exito":true,"resultado":7.0,"error":null}
 *                      o {"exito":false,"resultado":0.0,"error":"..."}.
 *  - Línea "salir" (texto plano) cierra la sesión.
 *
 * Por qué JSON-por-línea y no, por ejemplo, ObjectOutputStream nativo:
 *  - JSON es portable: un cliente Python, JavaScript o curl podría
 *    hablar con este servidor. Eso ya no sería cierto si usáramos
 *    serialización binaria propia de Java.
 *  - Es legible: depurar el protocolo con tcpdump o wireshark se vuelve
 *    trivial.
 *  - Prepara el camino para HTTP+JSON de la Etapa 5: el formato es el
 *    mismo, solo cambia el transporte.
 *
 * Note el patrón "readValue / writeValue" de Jackson — eso es todo lo
 * que el código de aplicación necesita saber. La librería se encarga
 * de tipos, escapado, formato de números, etc.
 */
public class HiloCliente implements Runnable {

    // ObjectMapper es thread-safe; podemos compartirlo entre todos los hilos.
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Socket cliente;

    public HiloCliente(Socket cliente) {
        this.cliente = cliente;
    }

    @Override
    public void run() {
        String idHilo = Thread.currentThread().getName();
        System.out.println("[" + idHilo + "] atendiendo a " + cliente.getInetAddress());

        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true)
        ) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                if (linea.equalsIgnoreCase("salir")) {
                    System.out.println("[" + idHilo + "] cliente solicito cerrar.");
                    break;
                }

                System.out.println("[" + idHilo + "] JSON recibido: " + linea);
                RespuestaOperacion respuesta = procesar(linea);
                String jsonRespuesta = mapper.writeValueAsString(respuesta);

                System.out.println("[" + idHilo + "] JSON enviado:  " + jsonRespuesta);
                salida.println(jsonRespuesta);
            }
        } catch (Exception e) {
            System.err.println("[" + idHilo + "] error: " + e.getMessage());
        } finally {
            try { cliente.close(); } catch (Exception ignored) {}
            System.out.println("[" + idHilo + "] cliente desconectado.");
        }
    }

    private RespuestaOperacion procesar(String json) {
        SolicitudOperacion req;
        try {
            // JSON -> objeto Java. Si el JSON está malformado o le faltan
            // campos obligatorios, Jackson lanza una excepción.
            req = mapper.readValue(json, SolicitudOperacion.class);
        } catch (Exception e) {
            return RespuestaOperacion.error("JSON invalido: " + e.getMessage());
        }

        if (req.getOperacion() == null) {
            return RespuestaOperacion.error("Falta el campo 'operacion'");
        }

        switch (req.getOperacion().toLowerCase()) {
            case "suma":           return RespuestaOperacion.ok(req.getA() + req.getB());
            case "resta":          return RespuestaOperacion.ok(req.getA() - req.getB());
            case "multiplicacion": return RespuestaOperacion.ok(req.getA() * req.getB());
            case "division":
                if (req.getB() == 0) {
                    return RespuestaOperacion.error("Division entre cero");
                }
                return RespuestaOperacion.ok(req.getA() / req.getB());
            default:
                return RespuestaOperacion.error("Operacion desconocida: " + req.getOperacion());
        }
    }
}
