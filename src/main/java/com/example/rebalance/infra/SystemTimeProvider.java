package com.example.rebalance.infra;

import com.example.rebalance.app.ports.TimeProvider;

import java.time.Instant;

public class SystemTimeProvider implements TimeProvider {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
