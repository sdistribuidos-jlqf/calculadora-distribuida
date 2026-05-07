import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ETAPA 2: Servidor de sockets multihilo.
 *
 * Diferencia clave con la Etapa 1: cada cliente es atendido en su propio
 * hilo, gracias a un ExecutorService con un pool de hilos. Esto permite
 * que N clientes interactúen con el servidor de forma concurrente.
 *
 * Por qué un pool y no `new Thread(...)`:
 *  - Un hilo por cliente sin límite consume memoria y agota recursos del
 *    sistema operativo bajo carga alta.
 *  - El pool reutiliza hilos y acota el paralelismo.
 *
 * El protocolo de aplicación sigue siendo texto plano "operacion,a,b" —
 * lo único que cambia es el modelo de concurrencia. Esto refuerza una idea
 * importante: la concurrencia y el protocolo son dimensiones distintas.
 */
public class ServidorCalculadora {

    private static final int PUERTO = 5000;
    private static final int MAX_HILOS = 10;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_HILOS);

        System.out.println("[Servidor] Iniciando en puerto " + PUERTO + " (multihilo, pool=" + MAX_HILOS + ")");

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (true) {
                Socket cliente = serverSocket.accept();
                System.out.println("[Servidor] Cliente conectado: " + cliente.getInetAddress()
                        + " — delegando a un hilo del pool");

                // En lugar de atender al cliente en el hilo principal,
                // delegamos a un Runnable que correrá en el pool.
                pool.submit(new HiloCliente(cliente));
            }
        } catch (Exception e) {
            System.err.println("[Servidor] Error: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }
}
