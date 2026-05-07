package com.calculadora.servidor;

import com.calculadora.common.CalculadoraRemota;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementación del servicio remoto.
 *
 * Al extender UnicastRemoteObject, esta clase queda automáticamente
 * "exportada": al instanciarla, RMI crea el stub que recibirá las
 * invocaciones remotas.
 *
 * Toda la complejidad de serializar argumentos, enviarlos por la red,
 * deserializarlos en el otro extremo, ejecutar el método y devolver el
 * resultado la oculta el runtime de RMI. Para nosotros es una clase
 * Java normal.
 */
public class CalculadoraImpl extends UnicastRemoteObject implements CalculadoraRemota {

    public CalculadoraImpl() throws RemoteException {
        super();
    }

    @Override
    public double sumar(double a, double b) {
        log("sumar", a, b);
        return a + b;
    }

    @Override
    public double restar(double a, double b) {
        log("restar", a, b);
        return a - b;
    }

    @Override
    public double multiplicar(double a, double b) {
        log("multiplicar", a, b);
        return a * b;
    }

    @Override
    public double dividir(double a, double b) {
        log("dividir", a, b);
        if (b == 0) {
            // Las excepciones también viajan: el cliente las recibirá
            // envueltas en una RemoteException o ServerException.
            throw new ArithmeticException("Division entre cero");
        }
        return a / b;
    }

    private void log(String op, double a, double b) {
        System.out.println("[Servidor] " + op + "(" + a + ", " + b + ") solicitado");
    }
}
