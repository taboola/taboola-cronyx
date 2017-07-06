package com.taboola.cronyx.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taboola.cronyx.HistorianEntry;
import com.taboola.cronyx.exceptions.CronyxException;

public class StdHistorianDAO implements HistorianDAO {

    private static final Logger logger = LoggerFactory.getLogger(StdHistorianDAO.class);

    private static final String HISTORIAN_TABLE_NAME = "cronyx.CRNX_TRIGGER_HISTORY";

    private static final String PREV_FIRE_KEYS_TABLE_NAME = "cronyx.CRNX_PREV_FIRE_KEYS";

    private static final String HISTORIAN_INSERT_QUERY =
            "INSERT INTO " + HISTORIAN_TABLE_NAME + " (SCHED_NAME,SCHED_INSTANCE_ID,CONTEXT_KEY,FIRE_KEY,TRIGGER_NAME,TRIGGER_GROUP,START_TIME,END_TIME,INPUT,OUTPUT,RUN_STATUS,EXCEPTION) " +
            "VALUES (:schedName,:schedInstanceId,:contextKey,:fireKey,:triggerName,:triggerGroup,:startTime,:endTime,:input,:output,:runStatus,:exception); ";


    private static final String PREV_FIRE_KEYS_INERT_QUERY =
            "REPLACE INTO " + PREV_FIRE_KEYS_TABLE_NAME + " (SCHED_NAME,CONTEXT_KEY,FIRE_KEY,PREV_FIRE_KEY) VALUES ";

    private static final String SELECT_BY_CONTEXT_QUERY =
            "SELECT h.SCHED_NAME, h.SCHED_INSTANCE_ID, h.CONTEXT_KEY, h.FIRE_KEY, h.TRIGGER_NAME, h.TRIGGER_GROUP, h.START_TIME, h.END_TIME, h.INPUT, h.OUTPUT, h.RUN_STATUS, h.EXCEPTION, GROUP_CONCAT(p.PREV_FIRE_KEY) AS PREV_TRIGGERS_FIRE_KEYS " +
            "FROM " + HISTORIAN_TABLE_NAME + " h " +
            "LEFT OUTER JOIN " + PREV_FIRE_KEYS_TABLE_NAME + " p " +
            "ON h.SCHED_NAME = p.SCHED_NAME AND h.CONTEXT_KEY = p.CONTEXT_KEY AND h.FIRE_KEY = p.FIRE_KEY " +
            "WHERE h.SCHED_NAME = :schedName " +
                "AND h.CONTEXT_KEY = :contextKey " +
            "GROUP BY h.SCHED_NAME, h.CONTEXT_KEY, h.FIRE_KEY";

    private static final String SELECT_BY_KEY_QUERY =
            "SELECT h.SCHED_NAME, h.SCHED_INSTANCE_ID, h.CONTEXT_KEY, h.FIRE_KEY, h.TRIGGER_NAME, h.TRIGGER_GROUP, h.START_TIME, h.END_TIME, h.INPUT, h.OUTPUT, h.RUN_STATUS, h.EXCEPTION, GROUP_CONCAT(p.PREV_FIRE_KEY) AS PREV_TRIGGERS_FIRE_KEYS " +
            "FROM " + HISTORIAN_TABLE_NAME + " h " +
            "LEFT OUTER JOIN " + PREV_FIRE_KEYS_TABLE_NAME + " p " +
            "ON h.SCHED_NAME = p.SCHED_NAME AND h.CONTEXT_KEY = p.CONTEXT_KEY AND h.FIRE_KEY = p.FIRE_KEY " +
            "WHERE h.SCHED_NAME = :schedName " +
                "AND h.CONTEXT_KEY = :contextKey " +
                "AND h.FIRE_KEY = :fireKey " +
            "GROUP BY h.SCHED_NAME, h.CONTEXT_KEY, h.FIRE_KEY";

