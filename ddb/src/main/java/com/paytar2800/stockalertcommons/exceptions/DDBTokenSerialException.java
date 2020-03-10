package com.paytar2800.stockalertcommons.exceptions;

public class DDBTokenSerialException extends RuntimeException {

    String message;

    public DDBTokenSerialException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
