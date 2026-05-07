package com.calculadora.servidor;

import com.calculadora.common.CalculadoraRemota;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Servidor RMI.
 *
 * Pasos:
 *  1. Crear una instancia del servicio remoto.
 *  2. Crear (o localizar) el registro RMI en un puerto conocido.
 *  3. Publicar el objeto bajo un nombre lógico ("CalculadoraService").
 *
 * El registro funciona como un "directorio telefónico" donde los clientes
 * buscan referencias remotas por nombre. Una vez publicado el objeto,
 * el servidor queda esperando invocaciones — RMI maneja los hilos por
 * detrás (el modelo es similar al pool multihilo de la Etapa 2, pero
 * implícito).
 */
public class ServidorRMI {

    public static final int PUERTO_REGISTRY = 1099;
    public static final String NOMBRE_SERVICIO = "CalculadoraService";

    public static void main(String[] args) {
        try {
            CalculadoraRemota servicio = new CalculadoraImpl();

            Registry registry = LocateRegistry.createRegistry(PUERTO_REGISTRY);
            registry.rebind(NOMBRE_SERVICIO, servicio);

            System.out.println("[Servidor RMI] Servicio '" + NOMBRE_SERVICIO
                    + "' publicado en el registro (puerto " + PUERTO_REGISTRY + ").");
            System.out.println("[Servidor RMI] Esperando invocaciones remotas...");
            // El proceso queda vivo gracias al hilo no-daemon que mantiene
            // el registro y el objeto exportado.
        } catch (Exception e) {
            System.err.println("[Servidor RMI] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
