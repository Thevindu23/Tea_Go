package com.teago.teago.controllers;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> invalid(MethodArgumentNotValidException e) {
        var errors = e.getBindingResult().getFieldErrors().stream().map(f -> f.getField()+": "+f.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(Map.of("message", String.join("; ", errors)));
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<?> denied(Exception e) { return ResponseEntity.status(403).body(Map.of("message", "Access denied")); }
    @ExceptionHandler({IllegalArgumentException.class, java.time.DateTimeException.class})
    ResponseEntity<?> invalidArgument(Exception e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    ResponseEntity<?> stale(Exception e) { return ResponseEntity.status(409).body(Map.of("message", "Record changed; refresh and try again")); }
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    ResponseEntity<?> conflict(Exception e) { return ResponseEntity.status(409).body(Map.of("message", "This record already exists or is referenced by other records")); }
}
