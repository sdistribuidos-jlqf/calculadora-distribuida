package com.calculadora.cliente;

import com.calculadora.modelo.RespuestaOperacion;
import com.calculadora.modelo.SolicitudOperacion;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

/**
 * ETAPA 4: Cliente Java que consume el servicio REST de FastAPI.
 *
 * Componentes:
 *  - java.net.http.HttpClient (incluido desde Java 11) para realizar
 *    peticiones HTTP. Sustituye al viejo HttpURLConnection.
 *  - Jackson (com.fasterxml.jackson.databind.ObjectMapper) para convertir
 *    objetos Java a JSON (serializar) y JSON a objetos Java (deserializar).
 *
 * Por qué Jackson:
 *  Java no trae soporte nativo de JSON en su biblioteca estándar. Sin una
 *  herramienta como Jackson tendríamos que construir el JSON manualmente
 *  con concatenación de strings — exactamente el problema que tenían los
 *  sockets de la Etapa 1, pero peor. Jackson resuelve esto con dos
 *  llamadas: writeValueAsString() y readValue().
 *
 * Esto demuestra una idea importante: a medida que los protocolos se
 * vuelven más estándar (HTTP+JSON), aparecen herramientas externas
 * (Jackson, Gson, OkHttp, Retrofit, etc.) que simplifican la integración.
 */
public class ClienteFastAPI {

    private static final String URL_BASE = "http://localhost:8000";
    private static final String ENDPOINT_OPERAR = URL_BASE + "/operar";

    // ObjectMapper es thread-safe y costoso de crear: se instancia una sola vez.
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {
        System.out.println("[Cliente FastAPI] Apuntando a " + URL_BASE);
        System.out.println("Operaciones: suma, resta, multiplicacion, division");
        System.out.println("Formato: operacion a b   (escriba 'salir' para terminar)\n");

        try (Scanner consola = new Scanner(System.in)) {
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
                    SolicitudOperacion solicitud = new SolicitudOperacion(
                            partes[0].toLowerCase(),
                            Double.parseDouble(partes[1]),
                            Double.parseDouble(partes[2])
                    );
                    RespuestaOperacion respuesta = enviar(solicitud);
                    System.out.println("Resultado: " + respuesta.getResultado());
                } catch (NumberFormatException e) {
                    System.out.println("Operandos invalidos.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Envía la solicitud al servidor y devuelve la respuesta deserializada.
     *
     * Pasos:
     *  1. Serializar el POJO a JSON con Jackson.
     *  2. Construir una petición HTTP POST con ese JSON como cuerpo.
     *  3. Enviar y obtener la respuesta.
     *  4. Si el código HTTP es 2xx, deserializar la respuesta a un POJO.
     *     Si no, extraer el mensaje de error.
     */
    private static RespuestaOperacion enviar(SolicitudOperacion solicitud) throws Exception {
        // 1. Java -> JSON
        String cuerpoJson = mapper.writeValueAsString(solicitud);

        // 2. Construir petición HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT_OPERAR))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpoJson))
                .build();

        // 3. Enviar
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 4. Procesar
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return mapper.readValue(response.body(), RespuestaOperacion.class);
        } else {
            // FastAPI devuelve los errores como JSON: {"detail": "..."}
            // Aquí lo simplificamos extrayendo el cuerpo crudo.
            throw new RuntimeException("HTTP " + response.statusCode() + " - " + response.body());
        }
    }
}
