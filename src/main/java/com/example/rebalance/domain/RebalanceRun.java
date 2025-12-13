package com.example.rebalance.domain;

import java.time.LocalDate;
import java.util.Objects;

public final class RebalanceRun {
    private final String runId;
    private final String indexCode;
    private final LocalDate effectiveDate;

    private RebalanceState state;

    // prerequisites
    private LocalDate marketDataAsOf;
    private LocalDate corporateActionsAsOf;
    private boolean approvals;

    // results
    private String proposedCompositionHash;
    private String finalCompositionHash;

    public RebalanceRun(String runId, String indexCode, LocalDate effectiveDate) {
        this.runId = Objects.requireNonNull(runId);
        this.indexCode = Objects.requireNonNull(indexCode);
        this.effectiveDate = Objects.requireNonNull(effectiveDate);
        this.state = RebalanceState.DRAFT;
    }

    public String getRunId() { return runId; }
    public String getIndexCode() { return indexCode; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public RebalanceState getState() { return state; }
    public void setState(RebalanceState state) { this.state = state; }

    public LocalDate getMarketDataAsOf() { return marketDataAsOf; }
    public void setMarketDataAsOf(LocalDate marketDataAsOf) { this.marketDataAsOf = marketDataAsOf; }
    public LocalDate getCorporateActionsAsOf() { return corporateActionsAsOf; }
    public void setCorporateActionsAsOf(LocalDate corporateActionsAsOf) { this.corporateActionsAsOf = corporateActionsAsOf; }
    public boolean isApprovals() { return approvals; }
    public void setApprovals(boolean approvals) { this.approvals = approvals; }
    public String getProposedCompositionHash() { return proposedCompositionHash; }
    public void setProposedCompositionHash(String proposedCompositionHash) { this.proposedCompositionHash = proposedCompositionHash; }
    public String getFinalCompositionHash() { return finalCompositionHash; }
    public void setFinalCompositionHash(String finalCompositionHash) { this.finalCompositionHash = finalCompositionHash; }
}
