package com.mayak.iet.integration.nativehost;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Native Messaging host entry point (desktop ↔ browser extension).
 * No UI.
 * No persistence.
 * Stateless except in-memory token.
 */

public class NativeHostMain {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TOKEN_URL = "http://127.0.0.1:38123/token";
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    public static void main(String[] args) {
        try {
            DataInputStream in = new DataInputStream(System.in);
            DataOutputStream out = new DataOutputStream(System.out);

            while (true) {
                int length;
                try {
                    length = Integer.reverseBytes(in.readInt());
                } catch (Exception e) {
                    return;
                }

                if (length <= 0 || length > 1_000_000) {
                    continue;
                }

                byte[] payload = new byte[length];
                in.readFully(payload);

                String message = new String(payload, StandardCharsets.UTF_8);
                Map<?, ?> request = MAPPER.readValue(message, Map.class);
                String type = (String) request.get("type");

                if (type == null) continue;

                switch (type) {
                    case "PING" -> write(out, Map.of("type", "PONG"));
                    case "GET_TOKEN" -> handleGetToken(out);
                    default -> write(out, Map.of("type", "ERROR", "message", "Unknown type: " + type
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static void handleGetToken(DataOutputStream out) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .GET()
                .timeout(Duration.ofMillis(500))
                .build();

        HttpResponse<String> response =
                HTTP.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 204) {
            write(out, Map.of("type", "NO_TOKEN"));
            return;
        }

        if (response.statusCode() != 200) {
            write(out, Map.of(
                    "type", "ERROR",
                    "message", "Bridge returned HTTP " + response.statusCode()
            ));
            return;
        }

        write(out, Map.of(
                "type", "TOKEN",
                "token", response.body()
        ));
    }

    private static void write(DataOutputStream out, Object msg) throws Exception {
        byte[] json = MAPPER.writeValueAsBytes(msg);
        out.writeInt(Integer.reverseBytes(json.length));
        out.write(json);
        out.flush();
    }
}