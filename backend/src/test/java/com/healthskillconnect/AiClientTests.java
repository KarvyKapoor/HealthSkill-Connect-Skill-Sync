package com.healthskillconnect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthskillconnect.service.AiClient;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AiClientTests {

    @Test
    void postForwardsJsonBodyToAiService() throws Exception {
        AtomicReference<String> receivedBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0),
                0
        );
        server.createContext("/notes", exchange -> {
            receivedBody.set(new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            ));
            byte[] response = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();

        try {
            AiClient client = new AiClient(
                    "http://127.0.0.1:" + server.getAddress().getPort(),
                    new ObjectMapper()
            );
            Map<String, Object> result = client.call(
                    "/notes",
                    Map.of("topic", "Patient safety")
            );

            assertEquals(Map.of("ok", true), result);
            assertTrue(receivedBody.get().contains("\"topic\":\"Patient safety\""));
        } finally {
            server.stop(0);
        }
    }
}
