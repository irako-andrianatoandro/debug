package com.example.rebalance.infra;

import com.example.rebalance.app.RebalanceWorkflowService;
import com.example.rebalance.app.ports.AuditRepository;
import com.example.rebalance.app.ports.EffectPublisher;
import com.example.rebalance.app.ports.RebalanceRepository;
import com.example.rebalance.app.ports.TimeProvider;
import com.example.rebalance.domain.RebalanceStateMachine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RebalanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RebalanceApplication.class, args);
    }

    @Bean
    public RebalanceRepository rebalanceRepository() { return new InMemoryRebalanceRepository(); }

    @Bean
    public AuditRepository auditRepository() { return new InMemoryAuditRepository(); }

    @Bean
    public TimeProvider timeProvider() { return new SystemTimeProvider(); }

    @Bean
    public EffectPublisher effectPublisher() { return new LoggingEffectPublisher(); }

    @Bean
    public RebalanceStateMachine stateMachine() { return new RebalanceStateMachine(); }

    @Bean
    public RebalanceWorkflowService workflowService(RebalanceRepository rebalanceRepository,
                                                    AuditRepository auditRepository,
                                                    EffectPublisher effectPublisher,
                                                    TimeProvider timeProvider,
                                                    RebalanceStateMachine stateMachine) {
        return new RebalanceWorkflowService(rebalanceRepository, auditRepository, effectPublisher, timeProvider, stateMachine);
    }
}
