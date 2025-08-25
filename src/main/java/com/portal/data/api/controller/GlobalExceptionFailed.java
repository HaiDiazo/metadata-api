package com.portal.data.api.controller;

import com.portal.data.api.dto.requests.MetadataRequest;
import com.portal.data.api.dto.response.ResponseApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

//@ControllerAdvice
public class GlobalExceptionFailed {
//    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        ResponseApi<Map<String, String>> responseApi = new ResponseApi<>(
                HttpStatus.BAD_REQUEST.value(),
                0,
                HttpStatus.BAD_REQUEST.name(),
                errors
        );
        return ResponseEntity.badRequest().body(responseApi);
    }
}
