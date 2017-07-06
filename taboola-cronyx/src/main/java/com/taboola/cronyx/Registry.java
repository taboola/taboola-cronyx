package com.taboola.cronyx;

import java.util.Map;
import java.util.function.Supplier;

public interface Registry<K, V> {
    void register(K key, V value);
    void unregister(K key);
    V get(K key);
    V getOrRegister(K key, Supplier<V> supplier);
    Map<K, V> getAll();
}
