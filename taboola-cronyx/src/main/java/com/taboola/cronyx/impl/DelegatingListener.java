package com.taboola.cronyx.impl;

import com.taboola.cronyx.CronyxExecutionContext;
import com.taboola.cronyx.FiringListener;

public class DelegatingListener implements FiringListener {

    private final Processor processor;

    public DelegatingListener(Processor processor) {
        this.processor = processor;
    }

    public void onEvent(CronyxExecutionContext context) {
        processor.process(context);
    }

}
