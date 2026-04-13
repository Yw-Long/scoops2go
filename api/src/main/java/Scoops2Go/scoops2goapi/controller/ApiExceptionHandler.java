package Scoops2Go.scoops2goapi.controller;

import Scoops2Go.scoops2goapi.exception.InvalidBasketException;
import Scoops2Go.scoops2goapi.exception.InvalidPromotionException;
import Scoops2Go.scoops2goapi.exception.InvalidTreatException;
import Scoops2Go.scoops2goapi.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;


import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        String path = null;

        if (request instanceof ServletWebRequest servletReq) {
            path = servletReq.getRequest().getRequestURI();
        } else {
            path = request.getDescription(false); // "uri=/..."
        }

        Map<String,Object> body = Map.of(
                "title", "Resource Not Found",
                "status", HttpStatus.NOT_FOUND.value(),
                "detail", ex.getMessage(),
                "instance", path,
                "timestamp", OffsetDateTime.now().toString()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidBasketException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBasket(InvalidBasketException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Invalid basket");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("detail", ex.getMessage());
        body.put("basketSize", ex.getBasketSize());
        body.put("timestamp", OffsetDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidTreatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTreat(InvalidTreatException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Invalid treat");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("detail", ex.getMessage());
        body.put("timestamp", OffsetDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidPromotionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTreat(InvalidPromotionException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Invalid promotion");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("detail", ex.getMessage());
        body.put("timestamp", OffsetDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Keep an application-wide fallback so unexpected errors are handled consistently.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", "https://example.com/probs/internal-server-error");
        body.put("title", "Internal server error");
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("detail", ex.getMessage());
        body.put("timestamp", OffsetDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}