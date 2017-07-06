package com.taboola.cronyx.impl;

import com.codahale.metrics.Gauge;

import java.util.concurrent.atomic.AtomicLong;

public class MarkerGauge implements Gauge {
    private final AtomicLong lastSuccessfulExecution = new AtomicLong();

    public MarkerGauge() {

    }

    public MarkerGauge(long lastSuccessfulExecution) {
        this.lastSuccessfulExecution.set(lastSuccessfulExecution);
    }

    @Override
    public Long getValue() {
        return System.currentTimeMillis() - lastSuccessfulExecution.get();
    }

    public void mark() {
        lastSuccessfulExecution.set(System.currentTimeMillis());
    }
}