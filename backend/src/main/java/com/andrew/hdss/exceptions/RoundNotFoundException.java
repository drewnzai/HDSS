package com.andrew.hdss.exceptions;

public class RoundNotFoundException extends RuntimeException {

    public RoundNotFoundException(Long id) {
        super("Round with ID " + id + " was not found");
    }
}