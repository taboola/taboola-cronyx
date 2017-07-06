package com.taboola.cronyx.impl;

import com.taboola.cronyx.CronyxExecutionContext;

public interface Processor {

    void process(CronyxExecutionContext context);
}
