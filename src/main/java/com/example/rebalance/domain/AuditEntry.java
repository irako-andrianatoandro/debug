package com.example.rebalance.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AuditEntry(
        String runId,
        String indexCode,
        LocalDate effectiveDate,
        Instant occurredAt,
        RebalanceEvent event,
        RebalanceState oldState,
        RebalanceState newState,
        String message,
        List<String> effects
) {}
