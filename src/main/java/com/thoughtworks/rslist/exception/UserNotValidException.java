package com.thoughtworks.rslist.exception;

public class UserNotValidException extends RuntimeException {
    private final String errorMessage;

    public UserNotValidException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
