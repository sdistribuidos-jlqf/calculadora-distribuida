import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * ETAPA 1: Cliente de sockets básico.
 *
 * Se conecta al servidor, lee operaciones desde la consola del usuario y
 * las envía con el formato "operacion,a,b". Imprime la respuesta del servidor.
 *
 * Escriba "salir" para cerrar la conexión.
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
