package com.innowise.task.exceptions;

public class TooManyCardsForUserException extends IllegalArgumentException {
    public TooManyCardsForUserException(String message)
    {
        super(message);
    }
}
