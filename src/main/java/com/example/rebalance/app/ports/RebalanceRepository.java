package com.example.rebalance.app.ports;

import com.example.rebalance.domain.RebalanceRun;

import java.util.Optional;

public interface RebalanceRepository {
    Optional<RebalanceRun> findById(String runId);
    void save(RebalanceRun run);
}
