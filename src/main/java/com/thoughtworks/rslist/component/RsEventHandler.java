package com.thoughtworks.rslist.component;

import com.thoughtworks.rslist.exception.RsEventNotValidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RsEventHandler {
    @ExceptionHandler({RsEventNotValidException.class, MethodArgumentNotValidException.class})
    public ResponseEntity rsExceptionHandler(Exception exception) {
        String errorString;
        if (exception instanceof MethodArgumentNotValidException) {
            errorString = "invalid param";
        } else {
            errorString = exception.getMessage();
        }
        Error error = new Error();
        error.setError(errorString);
        return ResponseEntity.badRequest().body(error);
    }
}
