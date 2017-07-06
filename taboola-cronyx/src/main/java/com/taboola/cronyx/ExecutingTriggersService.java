package com.taboola.cronyx;

import java.util.List;

public interface ExecutingTriggersService {
    List<TriggerDefinition> getCurrentlyExecutingTriggers(boolean local);
}
