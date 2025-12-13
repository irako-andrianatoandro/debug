package com.example.rebalance.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RebalanceStateMachineTest {

    @Test
    void ingestData_validTransition_producesEffectsAndAudit() {
        var run = new RebalanceRun("run-1", "INDEX_XYZ", LocalDate.of(2025, 1, 31));
        run.setMarketDataAsOf(LocalDate.of(2025, 1, 30));
        run.setCorporateActionsAsOf(LocalDate.of(2025, 1, 30));

        var sm = new RebalanceStateMachine();
        var occurredAt = Instant.parse("2025-01-31T00:00:00Z");

        var result = sm.transition(run, RebalanceEvent.INGEST_DATA, occurredAt);

        assertEquals(RebalanceState.DATA_COLLECTED, result.newState());
        assertEquals(1, result.effects().size());
        assertTrue(result.effects().getFirst() instanceof Effect.DataSnapshotCaptured);

        var audit = result.auditEntry();
        assertEquals("run-1", audit.runId());
        assertEquals(RebalanceEvent.INGEST_DATA, audit.event());
        assertEquals(RebalanceState.DRAFT, audit.oldState());
        assertEquals(RebalanceState.DATA_COLLECTED, audit.newState());
        assertEquals(occurredAt, audit.occurredAt());
        assertFalse(audit.message().isBlank());
        assertEquals(1, audit.effects().size());
        assertEquals("DataSnapshotCaptured", audit.effects().getFirst());
    }

    @Test
    void invalidTransition_throwsInvalidTransitionException() {
        var run = new RebalanceRun("run-2", "INDEX_XYZ", LocalDate.of(2025, 1, 31));
        var sm = new RebalanceStateMachine();
        assertThrows(InvalidTransitionException.class,
                () -> sm.transition(run, RebalanceEvent.APPROVE, Instant.now()));
    }

    @Test
    void guardFailure_throwsGuardFailedException() {
        var run = new RebalanceRun("run-3", "INDEX_XYZ", LocalDate.of(2025, 1, 31));
        var sm = new RebalanceStateMachine();
        assertThrows(GuardFailedException.class,
                () -> sm.transition(run, RebalanceEvent.INGEST_DATA, Instant.now()));
    }

    @Test
    void cancelAllowedFromMultipleStates() {
        var sm = new RebalanceStateMachine();

        // Move to DATA_COLLECTED first
        var runA = new RebalanceRun("run-A", "IDX", LocalDate.of(2025, 1, 31));
        runA.setMarketDataAsOf(LocalDate.of(2025, 1, 30));
        runA.setCorporateActionsAsOf(LocalDate.of(2025, 1, 30));
        var a1 = sm.transition(runA, RebalanceEvent.INGEST_DATA, Instant.now());
        runA.setState(a1.newState());

        var cancelA = sm.transition(runA, RebalanceEvent.CANCEL, Instant.now());
        assertEquals(RebalanceState.CANCELLED, cancelA.newState());

        // From APPROVED (still allowed)
        var runB = new RebalanceRun("run-B", "IDX", LocalDate.of(2025, 1, 31));
        runB.setMarketDataAsOf(LocalDate.of(2025, 1, 30));
        runB.setCorporateActionsAsOf(LocalDate.of(2025, 1, 30));
        runB.setApprovals(true);
        var b1 = sm.transition(runB, RebalanceEvent.INGEST_DATA, Instant.now());
        runB.setState(b1.newState());
        var b2 = sm.transition(runB, RebalanceEvent.COMPUTE_ELIGIBILITY, Instant.now());
        runB.setState(b2.newState());
        var b3 = sm.transition(runB, RebalanceEvent.SELECT_CONSTITUENTS, Instant.now());
        runB.setState(b3.newState());
        var b4 = sm.transition(runB, RebalanceEvent.CALCULATE_WEIGHTS, Instant.now());
        runB.setState(b4.newState());
        var b5 = sm.transition(runB, RebalanceEvent.REQUEST_REVIEW, Instant.now());
        runB.setState(b5.newState());
        var b6 = sm.transition(runB, RebalanceEvent.APPROVE, Instant.now());
        runB.setState(b6.newState());

        var cancelB = sm.transition(runB, RebalanceEvent.CANCEL, Instant.now());
        assertEquals(RebalanceState.CANCELLED, cancelB.newState());
    }
}
