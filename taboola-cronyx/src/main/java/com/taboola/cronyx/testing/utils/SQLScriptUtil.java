package com.taboola.cronyx.testing.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BinaryOperator;

public class SQLScriptUtil implements BinaryOperator<String> {
    private static final Logger logger = LoggerFactory.getLogger(SQLScriptUtil.class);

    private final Connection conn;


    public SQLScriptUtil(Connection conn) {
        this.conn = conn;
    }

    public static void runScript(DataSource dataSource, String scriptPath) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            SQLScriptUtil inst = new SQLScriptUtil(connection);
            Files.lines( Paths.get(ClassLoader.getSystemResource(scriptPath).toURI()) )
                    .reduce(inst);
        } catch (SQLException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

    }



    @Override
    public String apply(String s, String s2) {
        try {
            if (s.trim().startsWith("--")) {
                s = "";
            }

            String next = s + " " + s2;

            if (next.endsWith(";")) {
                logger.info("executing " + next);

                conn.createStatement().execute(next);

                return "";
            } else return s + " " + s2;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
