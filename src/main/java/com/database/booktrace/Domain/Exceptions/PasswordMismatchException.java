package com.database.booktrace.Domain.Exceptions;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String message){
        super(message);
    }
}
