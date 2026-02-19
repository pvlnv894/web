package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService executor;
    private final int port;
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js");

    public Server(int port) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(64);
    }

    public void start() {
        try (final var serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                final var socket = serverSocket.accept();
                executor.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private void handleConnection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = parseRequest(in);

            if (request == null) {
                sendResponse(out, "400 Bad Request", "text/plain", "Bad Request".getBytes(StandardCharsets.UTF_8));
                return;
            }

            final var filePath = Path.of(".", "public", request.getPath());
            final var mimeType = Files.probeContentType(filePath);

            if (request.getPath().equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
                sendResponse(out, "200 OK", mimeType, content);
                return;
            }

            sendResponse(out, "200 OK", mimeType, Files.readAllBytes(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Request parseRequest(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) return null;

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) return null;

        String method = parts[0];

        final String fullPath = parts[1];
        String query = null;
        String path;
        if (fullPath.contains("?")) {
            String[] split = fullPath.split("\\?", 2);
            path = split[0];
            query = split[1];
        } else {
            path = fullPath;
        }

        if (!validPaths.contains(path)) return null;

        Map<String, String> headers = new ConcurrentHashMap<>();
        String headerLine;
        int contentLength = 0;

        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(":", 2);
            if (headerParts.length == 2) {
                String headerName = headerParts[0].trim();
                String headerValue = headerParts[1].trim();
                headers.put(headerName, headerValue);
                if ("Content-Length".equalsIgnoreCase(headerName)) {
                    try {
                        contentLength = Integer.parseInt(headerValue);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        String body = null;
        if (contentLength > 0) {
            char[] bodyChars = new char[contentLength];
            int read = 0;
            while (read < contentLength) {
                int r = in.read(bodyChars, read, contentLength - read);
                if (r == -1) break;
                read += r;
            }
            body = new String(bodyChars, 0, read);
        }

        return new Request(method, path, query, headers, body);
    }

    private void sendResponse(BufferedOutputStream out, String status, String contentType, byte[] body) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), false);
        pw.printf("HTTP/1.1 %s\r\n", status);
        pw.printf("Content-Type: %s\r\n", contentType);
        pw.printf("Content-Length: %d\r\n", body.length);
        pw.print("\r\n");
        pw.flush();
        out.write(body);
        out.flush();
    }
}
