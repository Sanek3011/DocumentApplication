package com.hh.documentapplication.exceptions.handler;

import com.hh.documentapplication.entity.DocumentStatus;
import com.hh.documentapplication.exceptions.DocumentNotFoundException;
import com.hh.documentapplication.exceptions.DocumentStatusConflictException;
import com.hh.documentapplication.exceptions.DocumentToolException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<String> handleNotFound(DocumentToolException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DocumentStatusConflictException.class)
    public ResponseEntity<String> handleConflict(DocumentToolException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<String> handleMissingHeader(MissingRequestHeaderException ex) {
        if ("X-Initiator".equals(ex.getHeaderName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMismatchRequestParam(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() == DocumentStatus.class) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unknown document status ");
        }
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

}
