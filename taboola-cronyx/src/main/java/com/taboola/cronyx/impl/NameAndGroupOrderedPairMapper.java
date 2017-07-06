package com.taboola.cronyx.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.NameAndGroupOrderedPair;

public class NameAndGroupOrderedPairMapper implements RowMapper<NameAndGroupOrderedPair> {

    @Override
    public NameAndGroupOrderedPair mapRow(ResultSet resultSet, int i) throws SQLException {
        return new NameAndGroupOrderedPair(
                new NameAndGroup(resultSet.getString("PREVIOUS_TRIGGER_NAME"),
                                 resultSet.getString("PREVIOUS_TRIGGER_GROUP")),
                new NameAndGroup(resultSet.getString("AFTER_TRIGGER_NAME"),
                                 resultSet.getString("AFTER_TRIGGER_GROUP"))
        );
    }
}
