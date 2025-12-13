package com.example.rebalance.infra;

import com.example.rebalance.app.ports.AuditRepository;
import com.example.rebalance.domain.AuditEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAuditRepository implements AuditRepository {
    private final Map<String, List<AuditEntry>> storage = new ConcurrentHashMap<>();

    @Override
    public void append(AuditEntry entry) {
        storage.computeIfAbsent(entry.runId(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(entry);
    }

    @Override
    public List<AuditEntry> findByRunId(String runId) {
        return new ArrayList<>(storage.getOrDefault(runId, List.of()));
    }
}
