package com.example.rebalance.infra;

import com.example.rebalance.app.ports.RebalanceRepository;
import com.example.rebalance.domain.RebalanceRun;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRebalanceRepository implements RebalanceRepository {
    private final Map<String, RebalanceRun> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<RebalanceRun> findById(String runId) {
        return Optional.ofNullable(storage.get(runId));
    }

    @Override
    public void save(RebalanceRun run) {
        storage.put(run.getRunId(), run);
    }
}
