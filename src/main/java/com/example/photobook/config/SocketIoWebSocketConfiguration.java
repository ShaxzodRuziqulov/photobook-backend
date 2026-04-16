package com.example.photobook.config;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoWebSocket;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class SocketIoWebSocketConfiguration implements WebSocketConfigurer {

    private final EngineIoServer engineIoServer;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:5175,https://photobookvue-production.up.railway.app}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        SocketIoWebSocketHandler handler = new SocketIoWebSocketHandler(engineIoServer);
        String[] allowedOriginPatterns = allowedOriginPatterns();

        registry.addHandler(handler, "/socket.io", "/socket.io/")
                .setAllowedOriginPatterns(allowedOriginPatterns);
    }

    private String[] allowedOriginPatterns() {
        Set<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .collect(Collectors.toSet());
        origins.add("https://*.up.railway.app");
        return origins.toArray(String[]::new);
    }

    private static class SocketIoWebSocketHandler extends AbstractWebSocketHandler {

        private static final String ENGINE_IO_WEBSOCKET_ATTRIBUTE = "engineIoWebSocket";

        private final EngineIoServer engineIoServer;

        private SocketIoWebSocketHandler(EngineIoServer engineIoServer) {
            this.engineIoServer = engineIoServer;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            SpringEngineIoWebSocket webSocket = new SpringEngineIoWebSocket(session);
            session.getAttributes().put(ENGINE_IO_WEBSOCKET_ATTRIBUTE, webSocket);
            engineIoServer.handleWebSocket(webSocket);
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            getWebSocket(session).emitMessage(message.getPayload());
        }

        @Override
        protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
            ByteBuffer payload = message.getPayload();
            byte[] bytes = new byte[payload.remaining()];
            payload.get(bytes);
            getWebSocket(session).emitMessage(bytes);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            SpringEngineIoWebSocket webSocket = getWebSocket(session);
            if (webSocket != null) {
                webSocket.emitClose();
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable throwable) {
            SpringEngineIoWebSocket webSocket = getWebSocket(session);
            if (webSocket != null) {
                webSocket.emitError(throwable);
            }
        }

        private SpringEngineIoWebSocket getWebSocket(WebSocketSession session) {
            return (SpringEngineIoWebSocket) session.getAttributes().get(ENGINE_IO_WEBSOCKET_ATTRIBUTE);
        }
    }

    private static class SpringEngineIoWebSocket extends EngineIoWebSocket {

        private final WebSocketSession session;
        private final Map<String, String> query;
        private final Map<String, List<String>> headers;

        private SpringEngineIoWebSocket(WebSocketSession session) {
            this.session = session;
            this.query = parseQuery(session.getUri());
            this.headers = session.getHandshakeHeaders().headerSet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        @Override
        public Map<String, String> getQuery() {
            return query;
        }

        @Override
        public Map<String, List<String>> getConnectionHeaders() {
            return headers;
        }

        @Override
        public void write(String message) throws IOException {
            synchronized (session) {
                session.sendMessage(new TextMessage(message));
            }
        }

        @Override
        public void write(byte[] message) throws IOException {
            synchronized (session) {
                session.sendMessage(new BinaryMessage(message));
            }
        }

        @Override
        public void close() {
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (IOException ex) {
                emit("error", ex);
            }
        }

        private void emitMessage(String message) {
            emit("message", message);
        }

        private void emitMessage(byte[] message) {
            emit("message", message);
        }

        private void emitClose() {
            emit("close");
        }

        private void emitError(Throwable throwable) {
            emit("error", throwable);
        }

        private static Map<String, String> parseQuery(URI uri) {
            if (uri == null) {
                return Map.of();
            }

            return UriComponentsBuilder.fromUri(uri)
                    .build()
                    .getQueryParams()
                    .entrySet()
                    .stream()
                    .filter(entry -> !entry.getValue().isEmpty())
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0)));
        }
    }
}
