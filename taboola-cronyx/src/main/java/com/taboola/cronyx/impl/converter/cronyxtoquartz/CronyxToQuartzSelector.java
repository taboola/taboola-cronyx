package com.taboola.cronyx.impl.converter.cronyxtoquartz;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.quartz.Trigger;

import com.taboola.cronyx.TriggerDefinition;
import com.taboola.cronyx.exceptions.CronyxException;
import com.taboola.cronyx.impl.converter.quartztocronyx.QuartzToCronyxConverter;

public class CronyxToQuartzSelector {

    private List<Pair<Class, CronyxToQuartzConverter>> pairs;

    public CronyxToQuartzSelector(List<Pair<Class, CronyxToQuartzConverter>> pairs) {
        this.pairs = pairs;
    }

    public Trigger convert(TriggerDefinition triggerDefinition) {
        return getCronyxToQuartzConverter(triggerDefinition).convert(triggerDefinition);
    }

    private CronyxToQuartzConverter getCronyxToQuartzConverter(TriggerDefinition triggerDefinition) {
        Pair<Class, CronyxToQuartzConverter> pair = pairs.stream()
                .filter(p -> p.getLeft().isInstance(triggerDefinition))
                .findFirst()
                .orElseThrow(() -> new CronyxException("Could not find a CronyxToQuartzConverter to the triggerDefinition " + triggerDefinition.toString()));
        return pair.getRight();
    }
}
