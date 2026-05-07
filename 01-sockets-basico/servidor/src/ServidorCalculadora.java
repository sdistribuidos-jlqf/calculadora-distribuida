import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ETAPA 1: Servidor de sockets básico (single-thread).
 *
 * Características:
 *  - Atiende UN cliente a la vez. Si llega un segundo cliente mientras se
 *    procesa el primero, debe esperar.
 *  - Protocolo de texto plano: el cliente envía "operacion,a,b" y el servidor
 *    responde con el resultado o un mensaje de error.
 *  - Operaciones soportadas: suma, resta, multiplicacion, division.
 *
 * Limitación principal: no escala. Es el punto de partida para discutir por
 * qué necesitamos concurrencia (Etapa 2) y por qué los protocolos ad-hoc
 * de texto se vuelven frágiles (motivación para RMI y JSON).
 */
public class ServidorCalculadora {

    private static final int PUERTO = 5000;

    public static void main(String[] args) {
        System.out.println("[Servidor] Iniciando en puerto " + PUERTO + "...");

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("[Servidor] Esperando conexiones (modo single-thread)...");

            while (true) {
                // accept() bloquea hasta que llega un cliente.
                Socket cliente = serverSocket.accept();
                System.out.println("[Servidor] Cliente conectado: " + cliente.getInetAddress());

                // Toda la atención del cliente ocurre en el hilo principal.
                // Mientras este método no termine, ningún otro cliente será atendido.
                atenderCliente(cliente);
            }
        } catch (Exception e) {
            System.err.println("[Servidor] Error: " + e.getMessage());
        }
    }

    private static void atenderCliente(Socket cliente) {
        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true)
        ) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                System.out.println("[Servidor] Recibido: " + linea);
                String respuesta = procesar(linea);
                salida.println(respuesta);

                if (linea.equalsIgnoreCase("salir")) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[Servidor] Error con cliente: " + e.getMessage());
        } finally {
            try { cliente.close(); } catch (Exception ignored) {}
            System.out.println("[Servidor] Cliente desconectado.");
        }
    }

    /**
     * Protocolo: "operacion,a,b"
     * Ejemplos: "suma,3,4"  ->  "7.0"
     *           "division,10,0" -> "ERROR: division entre cero"
     */
    private static String procesar(String mensaje) {
        if (mensaje.equalsIgnoreCase("salir")) {
            return "ADIOS";
        }

        String[] partes = mensaje.split(",");
        if (partes.length != 3) {
            return "ERROR: formato invalido. Use 'operacion,a,b'";
        }

        try {
            String op = partes[0].trim().toLowerCase();
            double a = Double.parseDouble(partes[1].trim());
            double b = Double.parseDouble(partes[2].trim());

            switch (op) {
                case "suma":           return String.valueOf(a + b);
                case "resta":          return String.valueOf(a - b);
                case "multiplicacion": return String.valueOf(a * b);
                case "division":
                    if (b == 0) return "ERROR: division entre cero";
                    return String.valueOf(a / b);
                default:
                    return "ERROR: operacion desconocida";
            }
        } catch (NumberFormatException e) {
            return "ERROR: los operandos deben ser numeros";
        }
    }
}
