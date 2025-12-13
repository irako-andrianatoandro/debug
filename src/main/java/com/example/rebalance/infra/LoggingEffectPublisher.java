package com.example.rebalance.infra;

import com.example.rebalance.app.ports.EffectPublisher;
import com.example.rebalance.domain.Effect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoggingEffectPublisher implements EffectPublisher {
    private static final Logger log = LoggerFactory.getLogger(LoggingEffectPublisher.class);

    @Override
    public void publishAll(List<Effect> effects) {
        for (Effect e : effects) {
            log.info("Publishing effect: {}", e);
        }
    }
}
