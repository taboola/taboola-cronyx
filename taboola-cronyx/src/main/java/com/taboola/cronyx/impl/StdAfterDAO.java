package com.taboola.cronyx.impl;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.NameAndGroupOrderedPair;
import com.taboola.cronyx.exceptions.CronyxException;

public class StdAfterDAO implements AfterDAO {

    private static final String TABLE_NAME = "cronyx.CRNX_PREVIOUS_TO_AFTER_TRIGGERS";
    private static final String SCHEDULER_COL_NAME = "SCHED_NAME";
    private static final String PREVIOUS_TRIGGER_NAME = "PREVIOUS_TRIGGER_NAME";
    private static final String PREVIOUS_TRIGGER_GROUP = "PREVIOUS_TRIGGER_GROUP";
    private static final String AFTER_TRIGGER_NAME = "AFTER_TRIGGER_NAME";
    private static final String AFTER_TRIGGER_GROUP = "AFTER_TRIGGER_GROUP";

    private static final String SELECT_PREVIOUS_TO_AFTER_QUERY =
            "SELECT " +
            PREVIOUS_TRIGGER_NAME + " , " +
            PREVIOUS_TRIGGER_GROUP + " , " +
            AFTER_TRIGGER_NAME + " , " +
            AFTER_TRIGGER_GROUP +
            " FROM " + TABLE_NAME +
            " WHERE " + SCHEDULER_COL_NAME + " = :schedulerName";

    private static final String SELECT_PREVIOUS_QUERY =
            "SELECT " +
            PREVIOUS_TRIGGER_NAME + " , " +
            PREVIOUS_TRIGGER_GROUP +
            " FROM " + TABLE_NAME;

    private static final String SELECT_AFTER_QUERY =
            "SELECT " +
            AFTER_TRIGGER_NAME + " , " +
            AFTER_TRIGGER_GROUP +
            " FROM " + TABLE_NAME;

    private static final String GET_PREVIOUS_QUERY =
            SELECT_PREVIOUS_QUERY +
            " WHERE " + AFTER_TRIGGER_NAME + " = :triggerName" +
            " AND " + AFTER_TRIGGER_GROUP + " = :triggerGroup" +
            " AND " + SCHEDULER_COL_NAME + " = :schedulerName";

    private static final String GET_AFTER_QUERY =
            SELECT_AFTER_QUERY +
            " WHERE " + PREVIOUS_TRIGGER_NAME + " = :triggerName" +
            " AND " + PREVIOUS_TRIGGER_GROUP + " = :triggerGroup" +
            " AND " + SCHEDULER_COL_NAME + " = :schedulerName";

    private static final String INSERT_QUERY =
            "INSERT INTO " + TABLE_NAME + "(" +
            SCHEDULER_COL_NAME + " , " +
            PREVIOUS_TRIGGER_NAME + " , " +
            PREVIOUS_TRIGGER_GROUP + " , " +
            AFTER_TRIGGER_NAME + " , " +
            AFTER_TRIGGER_GROUP + ") " +
            "VALUES " +
            "(:schedulerName, :prevTriggerName, :prevTriggerGroup, :afterTriggerName, :afterTriggerGroup)";

    private static final String DELETE_AFTER_QUERY =
            " DELETE FROM " + TABLE_NAME +
            " WHERE " + AFTER_TRIGGER_NAME + " = :afterTriggerName" +
            " AND " + AFTER_TRIGGER_GROUP + " = :afterTriggerGroup" +
            " AND " + SCHEDULER_COL_NAME + " = :schedulerName";

    private final String schedulerName;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final NameAndGroupRowMapper nameAndGroupRowMapper;
    private final NameAndGroupOrderedPairMapper nameAndGroupOrderedPairMapper;

    public StdAfterDAO(String schedulerName, NamedParameterJdbcTemplate namedParameterJdbcTemplate, NameAndGroupRowMapper nameAndGroupRowMapper,
                       NameAndGroupOrderedPairMapper nameAndGroupOrderedPairMapper) {
        this.schedulerName = schedulerName;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.nameAndGroupRowMapper = nameAndGroupRowMapper;
        this.nameAndGroupOrderedPairMapper = nameAndGroupOrderedPairMapper;
    }

    @Override
    public List<NameAndGroupOrderedPair> getAllAncestorPairs(List<NameAndGroup> keys) {
        List<NameAndGroupOrderedPair> allPairs = namedParameterJdbcTemplate.query(SELECT_PREVIOUS_TO_AFTER_QUERY,
                new MapSqlParameterSource("schedulerName", schedulerName),
                nameAndGroupOrderedPairMapper);
        return NameAndGroupOrderedPairsUtil.getAllAncestorEdges(allPairs, keys);
    }

    @Override
    public List<NameAndGroup> getAfterTriggersByKey(NameAndGroup key) {
        return getTriggers(GET_AFTER_QUERY, key);
    }

    @Override
    public List<NameAndGroup> getPreviousTriggersByKey(NameAndGroup key) {
        return getTriggers(GET_PREVIOUS_QUERY, key);
    }

    @Override
    // Should be used in transaction context
    public void storeAfterTrigger(List<NameAndGroup> prevKeys, NameAndGroup afterKey) {
        if(prevKeys.isEmpty()) {
            throw new CronyxException("Can't store after trigger, no previous keys defined!");
        }

        deleteAfterTrigger(afterKey);
        insertPreviousToAfterPairs(prevKeys, afterKey);
    }

    @Override
    public void deleteAfterTrigger(NameAndGroup key) {
        try {
            namedParameterJdbcTemplate.update(DELETE_AFTER_QUERY, getAfterParams(key));
        } catch (Exception e) {
            throw new CronyxException("Could not delete after trigger:" +
                    " after: " + key.toString(), e);
        }
    }

    private List<NameAndGroup> getTriggers(String query, NameAndGroup key) {
        try {
            return namedParameterJdbcTemplate.query(query, getParams(key), nameAndGroupRowMapper);
        } catch (Exception e) {
            throw new CronyxException("Could not get triggers, query: " + query
                    + " key: " + key.toString(), e);
        }
    }

    private MapSqlParameterSource getParams(NameAndGroup key) {
        return new MapSqlParameterSource()
                .addValue("schedulerName", schedulerName)
                .addValue("triggerName", key.getName())
                .addValue("triggerGroup", key.getGroup());
    }

    private MapSqlParameterSource getAfterParams(NameAndGroup key) {
        return new MapSqlParameterSource()
                .addValue("schedulerName", schedulerName)
                .addValue("afterTriggerName", key.getName())
                .addValue("afterTriggerGroup", key.getGroup());
    }

    private void insertPreviousToAfterPairs(List<NameAndGroup> prevKeys, NameAndGroup afterKey) {
        try {
            namedParameterJdbcTemplate.batchUpdate(INSERT_QUERY,
                    prevKeys.stream()
                    .map(p -> new MapSqlParameterSource()
                        .addValue("schedulerName", schedulerName)
                        .addValue("prevTriggerName", p.getName())
                        .addValue("prevTriggerGroup", p.getGroup())
                        .addValue("afterTriggerName", afterKey.getName())
                        .addValue("afterTriggerGroup", afterKey.getGroup()))
                    .toArray(SqlParameterSource[]::new));
        } catch (Exception e) {
            throw new CronyxException("Could not store previous to after trigger relations", e);
        }
    }
}
