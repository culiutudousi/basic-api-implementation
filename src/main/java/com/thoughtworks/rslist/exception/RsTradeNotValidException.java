package com.thoughtworks.rslist.exception;

public class RsTradeNotValidException extends RuntimeException {
    private final String errorMessage;

    public RsTradeNotValidException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
