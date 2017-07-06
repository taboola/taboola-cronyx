package com.taboola.cronyx.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.taboola.cronyx.NameAndGroup;

public class NameAndGroupRowMapper implements RowMapper<NameAndGroup> {

    private final static int NAME_IX = 1;
    private final static int GROUP_IX = 2;

    @Override
    public NameAndGroup mapRow(ResultSet resultSet, int i) throws SQLException {
        return new NameAndGroup(resultSet.getString(NAME_IX), resultSet.getString(GROUP_IX));
    }
}
