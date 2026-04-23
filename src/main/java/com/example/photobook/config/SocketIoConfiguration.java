package com.example.photobook.config;

import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.socketio.server.SocketIoServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIoConfiguration {

    @Bean
    public EngineIoServer engineIoServer() {
        EngineIoServerOptions options = EngineIoServerOptions.newFromDefault();
        // CORS headers must be set only once (Spring Security CorsFilter + this server
        // both writing Access-Control-Allow-Origin breaks browsers).
        options.setCorsHandlingDisabled(true);
        return new EngineIoServer(options);
    }

    @Bean
    public SocketIoServer socketIoServer(EngineIoServer engineIoServer) {
        return new SocketIoServer(engineIoServer);
    }
}
