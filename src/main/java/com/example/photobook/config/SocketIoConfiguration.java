package com.example.photobook.config;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SocketIoConfiguration {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:5175,https://photobookvue-production.up.railway.app}")
    private String allowedOrigins;

    @Bean
    public EngineIoServer engineIoServer() {
        EngineIoServerOptions options = EngineIoServerOptions.newFromDefault();
        options.setAllowedCorsOrigins(parseAllowedOrigins());
        return new EngineIoServer(options);
    }

    @Bean
    public SocketIoServer socketIoServer(EngineIoServer engineIoServer) {
        return new SocketIoServer(engineIoServer);
    }

    private String[] parseAllowedOrigins() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);
    }
}
