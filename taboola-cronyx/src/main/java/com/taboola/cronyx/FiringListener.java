package com.taboola.cronyx;

@FunctionalInterface
public interface FiringListener {
    void onEvent(CronyxExecutionContext context) throws Exception;
}