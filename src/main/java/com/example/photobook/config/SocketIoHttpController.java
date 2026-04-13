package com.example.photobook.config;

import io.socket.engineio.server.EngineIoServer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class SocketIoHttpController {

    private final EngineIoServer engineIoServer;

    @GetMapping({"/socket.io", "/socket.io/"})
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        engineIoServer.handleRequest(request, response);
    }

    @PostMapping({"/socket.io", "/socket.io/"})
    public void post(HttpServletRequest request, HttpServletResponse response) throws IOException {
        engineIoServer.handleRequest(request, response);
    }
}
