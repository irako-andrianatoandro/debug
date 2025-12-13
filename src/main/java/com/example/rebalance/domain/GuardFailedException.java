package com.example.rebalance.domain;

public class GuardFailedException extends RuntimeException {
    public GuardFailedException(String message) {
        super(message);
    }
}
