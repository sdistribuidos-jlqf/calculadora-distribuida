package com.calculadora.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * ETAPA 3: Interfaz remota.
 *
 * Este es el "contrato" que comparten cliente y servidor en RMI.
 * Cualquier interfaz remota debe:
 *  - Extender java.rmi.Remote.
 *  - Declarar que cada método puede lanzar RemoteException, porque la
 *    invocación viaja por la red y puede fallar por motivos de red,
 *    serialización, etc.
 *
 * Diferencia conceptual con sockets:
 *  - Con sockets el "contrato" es un formato de mensaje (texto, JSON, etc.)
 *    que cliente y servidor deben acordar e implementar manualmente.
 *  - Con RMI el contrato ES esta interfaz Java. El compilador verifica
 *    los tipos. La llamada remota se ve igual que una llamada local.
 */
public interface CalculadoraRemota extends Remote {

    double sumar(double a, double b) throws RemoteException;

    double restar(double a, double b) throws RemoteException;

    double multiplicar(double a, double b) throws RemoteException;

    double dividir(double a, double b) throws RemoteException;
}
