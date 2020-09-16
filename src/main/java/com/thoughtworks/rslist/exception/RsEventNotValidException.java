package com.thoughtworks.rslist.exception;

public class RsEventNotValidException extends RuntimeException {
    private String errorMessage;

    public RsEventNotValidException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
