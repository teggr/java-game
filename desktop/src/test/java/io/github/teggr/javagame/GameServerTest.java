package io.github.teggr.javagame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class GameServerTest {
    private GameServer server;
    private HttpClient client;

    @BeforeEach
    void start() throws IOException {
        server = new GameServer();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void stop() {
        client.close();
        server.close();
    }

    @Test
    void servesGamePageAndAllBrowserAssets() throws Exception {
        var page = get("");
        assertEquals(200, page.statusCode());
        assertTrue(page.body().contains("game-canvas"));
        assertEquals("text/html; charset=utf-8", page.headers().firstValue("Content-Type").orElseThrow());
        assertTrue(page.headers().firstValue("Content-Security-Policy").orElseThrow().contains("script-src 'self'"));
        assertEquals("nosniff", page.headers().firstValue("X-Content-Type-Options").orElseThrow());
        assertEquals(200, get("index.html").statusCode());
        assertEquals(200, get("styles.css").statusCode());
        assertEquals("main();", get("bootstrap.js").body().trim());
        var javascript = get("classes.js");
        assertEquals(200, javascript.statusCode());
        assertFalse(javascript.body().isBlank());
        assertTrue(javascript.headers().firstValue("Content-Type").orElseThrow().startsWith("text/javascript"));
    }

    @Test
    void supportsHeadWithoutReturningBody() throws Exception {
        var response = client.send(HttpRequest.newBuilder(server.uri()).method("HEAD",
                        HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("", response.body());
        assertTrue(Integer.parseInt(response.headers().firstValue("Content-Length").orElseThrow()) > 0);
    }

    @Test
    void rejectsNonReadMethods() throws Exception {
        var response = client.send(HttpRequest.newBuilder(server.uri()).POST(
                HttpRequest.BodyPublishers.ofString("ignored")).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode());
        assertEquals("GET, HEAD", response.headers().firstValue("Allow").orElseThrow());
    }

    @Test
    void exposesOnlyExplicitGameResources() throws Exception {
        for (String path : new String[]{"missing", "../pom.xml", "%2e%2e/pom.xml",
                "META-INF/MANIFEST.MF", "io/github/teggr/javagame/DesktopApp.class"}) {
            assertEquals(404, get(path).statusCode(), path);
        }
    }

    @Test
    void rejectsForeignHostHeader() throws Exception {
        try (Socket socket = new Socket("127.0.0.1", server.uri().getPort())) {
            socket.setSoTimeout(5000);
            socket.getOutputStream().write(("GET / HTTP/1.1\r\nHost: attacker.invalid\r\n"
                    + "Connection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
            assertTrue(new String(socket.getInputStream().readAllBytes(), StandardCharsets.US_ASCII)
                    .startsWith("HTTP/1.1 403"));
        }
    }

    @Test
    void usesIndependentEphemeralPortsAndClosesCleanly() throws Exception {
        try (GameServer other = new GameServer()) {
            assertNotEquals(server.uri().getPort(), other.uri().getPort());
            assertEquals("127.0.0.1", server.uri().getHost());
            assertEquals(200, get("").statusCode());
        }
        server.close();
        assertThrows(IOException.class, () -> get(""));
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(server.uri().resolve(path))
                        .timeout(Duration.ofSeconds(5)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
}
