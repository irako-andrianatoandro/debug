package com.example.rebalance.app.ports;

import com.example.rebalance.domain.Effect;

import java.util.List;

public interface EffectPublisher {
    void publishAll(List<Effect> effects);
}
