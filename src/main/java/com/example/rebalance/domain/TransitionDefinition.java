package com.example.rebalance.domain;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class TransitionDefinition {
    @FunctionalInterface
    public interface Guard {
        void check(RebalanceRun run) throws GuardFailedException;
    }

    private final RebalanceState target;
    private final Guard guard; // optional, may be null
    private final Function<RebalanceRun, List<Effect>> effectsFactory;

    public TransitionDefinition(RebalanceState target,
                                Guard guard,
                                Function<RebalanceRun, List<Effect>> effectsFactory) {
        this.target = Objects.requireNonNull(target);
        this.guard = guard; // may be null
        this.effectsFactory = Objects.requireNonNull(effectsFactory);
    }

    public RebalanceState target() { return target; }
    public Guard guard() { return guard; }
    public Function<RebalanceRun, List<Effect>> effectsFactory() { return effectsFactory; }
}
