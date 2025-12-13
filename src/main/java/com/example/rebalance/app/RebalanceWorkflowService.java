package com.example.rebalance.app;

import com.example.rebalance.app.ports.AuditRepository;
import com.example.rebalance.app.ports.EffectPublisher;
import com.example.rebalance.app.ports.RebalanceRepository;
import com.example.rebalance.app.ports.TimeProvider;
import com.example.rebalance.domain.AuditEntry;
import com.example.rebalance.domain.RebalanceEvent;
import com.example.rebalance.domain.RebalanceRun;
import com.example.rebalance.domain.RebalanceState;
import com.example.rebalance.domain.RebalanceStateMachine;
import com.example.rebalance.domain.TransitionResult;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class RebalanceWorkflowService {
    private final RebalanceRepository rebalanceRepository;
    private final AuditRepository auditRepository;
    private final EffectPublisher effectPublisher;
    private final TimeProvider timeProvider;
    private final RebalanceStateMachine stateMachine;

    public RebalanceWorkflowService(RebalanceRepository rebalanceRepository,
                                    AuditRepository auditRepository,
                                    EffectPublisher effectPublisher,
                                    TimeProvider timeProvider,
                                    RebalanceStateMachine stateMachine) {
        this.rebalanceRepository = Objects.requireNonNull(rebalanceRepository);
        this.auditRepository = Objects.requireNonNull(auditRepository);
        this.effectPublisher = Objects.requireNonNull(effectPublisher);
        this.timeProvider = Objects.requireNonNull(timeProvider);
        this.stateMachine = Objects.requireNonNull(stateMachine);
    }

    public Optional<RebalanceRun> findRun(String runId) {
        return rebalanceRepository.findById(runId);
    }

    public void saveRun(RebalanceRun run) {
        rebalanceRepository.save(run);
    }

    public CommandResult trigger(String runId, RebalanceEvent event) {
        RebalanceRun run = rebalanceRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));
        RebalanceState oldState = run.getState();
        Instant occurredAt = timeProvider.now();

        TransitionResult result = stateMachine.transition(run, event, occurredAt);

        // update aggregate
        run.setState(result.newState());
        rebalanceRepository.save(run);

        // audit and side effects
        auditRepository.append(result.auditEntry());
        effectPublisher.publishAll(result.effects());

        return new CommandResult(
                run.getRunId(),
                oldState,
                result.newState(),
                occurredAt,
                result.effects().stream().map(e -> e.getClass().getSimpleName()).toList(),
                result.auditEntry()
        );
    }

    public List<AuditEntry> audit(String runId) {
        return auditRepository.findByRunId(runId);
    }

    public record CommandResult(
            String runId,
            RebalanceState oldState,
            RebalanceState newState,
            Instant occurredAt,
            List<String> effects,
            AuditEntry auditEntry
    ) {}
}
