package com.calculadora.common;

/**
 * DTO (Data Transfer Object) que representa una petición de operación.
 *
 * Esta clase es el "contrato" entre cliente y servidor en la Etapa 3.
 * Ambos lados la usan: el cliente la serializa a JSON antes de enviarla,
 * el servidor la deserializa al recibirla. Como es un POJO simple,
 * Jackson sabe convertirla a/desde JSON sin configuración adicional.
 *
 * Comparado con la Etapa 2 (texto plano "operacion,a,b"):
 *  - Aquí los nombres de los campos viajan dentro del JSON, así que un
 *    error de orden ya no rompe nada.
 *  - Si añadimos un campo nuevo (por ejemplo "precision"), los clientes
 *    viejos pueden ignorarlo gracias a @JsonIgnoreProperties (ver
 *    RespuestaOperacion).
 *  - El tipado de cada campo es explícito: ya no parseamos strings a mano.
 */
public class SolicitudOperacion {

    private String operacion;
    private double a;
    private double b;

    // Jackson requiere un constructor sin argumentos para deserializar.
    public SolicitudOperacion() {}

    public SolicitudOperacion(String operacion, double a, double b) {
        this.operacion = operacion;
        this.a = a;
        this.b = b;
    }

    public String getOperacion() { return operacion; }
    public void setOperacion(String operacion) { this.operacion = operacion; }

    public double getA() { return a; }
    public void setA(double a) { this.a = a; }

    public double getB() { return b; }
    public void setB(double b) { this.b = b; }
}
