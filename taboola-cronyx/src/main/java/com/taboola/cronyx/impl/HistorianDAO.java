package com.taboola.cronyx.impl;

import java.util.List;

import com.taboola.cronyx.HistorianEntry;

public interface HistorianDAO {
    void writeEntry(HistorianEntry entry);
    List<HistorianEntry> readEntriesByContext(String contextKey);
    HistorianEntry readEntryByKey(String contextKey, String fireKey);
}
