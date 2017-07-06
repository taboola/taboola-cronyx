package com.taboola.cronyx.impl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taboola.cronyx.HistorianEntry;
import com.taboola.cronyx.NameAndGroup;

public class HistorianEntryMapper implements RowMapper<HistorianEntry> {

    private static final Logger logger = LoggerFactory.getLogger(HistorianEntryMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public HistorianEntry mapRow(ResultSet resultSet, int i) throws SQLException {
        return new HistorianEntry(
                resultSet.getString("SCHED_NAME"),
                resultSet.getString("SCHED_INSTANCE_ID"),
                resultSet.getString("CONTEXT_KEY"),
                resultSet.getString("FIRE_KEY"),
                new NameAndGroup(resultSet.getString("TRIGGER_NAME"), resultSet.getString("TRIGGER_GROUP")),
                resultSet.getString("PREV_TRIGGERS_FIRE_KEYS") == null ? null : Arrays.asList(resultSet.getString("PREV_TRIGGERS_FIRE_KEYS").split(",")),
                resultSet.getTimestamp("START_TIME").toInstant(),
                resultSet.getTimestamp("END_TIME") == null ? null : resultSet.getTimestamp("END_TIME").toInstant(),
                readByteValue(resultSet.getBytes("INPUT"), JobDataMap.class),
                readByteValue(resultSet.getBytes("OUTPUT"), Object.class),
                ExecutionStatus.valueOf(resultSet.getString("RUN_STATUS")),
                readByteValue(resultSet.getBytes("EXCEPTION"), String.class)
        );
    }

    private <T> T readByteValue(byte[] value, Class<T> cls) {
        try {
            return value == null ? null : objectMapper.readValue(value, cls);
        } catch (IOException e) {
            logger.error("Encountered an exception when processing a json value.", e);
            return null;
        }
    }
}
