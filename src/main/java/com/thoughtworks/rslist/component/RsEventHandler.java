package com.thoughtworks.rslist.component;

import com.thoughtworks.rslist.exception.RsEventNotValidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RsEventHandler {
    @ExceptionHandler({RsEventNotValidException.class})
    public ResponseEntity rsExceptionHandler(Exception e) {
        Error error = new Error();
        error.setError(e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

}
