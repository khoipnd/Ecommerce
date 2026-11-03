package com.fpt.ecommerce.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * BE-15 Bug fix: bổ sung các lỗi mà GlobalExceptionHandler cũ chưa xử lý.
 * Không trùng exception với GlobalExceptionHandler nên hai class cùng tồn tại được.
 * Tất cả trả về cùng định dạng {"error": "..."}.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    // ProductService đang ném ResponseStatusException (404 / 400) -> thống nhất body {"error": ...}
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : "Request failed";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", message));
    }

    // Ví dụ: xóa Category đang có Product (vi phạm khóa ngoại) -> trước đây bị 500
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",
                "Operation violates data integrity (record is still referenced or value is duplicated)"));
    }

    // JSON body sai định dạng
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Malformed JSON request body"));
    }

    // Query param / path variable sai kiểu, ví dụ ?categoryId=abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(Map.of("error",
                "Invalid value for parameter '" + ex.getName() + "'"));
    }
}
