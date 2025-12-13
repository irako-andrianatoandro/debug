package com.example.rebalance.app.ports;

import com.example.rebalance.domain.AuditEntry;

import java.util.List;

public interface AuditRepository {
    void append(AuditEntry entry);
    List<AuditEntry> findByRunId(String runId);
}
