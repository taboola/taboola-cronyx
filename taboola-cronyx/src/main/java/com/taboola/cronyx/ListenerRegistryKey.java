package com.taboola.cronyx;

import com.taboola.cronyx.impl.ListenerMatcher;

public class ListenerRegistryKey {
    public final String name;
    public final ListenerMatcher matcher;
    public final FiringListener listener;

    public ListenerRegistryKey(String name, ListenerMatcher matcher, FiringListener listener) {
        this.name = name;
        this.matcher = matcher;
        this.listener = listener;
    }
}
