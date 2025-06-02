package com.database.booktrace.Domain.Exceptions;

public class DuplicateDataException extends RuntimeException{
    public DuplicateDataException(String message){
        super(message);
    }
}
