package com.calculadora.cliente;

import com.calculadora.common.CalculadoraRemota;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * Cliente RMI.
 *
 * El cliente:
 *  1. Localiza el registro RMI del servidor.
 *  2. Pide la referencia al servicio por su nombre lógico.
 *  3. Invoca métodos remotos como si fueran locales.
 *
 * Note la diferencia con sockets: no hay parseo de strings, no hay
 * acuerdo manual sobre el formato del mensaje, y los errores de tipo
 * los detecta el compilador. El "stub" devuelto por lookup() tiene la
 * misma forma que la interfaz CalculadoraRemota.
 */
public class ClienteRMI {

    private static final String HOST = "localhost";
    private static final int PUERTO_REGISTRY = 1099;
    private static final String NOMBRE_SERVICIO = "CalculadoraService";

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(HOST, PUERTO_REGISTRY);
            CalculadoraRemota calc = (CalculadoraRemota) registry.lookup(NOMBRE_SERVICIO);

            System.out.println("[Cliente RMI] Conectado al servicio '" + NOMBRE_SERVICIO + "'.");
            System.out.println("Operaciones: suma, resta, multiplicacion, division");
            System.out.println("Formato: operacion a b   (escriba 'salir' para terminar)\n");

            Scanner consola = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                String linea = consola.nextLine().trim();
                if (linea.isEmpty()) continue;
                if (linea.equalsIgnoreCase("salir")) break;

                String[] partes = linea.split("\\s+");
                if (partes.length != 3) {
                    System.out.println("Formato invalido. Use: operacion a b");
                    continue;
                }

                try {
                    double a = Double.parseDouble(partes[1]);
                    double b = Double.parseDouble(partes[2]);
                    double resultado;

                    switch (partes[0].toLowerCase()) {
                        case "suma":           resultado = calc.sumar(a, b); break;
                        case "resta":          resultado = calc.restar(a, b); break;
                        case "multiplicacion": resultado = calc.multiplicar(a, b); break;
                        case "division":       resultado = calc.dividir(a, b); break;
                        default:
                            System.out.println("Operacion desconocida.");
                            continue;
                    }

                    System.out.println("Resultado: " + resultado);
                } catch (NumberFormatException e) {
                    System.out.println("Operandos invalidos.");
                } catch (Exception e) {
                    // Aquí caen tanto las excepciones de negocio (división entre
                    // cero) como las de red.
                    System.out.println("Error remoto: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[Cliente RMI] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
