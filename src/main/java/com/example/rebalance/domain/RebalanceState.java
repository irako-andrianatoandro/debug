package com.example.rebalance.domain;

public enum RebalanceState {
    DRAFT,
    DATA_COLLECTED,
    ELIGIBILITY_COMPUTED,
    CONSTITUENTS_SELECTED,
    WEIGHTS_CALCULATED,
    REVIEW_PENDING,
    APPROVED,
    PUBLISHED,
    CANCELLED,
    FAILED
}
