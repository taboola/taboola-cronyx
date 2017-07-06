package com.taboola.cronyx.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.NameAndGroupOrderedPair;

public class NameAndGroupOrderedPairsUtil {

    public static List<NameAndGroupOrderedPair> getAllAncestorEdges(List<NameAndGroupOrderedPair> edges, List<NameAndGroup> vertices) {
        List<NameAndGroupOrderedPair> ancestorEdges = new ArrayList<>();
        List<NameAndGroupOrderedPair> curEdges = getDirectAncestorEdges(edges, vertices);
        while(!curEdges.isEmpty()) {
            ancestorEdges.addAll(curEdges);
            edges.removeAll(curEdges);
            curEdges = getDirectAncestorEdges(edges, getPreviousVertices(curEdges));
        }

        return ancestorEdges;
    }

    private static List<NameAndGroupOrderedPair> getDirectAncestorEdges(List<NameAndGroupOrderedPair> edges, List<NameAndGroup> vertices) {
        return edges
                .stream()
                .filter(e -> vertices.contains(e.getAfter()))
                .collect(Collectors.toList());
    }

    private static List<NameAndGroup> getPreviousVertices(List<NameAndGroupOrderedPair> edges) {
        return edges
                .stream()
                .map(NameAndGroupOrderedPair::getPrevious)
                .collect(Collectors.toList());
    }
}
