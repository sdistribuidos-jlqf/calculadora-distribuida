package com.calculadora.cliente;

import com.calculadora.common.RespuestaOperacion;
import com.calculadora.common.SolicitudOperacion;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * ETAPA 3: Cliente que envía objetos como JSON sobre un socket TCP.
 *
 * Flujo de un envío:
 *  1. El usuario escribe "suma 3 4" en la consola.
 *  2. Construimos un SolicitudOperacion (POJO).
 *  3. Jackson lo convierte a String JSON: {"operacion":"suma","a":3,"b":4}.
 *  4. Lo enviamos por el socket como una línea más '\n'.
 *  5. Leemos la respuesta (otra línea JSON).
 *  6. Jackson la convierte de vuelta a un POJO RespuestaOperacion.
 *  7. Mostramos resultado o error al usuario.
 *
 * Esta es la primera etapa donde el código de aplicación NO toca el
 * formato del mensaje. Jackson es la "herramienta externa" que nos
 * libera de hacer parseo manual — la pista directa de por qué los
 * sistemas distribuidos modernos casi siempre dependen de bibliotecas
 * de serialización.
 */
public class ClienteCalculadora {

    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();

        try (
            Socket socket = new Socket(HOST, PUERTO);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner consola = new Scanner(System.in)
        ) {
            System.out.println("[Cliente] Conectado a " + HOST + ":" + PUERTO);
            System.out.println("Formato: operacion a b   (ej: suma 3 4)");
            System.out.println("Operaciones: suma, resta, multiplicacion, division");
            System.out.println("Escriba 'salir' para terminar.\n");

            while (true) {
                System.out.print("> ");
                String linea = consola.nextLine().trim();
                if (linea.isEmpty()) continue;
                if (linea.equalsIgnoreCase("salir")) {
                    salida.println("salir");
                    break;
                }

                String[] partes = linea.split("\\s+");
                if (partes.length != 3) {
                    System.out.println("Formato invalido. Use: operacion a b");
                    continue;
                }

                try {
                    SolicitudOperacion req = new SolicitudOperacion(
                            partes[0].toLowerCase(),
                            Double.parseDouble(partes[1]),
                            Double.parseDouble(partes[2])
                    );

                    // POJO -> JSON. Esta es la línea clave de la etapa.
                    String jsonEnviado = mapper.writeValueAsString(req);
                    salida.println(jsonEnviado);
                    System.out.println("  enviado:  " + jsonEnviado);

                    // Recibir respuesta.
                    String jsonRecibido = entrada.readLine();
                    if (jsonRecibido == null) {
                        System.out.println("Conexion cerrada por el servidor.");
                        break;
                    }
                    System.out.println("  recibido: " + jsonRecibido);

                    // JSON -> POJO.
                    RespuestaOperacion resp = mapper.readValue(jsonRecibido, RespuestaOperacion.class);

                    if (resp.isExito()) {
                        System.out.println("Resultado: " + resp.getResultado());
                    } else {
                        System.out.println("Error del servidor: " + resp.getError());
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Operandos invalidos.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[Cliente] Error: " + e.getMessage());
        }
    }
}
