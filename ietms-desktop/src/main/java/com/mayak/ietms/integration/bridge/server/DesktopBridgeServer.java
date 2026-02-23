package com.mayak.ietms.integration.bridge.server;

import com.mayak.ietms.integration.auth.AuthState;
import com.sun.net.httpserver.HttpServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class DesktopBridgeServer {

    private static final int PORT = 38123;
    private final AuthState authState;
    private HttpServer server;

    public DesktopBridgeServer(AuthState authState) {
        this.authState = authState;
    }

    @PostConstruct
    public void start() {
        tryStart(3);
    }

    private void tryStart(int retries) {
        for (int i = 1; i <= retries; i++) {
            try {
                server = HttpServer.create(
                        new InetSocketAddress("127.0.0.1", PORT), 0);

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
                log.info("[bridge] Started on port {}", PORT);
                return;

            } catch (BindException e) {
                log.warn("[bridge] Port busy, retry {}/{}", i, retries);
                sleep(1500);
            } catch (Exception e) {
                log.error("[bridge] Unexpected error", e);
                return;
            }
        }

        log.error("[bridge] Failed to start after retries. Disabled.");
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            log.info("[bridge] Stopping");
            server.stop(0);
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}