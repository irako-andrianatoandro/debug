package com.example.rebalance.app.ports;

import java.time.Instant;

public interface TimeProvider {
    Instant now();
}
