package com.healthskillconnect.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiClient {

    private static final Logger log = LoggerFactory.getLogger(AiClient.class);
    private static final TypeReference<Map<String, Object>> JSON_MAP =
            new TypeReference<>() {};

    private final String base;
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public AiClient(
            @Value("${app.ai-base-url}") String base,
            ObjectMapper objectMapper
    ) {
        this.base = base.replaceAll("/+$", "");
        this.client = HttpClient.newBuilder()
                // The FastAPI/Uvicorn service does not receive HTTP/2 request bodies reliably.
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> call(String path, Map<String, Object> body) {
        String endpoint = base + path;
        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException(
                    "AI request body must not be null or empty."
            );
        }

        log.info("Calling AI service: {}", endpoint);
        log.debug("AI request fields: {}", body.keySet());

        HttpRequest request = jsonRequest(endpoint, body, "application/json");
        HttpResponse<String> response = send(request, endpoint);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.error(
                    "AI service returned HTTP {} for {}. Response: {}",
                    response.statusCode(),
                    endpoint,
                    response.body()
            );
            throw new IllegalStateException(
                    "AI service returned HTTP "
                            + response.statusCode()
                            + ": "
                            + response.body()
            );
        }

        try {
            Map<String, Object> result = objectMapper.readValue(
                    response.body(),
                    JSON_MAP
            );
            if (result == null) {
                throw new IllegalStateException(
                        "AI service returned an empty response."
                );
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "AI service returned an invalid JSON response.",
                    e
            );
        }
    }

    public Map<String, Object> get(String path) {
        String endpoint = base + path;
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = send(request, endpoint);

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.error(
                    "AI GET returned HTTP {} for {}. Response: {}",
                    response.statusCode(),
                    endpoint,
                    response.body()
            );
            throw new IllegalStateException(
                    "AI service returned HTTP "
                            + response.statusCode()
                            + ": "
                            + response.body()
            );
        }

        try {
            Map<String, Object> result = objectMapper.readValue(
                    response.body(),
                    JSON_MAP
            );
            if (result == null) {
                throw new IllegalStateException(
                        "AI service returned an empty response."
                );
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "AI service returned an invalid JSON response.",
                    e
            );
        }
    }

    public byte[] presentation(Map<String, Object> body) {
        String endpoint = base + "/presentation";
        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException(
                    "Presentation request body must not be null or empty."
            );
        }

        HttpRequest request = jsonRequest(
                endpoint,
                body,
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        );

        HttpResponse<byte[]> response;
        try {
            response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Presentation request was interrupted.",
                    e
            );
        } catch (IOException e) {
            log.error("Presentation service connection failed at {}", endpoint, e);
            throw new IllegalStateException(
                    "Could not communicate with the AI service for presentation generation.",
                    e
            );
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorBody = new String(
                    response.body(),
                    StandardCharsets.UTF_8
            );
            log.error(
                    "Presentation request returned HTTP {}: {}",
                    response.statusCode(),
                    errorBody
            );
            throw new IllegalStateException(
                    "Presentation generation failed with HTTP "
                            + response.statusCode()
                            + ": "
                            + errorBody
            );
        }
        return response.body();
    }

    private HttpRequest jsonRequest(
            String endpoint,
            Map<String, Object> body,
            String accept
    ) {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Could not serialize the AI request.",
                    e
            );
        }
        return HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Accept", accept)
                .POST(HttpRequest.BodyPublishers.ofString(
                        json,
                        StandardCharsets.UTF_8
                ))
                .build();
    }

    private HttpResponse<String> send(
            HttpRequest request,
            String endpoint
    ) {
        try {
            return client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8
                    )
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "AI request was interrupted for " + endpoint,
                    e
            );
        } catch (IOException e) {
            log.error("Failed to call AI service at {}", endpoint, e);
            throw new IllegalStateException(
                    "Could not communicate with the AI service at " + endpoint,
                    e
            );
        }
    }
}
