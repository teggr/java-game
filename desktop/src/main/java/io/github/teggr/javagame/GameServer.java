package io.github.teggr.javagame;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class GameServer implements AutoCloseable {
    private static final Map<String, String> RESOURCES = Map.of(
            "/", "text/html; charset=utf-8",
            "/index.html", "text/html; charset=utf-8",
            "/styles.css", "text/css; charset=utf-8",
            "/bootstrap.js", "text/javascript; charset=utf-8",
            "/classes.js", "text/javascript; charset=utf-8");

    private final HttpServer server;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    GameServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::serve);
        server.setExecutor(executor);
        server.start();
    }

    URI uri() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/");
    }

    private void serve(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!uri().getAuthority().equals(exchange.getRequestHeaders().getFirst("Host"))) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            String method = exchange.getRequestMethod();
            if (!method.equals("GET") && !method.equals("HEAD")) {
                exchange.getResponseHeaders().set("Allow", "GET, HEAD");
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String path = exchange.getRequestURI().getRawPath();
            String contentType = RESOURCES.get(path);
            if (contentType == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            String resource = "/web" + (path.equals("/") ? "/index.html" : path);
            try (var input = GameServer.class.getResourceAsStream(resource)) {
                if (input == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
                byte[] content = input.readAllBytes();
                var headers = exchange.getResponseHeaders();
                headers.set("Content-Type", contentType);
                headers.set("X-Content-Type-Options", "nosniff");
                headers.set("Cache-Control", "no-store");
                headers.set("Content-Security-Policy",
                        "default-src 'self'; script-src 'self'; style-src 'self'; "
                                + "object-src 'none'; frame-ancestors 'none'; base-uri 'none'");
                if (method.equals("HEAD")) {
                    headers.set("Content-Length", Integer.toString(content.length));
                    exchange.sendResponseHeaders(200, -1);
                } else {
                    exchange.sendResponseHeaders(200, content.length);
                    exchange.getResponseBody().write(content);
                }
            }
        }
    }

    @Override
    public void close() {
        server.stop(0);
        executor.shutdownNow();
    }
}
