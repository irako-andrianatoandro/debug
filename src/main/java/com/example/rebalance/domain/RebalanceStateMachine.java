package com.example.rebalance.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class RebalanceStateMachine {

    private final Map<TransitionKey, TransitionDefinition> transitions = new EnumKeyedMap();

    public RebalanceStateMachine() {
        // DRAFT + START -> DRAFT (idempotent)
        put(RebalanceState.DRAFT, RebalanceEvent.START,
                new TransitionDefinition(RebalanceState.DRAFT, null,
                        run -> List.of(new Effect.RebalanceStarted(run.getRunId()))));

        // DRAFT + INGEST_DATA -> DATA_COLLECTED (guard dates present and <= effectiveDate)
        put(RebalanceState.DRAFT, RebalanceEvent.INGEST_DATA,
                new TransitionDefinition(RebalanceState.DATA_COLLECTED,
                        run -> {
                            if (run.getMarketDataAsOf() == null || run.getCorporateActionsAsOf() == null) {
                                throw new GuardFailedException("Both marketDataAsOf and corporateActionsAsOf must be present");
                            }
                            if (run.getMarketDataAsOf().isAfter(run.getEffectiveDate()) ||
                                    run.getCorporateActionsAsOf().isAfter(run.getEffectiveDate())) {
                                throw new GuardFailedException("Prerequisite dates must be <= effectiveDate");
                            }
                        },
                        run -> List.of(new Effect.DataSnapshotCaptured(run.getRunId(), run.getMarketDataAsOf(), run.getCorporateActionsAsOf()))));

        // DATA_COLLECTED + COMPUTE_ELIGIBILITY -> ELIGIBILITY_COMPUTED
        put(RebalanceState.DATA_COLLECTED, RebalanceEvent.COMPUTE_ELIGIBILITY,
                new TransitionDefinition(RebalanceState.ELIGIBILITY_COMPUTED,
                        null,
                        run -> List.of(new Effect.EligibilityComputed(run.getRunId()))));

        // ELIGIBILITY_COMPUTED + SELECT_CONSTITUENTS -> CONSTITUENTS_SELECTED
        put(RebalanceState.ELIGIBILITY_COMPUTED, RebalanceEvent.SELECT_CONSTITUENTS,
                new TransitionDefinition(RebalanceState.CONSTITUENTS_SELECTED,
                        null,
                        run -> List.of(new Effect.ConstituentsSelected(run.getRunId(), run.getProposedCompositionHash()))));

        // CONSTITUENTS_SELECTED + CALCULATE_WEIGHTS -> WEIGHTS_CALCULATED
        put(RebalanceState.CONSTITUENTS_SELECTED, RebalanceEvent.CALCULATE_WEIGHTS,
                new TransitionDefinition(RebalanceState.WEIGHTS_CALCULATED,
                        null,
                        run -> List.of(new Effect.WeightsCalculated(run.getRunId(), run.getProposedCompositionHash()))));

        // WEIGHTS_CALCULATED + REQUEST_REVIEW -> REVIEW_PENDING
        put(RebalanceState.WEIGHTS_CALCULATED, RebalanceEvent.REQUEST_REVIEW,
                new TransitionDefinition(RebalanceState.REVIEW_PENDING,
                        null,
                        run -> List.of(new Effect.ReviewRequested(run.getRunId()))));

        // REVIEW_PENDING + APPROVE -> APPROVED (guard approvals == true)
        put(RebalanceState.REVIEW_PENDING, RebalanceEvent.APPROVE,
                new TransitionDefinition(RebalanceState.APPROVED,
                        run -> {
                            if (!run.isApprovals()) {
                                throw new GuardFailedException("Approvals must be true to approve the rebalance");
                            }
                        },
                        run -> List.of(new Effect.RebalanceApproved(run.getRunId()))));

        // APPROVED + PUBLISH -> PUBLISHED (guard finalCompositionHash != null)
        put(RebalanceState.APPROVED, RebalanceEvent.PUBLISH,
                new TransitionDefinition(RebalanceState.PUBLISHED,
                        run -> {
                            if (run.getFinalCompositionHash() == null) {
                                throw new GuardFailedException("finalCompositionHash must be set before publishing");
                            }
                        },
                        run -> List.of(
                                new Effect.CompositionPublished(run.getRunId(), run.getIndexCode(), run.getEffectiveDate(), run.getFinalCompositionHash()),
                                new Effect.NotifyDownstreamSystems(run.getRunId())
                        )));
    }

    public TransitionResult transition(RebalanceRun run, RebalanceEvent event, Instant occurredAt) {
        var current = run.getState();

        // AnyState (except PUBLISHED/CANCELLED) + CANCEL -> CANCELLED
        if (event == RebalanceEvent.CANCEL && current != RebalanceState.PUBLISHED && current != RebalanceState.CANCELLED) {
            return complete(run, event, occurredAt, current,
                    new TransitionDefinition(RebalanceState.CANCELLED, null,
                            r -> List.of(new Effect.RebalanceCancelled(r.getRunId()))),
                    "Run %s cancelled".formatted(run.getRunId()));
        }

        // AnyState (except PUBLISHED/CANCELLED) + FAIL -> FAILED
        if (event == RebalanceEvent.FAIL && current != RebalanceState.PUBLISHED && current != RebalanceState.CANCELLED) {
            return complete(run, event, occurredAt, current,
                    new TransitionDefinition(RebalanceState.FAILED, null,
                            r -> List.of(new Effect.RebalanceFailed(r.getRunId(), "Unknown failure"))),
                    "Run %s failed".formatted(run.getRunId()));
        }

        var def = transitions.get(new TransitionKey(current, event));
        if (def == null) {
            throw new InvalidTransitionException("No transition defined for state=" + current + " and event=" + event);
        }
        return complete(run, event, occurredAt, current, def, null);
    }

    private TransitionResult complete(RebalanceRun run,
                                      RebalanceEvent event,
                                      Instant occurredAt,
                                      RebalanceState oldState,
                                      TransitionDefinition def,
                                      String explicitMessage) {
        if (def.guard() != null) {
            def.guard().check(run);
        }
        List<Effect> effects = new ArrayList<>(def.effectsFactory().apply(run));
        var newState = def.target();
        var message = explicitMessage != null ? explicitMessage : defaultMessage(run, event, oldState, newState);
        var effectNames = effects.stream().map(e -> e.getClass().getSimpleName()).toList();
        var audit = new AuditEntry(
                run.getRunId(),
                run.getIndexCode(),
                run.getEffectiveDate(),
                occurredAt,
                event,
                oldState,
                newState,
                message,
                effectNames
        );
        return new TransitionResult(newState, effects, audit);
    }

    private String defaultMessage(RebalanceRun run, RebalanceEvent event, RebalanceState oldState, RebalanceState newState) {
        return "Event %s applied to run %s: %s -> %s".formatted(event, run.getRunId(), oldState, newState);
    }

    private void put(RebalanceState s, RebalanceEvent e, TransitionDefinition def) {
        transitions.put(new TransitionKey(s, e), def);
    }

    // Efficient map using enums as top-level keys
    private static final class EnumKeyedMap extends java.util.HashMap<TransitionKey, TransitionDefinition> {
        // Simple extension only to give a type name
    }
}
