package com.example.rebalance.domain;

public enum RebalanceEvent {
    START,
    INGEST_DATA,
    COMPUTE_ELIGIBILITY,
    SELECT_CONSTITUENTS,
    CALCULATE_WEIGHTS,
    REQUEST_REVIEW,
    APPROVE,
    PUBLISH,
    CANCEL,
    FAIL
}
