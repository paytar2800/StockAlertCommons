package com.paytar2800.stockalertcommons.exceptions;

public class DDBException extends RuntimeException {

    String message;

    public DDBException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