    private final String schedulerName;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<HistorianEntry> rowMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StdHistorianDAO(String schedulerName, NamedParameterJdbcTemplate namedParameterJdbcTemplate, RowMapper<HistorianEntry> rowMapper) {
        this.schedulerName = schedulerName;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.rowMapper = rowMapper;
    }

    @Override
    @Transactional(value="platformTransactionManager")
    public void writeEntry(HistorianEntry entry) {
        if(!entry.getSchedulerName().equals(schedulerName)) {
            throw new CronyxException("Tried to write entry under a different scheduler!\n" +
                    "This scheduler: " + schedulerName + ", But got: " + entry.getSchedulerName());
        }

        namedParameterJdbcTemplate.update(HISTORIAN_INSERT_QUERY, getParametersMap(entry));
        insertPrevFireKeys(entry);
    }

    @Override
    public List<HistorianEntry> readEntriesByContext(String contextKey) {
        return namedParameterJdbcTemplate.query(SELECT_BY_CONTEXT_QUERY,
                new MapSqlParameterSource()
                        .addValue("schedName", schedulerName)
                        .addValue("contextKey", contextKey),
                rowMapper);
    }

    @Override
    public HistorianEntry readEntryByKey(String contextKey, String fireKey) {
        return namedParameterJdbcTemplate.queryForObject(SELECT_BY_KEY_QUERY,
                new MapSqlParameterSource()
                        .addValue("schedName", schedulerName)
                        .addValue("contextKey", contextKey)
                        .addValue("fireKey", fireKey),
                rowMapper);
    }

    private void insertPrevFireKeys(HistorianEntry entry) {
        if(entry.getPreviousTriggerFireKeys() == null || entry.getPreviousTriggerFireKeys().isEmpty()) {
            return;
        }

        StringBuilder query = new StringBuilder(PREV_FIRE_KEYS_INERT_QUERY);
        MapSqlParameterSource paramMap = new MapSqlParameterSource()
                .addValue("schedName", schedulerName)
                .addValue("contextKey", entry.getContextKey())
                .addValue("fireKey", entry.getFireKey());

        int size = entry.getPreviousTriggerFireKeys().size();
        for(int i = 0; i < size; i++) {
            String prevFireKeyVar = "prevFireKey" + i;
            query.append("(:schedName,:contextKey,:fireKey,:").append(prevFireKeyVar).append("),");
            paramMap.addValue(prevFireKeyVar, entry.getPreviousTriggerFireKeys().get(i));
        }

        query.deleteCharAt(query.length() - 1);

        namedParameterJdbcTemplate.update(query.toString(), paramMap);
    }

    private MapSqlParameterSource getParametersMap(HistorianEntry entry) {
        return new MapSqlParameterSource()
                .addValue("schedName", schedulerName)
                .addValue("schedInstanceId", entry.getSchedulerInstanceId())
                .addValue("contextKey", entry.getContextKey())
                .addValue("fireKey", entry.getFireKey())
                .addValue("triggerName", entry.getTriggerKey().getName())
                .addValue("triggerGroup", entry.getTriggerKey().getGroup())
                .addValue("prevTriggersFireKeys", writeValueAsBytes(entry.getPreviousTriggerFireKeys()))
                .addValue("startTime", Date.from(entry.getStartTime()))
                .addValue("endTime", entry.getEndTime() == null ? null : Date.from(entry.getEndTime()))
                .addValue("input", writeValueAsBytes(entry.getInput()))
                .addValue("output", writeValueAsBytes(entry.getOutput()))
                .addValue("runStatus", entry.getRunStatus().name())
                .addValue("exception", writeValueAsBytes(entry.getException()));
    }

    private byte[] writeValueAsBytes(Object o) {
        try {
            return o == null ? null : objectMapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            logger.error("Encountered an error while processing entry value as json.\n" + o.toString(), e);
            return null;
        }
    }
}
