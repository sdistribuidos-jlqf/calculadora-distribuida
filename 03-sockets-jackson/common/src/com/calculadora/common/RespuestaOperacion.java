package com.calculadora.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO de respuesta. Lleva el resultado (cuando éxito = true) o un mensaje
 * de error (cuando éxito = false).
 *
 * Modelo unificado de "éxito | error" en lugar de tener dos clases o
 * de mezclar canales (datos válidos vs cadena "ERROR:..." en el texto
 * plano de la Etapa 2). Esto facilita el manejo en el cliente: una sola
 * forma para procesar, una sola forma para verificar.
 *
 * @JsonIgnoreProperties(ignoreUnknown = true) hace al cliente tolerante
 * a campos extra que pudieran añadirse en el futuro. Si el servidor
 * comienza a devolver, por ejemplo, "tiempoMs", los clientes viejos
 * siguen funcionando sin recompilar.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespuestaOperacion {

    private boolean exito;
    private double resultado;
    private String error;

    public RespuestaOperacion() {}

    public static RespuestaOperacion ok(double resultado) {
        RespuestaOperacion r = new RespuestaOperacion();
        r.exito = true;
        r.resultado = resultado;
        return r;
    }

    public static RespuestaOperacion error(String mensaje) {
        RespuestaOperacion r = new RespuestaOperacion();
        r.exito = false;
        r.error = mensaje;
        return r;
    }

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }

    public double getResultado() { return resultado; }
    public void setResultado(double resultado) { this.resultado = resultado; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
