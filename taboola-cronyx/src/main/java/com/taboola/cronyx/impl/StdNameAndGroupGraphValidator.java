package com.taboola.cronyx.impl;

import java.util.Iterator;
import java.util.List;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;

import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.NameAndGroupOrderedPair;
import com.taboola.cronyx.exceptions.CronyxException;

public class StdNameAndGroupGraphValidator implements NameAndGroupGraphValidator {

    private Scheduler scheduler;

    public StdNameAndGroupGraphValidator(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void validateGraph(List<NameAndGroupOrderedPair> edges) {
        DirectedAcyclicGraph<NameAndGroup, DefaultEdge> graph = buildDirectedAcyclicGraph(edges);
        assertHasSingleCronRoot(graph);
    }

    private DirectedAcyclicGraph<NameAndGroup, DefaultEdge> buildDirectedAcyclicGraph(List<NameAndGroupOrderedPair> edges) {
        DirectedAcyclicGraph<NameAndGroup, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        for(NameAndGroupOrderedPair pair : edges) {
            graph.addVertex(pair.getPrevious());
            graph.addVertex(pair.getAfter());
            try {
                graph.addEdge(pair.getPrevious(), pair.getAfter());
            } catch (IllegalArgumentException e) {
                throw new CronyxException(String.format("The relation %s to %s creates a cycle!", pair.getPrevious(), pair.getAfter()));
            }
        }

        return graph;
    }

    private void assertHasSingleCronRoot(DirectedAcyclicGraph<NameAndGroup, DefaultEdge> graph) {
        Iterator<NameAndGroup> itr = graph.iterator();
        NameAndGroup root = null;
        while (itr.hasNext()) {
            NameAndGroup cur = itr.next();
            if(!isRoot(graph, cur)) {
                continue;
            }

            if(root != null) {
                throw new CronyxException(String.format("Graph has at least 2 roots: %s, %s", root, cur));
            }

            if(!isCron(cur)) {
                throw new CronyxException("Found a root that is not a cron trigger: " + cur);
            }

            root = cur;
        }

        if(root == null) {
            throw new CronyxException("Didn't find a root in the graph!");
        }
    }

    private boolean isRoot(DirectedAcyclicGraph<NameAndGroup, DefaultEdge> graph, NameAndGroup vertex) {
        return graph.inDegreeOf(vertex) == 0;
    }

    private boolean isCron(NameAndGroup trigger) {
        try {
            return scheduler.getTrigger(new TriggerKey(trigger.getName(), trigger.getGroup())) instanceof CronTrigger;
        } catch (SchedulerException e) {
            throw new CronyxException(e);
        }
    }
}
