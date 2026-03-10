package com.example.photobook.config;

import com.example.photobook.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(ErrorResponseDto.builder()
                .message(exception.getMessage())
                .errors(Map.of("request", List.of(exception.getMessage())))
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponseDto.builder()
                .message("Internal server error")
                .errors(Map.of("server", List.of(exception.getMessage())))
                .build());
    }
}
