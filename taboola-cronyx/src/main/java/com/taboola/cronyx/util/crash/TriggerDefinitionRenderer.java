package com.taboola.cronyx.util.crash;

import com.taboola.cronyx.*;
import com.taboola.cronyx.impl.JavaJobIntrospecter;
import com.taboola.cronyx.impl.TriggerStatus;
import com.taboola.cronyx.Cron;
import com.taboola.cronyx.Immediate;
import com.taboola.cronyx.TriggerDefinition;

import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;
import org.crsh.text.ui.*;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.taboola.cronyx.Constants.NEXT_FIRING_TIME;
import static com.taboola.cronyx.Constants.PREVIOUS_FIRING_TIME;
import static com.taboola.cronyx.Constants.PREVIOUS_TRIGGERS;
import static com.taboola.cronyx.util.crash.RendererUtils.addTreeLabel;

public class TriggerDefinitionRenderer extends Renderer<TriggerDefinition> {
    //translates to year 2255
    private static final Date MAX_DATE = new Date(9000000000000L);

    //@TODO: we need to inject this functionality in the data map.
    private JavaJobIntrospecter javaJobIntrospecter = new JavaJobIntrospecter();

    @Override
    public Class<TriggerDefinition> getType() {
        return TriggerDefinition.class;
    }

    @Override
    public LineRenderer renderer(Iterator<TriggerDefinition> stream) {
        //
        TableElement table = new TableElement(3,3,1,2,2)
                .overflow(Overflow.HIDDEN)
                .rightCellPadding(1);

        // Header
        table.add(
                new RowElement().style(Decoration.bold.fg(Color.black).bg(Color.white)).add(
                        "TriggerKey",
                        "JobKey",
                        "Status",
                        "Previous Firing Time",
                        "Next Firing Time"
                )
        );

        boolean firstHandled = false;

        while (stream.hasNext()){
            TriggerDefinition td = stream.next();

            if (stream.hasNext() || firstHandled) {  // more than one item
                TriggerStatus status = (TriggerStatus) td.getTriggerData().get(Constants.TRIGGER_STATUS);
                Object prev = td.getTriggerData().get(PREVIOUS_FIRING_TIME);
                Object next = td.getTriggerData().get(NEXT_FIRING_TIME);

                table.row(
                        new LabelElement(td.getTriggerKey().toString()),
                        new LabelElement(td.getJobKey().toString()),
                        new LabelElement(status == null ? "null" : status.toString()),
                        new LabelElement(prev == null ? "never fired" : prev.toString()),
                        new LabelElement(getNextFireTimeToDisplay(next))
                );

                firstHandled = true;

            } else { //only one item - return it as a tree
                return buildAsTree(td).renderer();
            }

        }

        return table.renderer();
    }

    private TreeElement buildAsTree(TriggerDefinition jd) {
        TriggerStatus status = (TriggerStatus) jd.getTriggerData().get(Constants.TRIGGER_STATUS);
        TreeElement thisTriggerTree = new TreeElement(jd.getTriggerKey().toString());
        addTreeLabel(thisTriggerTree, "JobKey", jd.getJobKey().toString() );
        addTreeLabel(thisTriggerTree, "Status", status == null ? "null" : status.toString());
        addTreeLabel(thisTriggerTree, "Description", jd.getDescription() );
        addTreeLabel(thisTriggerTree, "Schedule", jd instanceof Cron ? "Cron:" + ((Cron) jd).getCronExpression() : (jd instanceof Immediate ? "now" : "?"));
        addTreeLabel(thisTriggerTree, "Previous Firing Time", jd.getTriggerData().get(PREVIOUS_FIRING_TIME) == null ? "null" : jd.getTriggerData().get(PREVIOUS_FIRING_TIME).toString());
        Object nextFireTime = jd.getTriggerData().get(NEXT_FIRING_TIME);
        addTreeLabel(thisTriggerTree, "Next Firing Time", getNextFireTimeToDisplay(nextFireTime));
        List<String> prevTriggers = (List<String>) jd.getTriggerData().get(PREVIOUS_TRIGGERS);
        if (prevTriggers != null && !prevTriggers.isEmpty()) {
            addTreeLabel(thisTriggerTree, "Previous Triggers", prevTriggers.toString());
        }

        TreeElement dataMapTree = new TreeElement("Data");
        jd.getTriggerData().entrySet().stream()
                                      .filter(entry -> !entry.getKey().startsWith("_"))
                                      .forEach(entry -> addTreeLabel(dataMapTree, entry.getKey(), entry.getValue().toString()));

        thisTriggerTree.addChild(dataMapTree);
        TreeElement jobSubTree = new TreeElement("Job Definition");

        JobDefinition jobDefinition = (JobDefinition) jd.getTriggerData().get(Constants.JOB_DEFINITION);
        if (jobDefinition != null) {
            jobSubTree.addChild(JobDefinitionRenderer.buildAsTree(jobDefinition));
            thisTriggerTree.addChild(jobSubTree);
        }

        return thisTriggerTree;
    }

    private String getNextFireTimeToDisplay(Object nextFireTime) {
        if (nextFireTime == null) {
            return "no more firing";
        }
        return (MAX_DATE.equals(nextFireTime) ? "depends on another trigger" : nextFireTime.toString());
    }
}
