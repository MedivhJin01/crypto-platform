package com.example.crypto_platform.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class CsRequestExceptionHandler {

    /**
     * Handles any CandlestickParamException thrown anywhere in the app.
     */
    @ExceptionHandler(CsRequestException.class)
    public ResponseEntity<Map<String, Object>> handleCandlestickParamException(CsRequestException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Invalid CandlestickParam",
                        "message", ex.getMessage()
                ));
    }

    /**
     * (Optional) Catch-all for other uncaught exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Internal Server Error",
                        "message", ex.getMessage()
                ));
    }
}