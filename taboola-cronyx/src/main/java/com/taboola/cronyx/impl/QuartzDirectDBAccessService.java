package com.taboola.cronyx.impl;

import com.taboola.cronyx.ExecutingTriggersService;
import com.taboola.cronyx.NameAndGroup;
import com.taboola.cronyx.SchedulingService;
import com.taboola.cronyx.TriggerDefinition;
import com.taboola.cronyx.exceptions.CronyxException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuartzDirectDBAccessService implements ExecutingTriggersService {

    private DataSource dataSource;
    private SchedulingService scheduler;
    private String schedulerName;

    public QuartzDirectDBAccessService(DataSource dataSource, SchedulingService scheduler, String schedulerName) {
        this.dataSource = dataSource;
        this.scheduler = scheduler;
        this.schedulerName = schedulerName;
    }

    @Override
    public List<TriggerDefinition> getCurrentlyExecutingTriggers(boolean local) {
        if (local) {
            return scheduler.getLocallyExecutingTriggers();
        } else {
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement fetchAllTriggers = conn.prepareStatement("SELECT * FROM cronyx.QRTZ_FIRED_TRIGGERS WHERE SCHED_NAME = ?");
                fetchAllTriggers.setString(1, schedulerName);
                ResultSet allRunningTriggers = fetchAllTriggers.executeQuery();
                List<TriggerDefinition> triggerList = parseResultSet(allRunningTriggers);
                allRunningTriggers.close();
                fetchAllTriggers.close();
                return triggerList;
            } catch (SQLException e) {
                throw new CronyxException("failed to retrieve executing triggers from db", e);
            }
        }
    }

    private List<TriggerDefinition> parseResultSet(ResultSet allRunningTriggers) throws SQLException {
        List<TriggerDefinition> triggers = new ArrayList<>();
        while (allRunningTriggers.next()) {
            NameAndGroup triggerKey = new NameAndGroup(allRunningTriggers.getString(3), allRunningTriggers.getString(4));
            TriggerDefinition trigger = scheduler.getTriggerByKey(triggerKey);
            if (trigger != null) {
                triggers.add(trigger);
            }
        }
        return triggers;
    }
}
