package com.calculadora.servidor;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ETAPA 3: Servidor de sockets multihilo + Jackson.
 *
 * El modelo de concurrencia es idéntico al de la Etapa 2: un pool de
 * hilos que atiende N clientes en paralelo. Lo que cambia es el
 * protocolo de aplicación: en lugar de texto crudo "operacion,a,b",
 * los mensajes son objetos Java serializados a JSON con Jackson.
 *
 * Esta etapa muestra que la concurrencia y el formato del mensaje son
 * dimensiones ortogonales: podemos mejorar una sin tocar la otra.
 */
public class ServidorCalculadora {

    private static final int PUERTO = 5000;
    private static final int MAX_HILOS = 10;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_HILOS);

        System.out.println("[Servidor] Iniciando en puerto " + PUERTO
                + " (multihilo + Jackson, pool=" + MAX_HILOS + ")");

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (true) {
                Socket cliente = serverSocket.accept();
                System.out.println("[Servidor] Cliente conectado: " + cliente.getInetAddress());
                pool.submit(new HiloCliente(cliente));
            }
        } catch (Exception e) {
            System.err.println("[Servidor] Error: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }
}
