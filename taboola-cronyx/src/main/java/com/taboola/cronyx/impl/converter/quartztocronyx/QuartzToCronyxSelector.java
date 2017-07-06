package com.taboola.cronyx.impl.converter.quartztocronyx;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.quartz.Trigger;

import com.taboola.cronyx.TriggerDefinition;
import com.taboola.cronyx.exceptions.CronyxException;

public class QuartzToCronyxSelector {

    private List<Pair<Class, QuartzToCronyxConverter>> pairs;

    public QuartzToCronyxSelector(List<Pair<Class, QuartzToCronyxConverter>> pairs) {
        this.pairs = pairs;
    }

    public TriggerDefinition convert(Trigger trigger) {
        return getQuartzToCronyxConverter(trigger).convert(trigger);
    }

    private QuartzToCronyxConverter getQuartzToCronyxConverter(Trigger trigger) {
        Pair<Class, QuartzToCronyxConverter> pair = pairs.stream()
                .filter(p -> p.getLeft().isInstance(trigger))
                .findFirst()
                .orElseThrow(() -> new CronyxException("Could not find a QuartzToCronyxConverter to the trigger " + trigger.toString()));
        return pair.getRight();
    }
}
