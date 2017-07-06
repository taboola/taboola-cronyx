package com.taboola.cronyx.impl;

import java.util.List;

import com.taboola.cronyx.NameAndGroupOrderedPair;

public interface NameAndGroupGraphValidator {
    void validateGraph(List<NameAndGroupOrderedPair> edges);
}
