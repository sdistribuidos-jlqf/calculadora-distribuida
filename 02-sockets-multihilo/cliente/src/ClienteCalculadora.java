import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * ETAPA 2: Cliente.
 *
 * El cliente es prácticamente idéntico al de la Etapa 1 — eso es
 * intencional. La concurrencia se introdujo en el SERVIDOR, no en el
 * protocolo. El cliente no se entera del cambio.
 *
 * Para demostrar la concurrencia: arranque el servidor y luego ejecute
 * varias instancias de este cliente en terminales distintas. Las
 * peticiones se atienden en paralelo (cada una en un hilo del pool).
 */
public class ClienteCalculadora {

    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(HOST, PUERTO);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner consola = new Scanner(System.in)
        ) {
            System.out.println("[Cliente] Conectado a " + HOST + ":" + PUERTO);
            System.out.println("Formato: operacion,a,b");
            System.out.println("Operaciones: suma, resta, multiplicacion, division");
            System.out.println("Escriba 'salir' para terminar.\n");

            while (true) {
                System.out.print("> ");
                String mensaje = consola.nextLine();
                if (mensaje.isBlank()) continue;

                salida.println(mensaje);
                String respuesta = entrada.readLine();
                System.out.println("[Servidor] " + respuesta);

                if (mensaje.equalsIgnoreCase("salir")) break;
            }
        } catch (Exception e) {
            System.err.println("[Cliente] Error: " + e.getMessage());
        }
    }
}
