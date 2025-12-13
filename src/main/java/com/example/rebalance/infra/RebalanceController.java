package com.example.rebalance.infra;

import com.example.rebalance.app.RebalanceWorkflowService;
import com.example.rebalance.domain.AuditEntry;
import com.example.rebalance.domain.RebalanceEvent;
import com.example.rebalance.domain.RebalanceRun;
import com.example.rebalance.domain.RebalanceState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rebalances")
public class RebalanceController {
    private final RebalanceWorkflowService service;

    public RebalanceController(RebalanceWorkflowService service) {
        this.service = service;
    }

    // Optional: create a draft run
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequest req) {
        var run = new RebalanceRun(req.runId(), req.indexCode(), req.effectiveDate());
        // optionally accept initial fields
        if (req.marketDataAsOf() != null) run.setMarketDataAsOf(req.marketDataAsOf());
        if (req.corporateActionsAsOf() != null) run.setCorporateActionsAsOf(req.corporateActionsAsOf());
        if (req.approvals() != null) run.setApprovals(req.approvals());
        if (req.proposedCompositionHash() != null) run.setProposedCompositionHash(req.proposedCompositionHash());
        if (req.finalCompositionHash() != null) run.setFinalCompositionHash(req.finalCompositionHash());
        service.saveRun(run);
        return ResponseEntity.ok(Map.of(
                "runId", run.getRunId(),
                "state", run.getState().name()
        ));
    }

    @PostMapping("/{runId}/events/{event}")
    public ResponseEntity<RebalanceWorkflowService.CommandResult> trigger(@PathVariable String runId,
                                                                          @PathVariable String event) {
        var ev = RebalanceEvent.valueOf(event);
        var result = service.trigger(runId, ev);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{runId}/audit")
    public ResponseEntity<List<AuditEntry>> audit(@PathVariable String runId) {
        return ResponseEntity.ok(service.audit(runId));
    }

    public record CreateRequest(
            String runId,
            String indexCode,
            LocalDate effectiveDate,
            LocalDate marketDataAsOf,
            LocalDate corporateActionsAsOf,
            Boolean approvals,
            String proposedCompositionHash,
            String finalCompositionHash
    ) {}
}
