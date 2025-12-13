package com.example.rebalance.app;

import com.example.rebalance.app.ports.AuditRepository;
import com.example.rebalance.app.ports.EffectPublisher;
import com.example.rebalance.app.ports.RebalanceRepository;
import com.example.rebalance.app.ports.TimeProvider;
import com.example.rebalance.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RebalanceWorkflowServiceTest {

    @Test
    void orchestrates_save_audit_effects_and_time() {
        var saved = new ArrayList<RebalanceRun>();
        var audits = new ArrayList<AuditEntry>();
        var publishedEffects = new ArrayList<Effect>();

        var repo = new RebalanceRepository() {
            RebalanceRun run;
            @Override public Optional<RebalanceRun> findById(String runId) { return Optional.ofNullable(run); }
            @Override public void save(RebalanceRun run) { this.run = run; saved.add(run); }
        };
        var auditRepo = new AuditRepository() {
            @Override public void append(AuditEntry entry) { audits.add(entry); }
            @Override public List<AuditEntry> findByRunId(String runId) { return List.copyOf(audits); }
        };
        var publisher = new EffectPublisher() {
            @Override public void publishAll(List<Effect> effects) { publishedEffects.addAll(effects); }
        };
        var fixedInstant = Instant.parse("2025-02-01T12:00:00Z");
        var time = new TimeProvider() { @Override public Instant now() { return fixedInstant; } };

        var sm = new RebalanceStateMachine();
        var service = new RebalanceWorkflowService(repo, auditRepo, publisher, time, sm);

        var run = new RebalanceRun("run-x", "IDX", LocalDate.of(2025, 2, 28));
        run.setMarketDataAsOf(LocalDate.of(2025, 2, 27));
        run.setCorporateActionsAsOf(LocalDate.of(2025, 2, 27));
        repo.save(run);

        var result = service.trigger("run-x", RebalanceEvent.INGEST_DATA);

        assertEquals(RebalanceState.DRAFT, result.oldState());
        assertEquals(RebalanceState.DATA_COLLECTED, result.newState());
        assertEquals(fixedInstant, result.occurredAt());
        assertFalse(result.effects().isEmpty());

        // verify that save was called with updated state
        assertTrue(saved.stream().anyMatch(r -> r.getState() == RebalanceState.DATA_COLLECTED));

        // audit appended
        assertEquals(1, audits.size());
        assertEquals(fixedInstant, audits.getFirst().occurredAt());

        // effects published
        assertFalse(publishedEffects.isEmpty());
    }
}
