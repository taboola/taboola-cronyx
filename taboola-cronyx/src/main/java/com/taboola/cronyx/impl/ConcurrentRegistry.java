package com.taboola.cronyx.impl;

import com.taboola.cronyx.Registry;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class ConcurrentRegistry<K, V> implements Registry<K, V> {

    private ConcurrentMap<K, V> map = new ConcurrentHashMap<>();

    @Override
    public void register(K key, V value) {
        map.put(key, value);
    }

    @Override
    public void unregister(K key) {
        map.remove(key);
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public V getOrRegister(K key, Supplier<V> supplier) {
        return map.computeIfAbsent(key, k -> supplier.get());
    }

    @Override
    public Map<K, V> getAll() {
        return Collections.unmodifiableMap(map);
    }
}
