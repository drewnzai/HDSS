package com.andrew.hdss.exceptions;

public class RoundCreationInProgressException extends RuntimeException {

    public RoundCreationInProgressException() {
        super("Another round creation is already in progress");
    }
}