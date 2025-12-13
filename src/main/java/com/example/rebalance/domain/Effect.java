package com.example.rebalance.domain;

public sealed interface Effect permits Effect.RebalanceStarted, Effect.DataSnapshotCaptured, Effect.EligibilityComputed,
        Effect.ConstituentsSelected, Effect.WeightsCalculated, Effect.ReviewRequested, Effect.RebalanceApproved,
        Effect.CompositionPublished, Effect.NotifyDownstreamSystems, Effect.RebalanceCancelled, Effect.RebalanceFailed {

    record RebalanceStarted(String runId) implements Effect {}

    record DataSnapshotCaptured(String runId, java.time.LocalDate marketDataAsOf, java.time.LocalDate corporateActionsAsOf) implements Effect {}

    record EligibilityComputed(String runId) implements Effect {}

    record ConstituentsSelected(String runId, String proposedCompositionHash) implements Effect {}

    record WeightsCalculated(String runId, String proposedCompositionHash) implements Effect {}

    record ReviewRequested(String runId) implements Effect {}

    record RebalanceApproved(String runId) implements Effect {}

    record CompositionPublished(String runId, String indexCode, java.time.LocalDate effectiveDate, String finalCompositionHash) implements Effect {}

    record NotifyDownstreamSystems(String runId) implements Effect {}

    record RebalanceCancelled(String runId) implements Effect {}

    record RebalanceFailed(String runId, String reason) implements Effect {}
}
