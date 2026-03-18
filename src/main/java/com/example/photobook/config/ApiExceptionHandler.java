package com.example.photobook.config;

import com.example.photobook.dto.ErrorResponseDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleMaxUploadSize(MaxUploadSizeExceededException exception) {
        String message = "Maximum upload size exceeded";
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ErrorResponseDto.builder()
                .message(message)
                .errors(Map.of("file", List.of(message)))
                .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(ErrorResponseDto.builder()
                .message(exception.getMessage())
                .errors(Map.of("request", List.of(exception.getMessage())))
                .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        String message = "Request violates data constraints";
        if (exception.getMostSpecificCause().getMessage() != null) {
            String details = exception.getMostSpecificCause().getMessage().toLowerCase();
            if (details.contains("username")) {
                message = "username already exists";
            } else if (details.contains("uk_order_employees_order_user")
                    || details.contains("order_employees")
                    && details.contains("order_id")
                    && details.contains("user_id")) {
                message = "employee already assigned to this order";
            } else if (details.contains("product_categories") || details.contains("product category")
                    || details.contains("uk") && details.contains("name")) {
                message = "product category name already exists";
            }
        }
        return ResponseEntity.badRequest().body(ErrorResponseDto.builder()
                .message(message)
                .errors(Map.of("request", List.of(message)))
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
