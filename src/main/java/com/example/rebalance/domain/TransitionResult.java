package com.example.rebalance.domain;

import java.util.List;

public record TransitionResult(
        RebalanceState newState,
        List<Effect> effects,
        AuditEntry auditEntry
) {}
