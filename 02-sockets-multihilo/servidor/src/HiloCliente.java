import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Worker que atiende a un cliente concreto en un hilo del pool.
 *
 * Cada instancia es independiente: tiene su propio Socket y sus propios
 * streams. Esto significa que dos clientes simultáneos no comparten estado
 * y no se bloquean entre sí.
 */
public class HiloCliente implements Runnable {

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
                System.out.println("[" + idHilo + "] recibido: " + linea);
                String respuesta = procesar(linea);
                salida.println(respuesta);

                if (linea.equalsIgnoreCase("salir")) break;
            }
        } catch (Exception e) {
            System.err.println("[" + idHilo + "] error: " + e.getMessage());
        } finally {
            try { cliente.close(); } catch (Exception ignored) {}
            System.out.println("[" + idHilo + "] cliente desconectado.");
        }
    }

    private String procesar(String mensaje) {
        if (mensaje.equalsIgnoreCase("salir")) return "ADIOS";

        String[] partes = mensaje.split(",");
        if (partes.length != 3) return "ERROR: formato invalido. Use 'operacion,a,b'";

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
