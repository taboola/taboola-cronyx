package com.taboola.cronyx.impl;

import java.util.List;

import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.NameAndGroupOrderedPair;

public interface AfterDAO {

    List<NameAndGroupOrderedPair> getAllAncestorPairs(List<NameAndGroup> keys);

    List<NameAndGroup> getAfterTriggersByKey(NameAndGroup key);

    List<NameAndGroup> getPreviousTriggersByKey(NameAndGroup key);

    void storeAfterTrigger(List<NameAndGroup> prevKeys, NameAndGroup afterKey);

    void deleteAfterTrigger(NameAndGroup key);

}
