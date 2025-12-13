package com.example.rebalance.domain;

import java.util.Objects;

public record TransitionKey(RebalanceState state, RebalanceEvent event) {
    public TransitionKey {
        Objects.requireNonNull(state);
        Objects.requireNonNull(event);
    }
}
