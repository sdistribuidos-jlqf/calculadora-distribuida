package com.calculadora.modelo;

/**
 * POJO que representa una petición de operación.
 *
 * Jackson convertirá automáticamente esta clase a JSON con la forma:
 *   { "operacion": "suma", "a": 3.0, "b": 4.0 }
 *
 * Los nombres de los campos coinciden con los del servidor FastAPI.
 * Si no coincidieran, usaríamos la anotación @JsonProperty para
 * mapearlos.
 */
public class SolicitudOperacion {

    private String operacion;
    private double a;
    private double b;

    public SolicitudOperacion() { /* Jackson necesita el constructor sin argumentos */ }

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
