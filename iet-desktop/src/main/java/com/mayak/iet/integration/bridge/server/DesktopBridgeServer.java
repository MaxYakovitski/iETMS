package com.mayak.iet.integration.bridge.server;

import com.mayak.iet.integration.auth.AuthState;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
public class DesktopBridgeServer {
    private final AuthState authState;
    private HttpServer server;

    public DesktopBridgeServer(AuthState authState) {
        this.authState = authState;
    }

    public void start() {
        try {
            server = HttpServer.create(
                    new InetSocketAddress("127.0.0.1", 38123), 0);

            server.createContext("/token", exchange -> {
                if (!"GET".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                String token = authState.getToken();
                if (token == null) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                byte[] body = token.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            });

            server.start();
            log.info("[bridge] Desktop token bridge started on 127.0.0.1:38123");

        } catch (Exception e) {
            throw new IllegalStateException("Failed to start desktop bridge", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}