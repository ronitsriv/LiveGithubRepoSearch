package org.example.freightfoxgithubassignment.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badInput(IllegalArgumentException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> badBody(HttpMessageNotReadableException e) {
        return build(HttpStatus.BAD_REQUEST, "Request body is missing or not valid JSON");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> badParam(MethodArgumentTypeMismatchException e) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + e.getName() + "'");
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> githubClientError(HttpClientErrorException e) {
        int code = e.getStatusCode().value();
        if (code == 403 || code == 429) {
            return build(HttpStatus.TOO_MANY_REQUESTS,
                    "GitHub rate limit reached. Please wait a minute and try again.");
        }
        if (code == 422) {
            return build(HttpStatus.BAD_REQUEST, "GitHub could not process this search query");
        }
        return build(HttpStatus.BAD_GATEWAY, "GitHub returned an error (" + code + ")");
    }

    @ExceptionHandler({HttpServerErrorException.class, ResourceAccessException.class})
    public ResponseEntity<Map<String, Object>> githubDown(Exception e) {
        return build(HttpStatus.BAD_GATEWAY, "GitHub is unavailable right now. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message));
    }
}
