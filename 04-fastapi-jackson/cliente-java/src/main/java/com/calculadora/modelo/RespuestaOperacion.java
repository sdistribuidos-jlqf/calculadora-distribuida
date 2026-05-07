package com.calculadora.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO que representa la respuesta exitosa del servidor.
 *
 * @JsonIgnoreProperties(ignoreUnknown = true) hace que el cliente sea
 * tolerante a cambios en el servidor: si en el futuro el servidor añade
 * campos nuevos, este cliente seguirá funcionando sin lanzar excepciones.
 * Es un patrón de robustez muy útil en sistemas distribuidos donde
 * cliente y servidor evolucionan a ritmos distintos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespuestaOperacion {

    private String operacion;
    private double a;
    private double b;
    private double resultado;

    public RespuestaOperacion() {}

    public String getOperacion() { return operacion; }
    public void setOperacion(String operacion) { this.operacion = operacion; }

    public double getA() { return a; }
    public void setA(double a) { this.a = a; }

    public double getB() { return b; }
    public void setB(double b) { this.b = b; }

    public double getResultado() { return resultado; }
    public void setResultado(double resultado) { this.resultado = resultado; }

    @Override
    public String toString() {
        return operacion + "(" + a + ", " + b + ") = " + resultado;
    }
}
