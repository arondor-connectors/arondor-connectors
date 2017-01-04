package com.arondor.viewer.jdbc.util;

import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class SqlRunner extends AbstractDAO
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlRunner.class);

    public static final String DEFAULT_DELIMITER = "/";

    private final boolean autoCommit, stopOnError;

    private final String commandDelimiter = SqlRunner.DEFAULT_DELIMITER;

    public SqlRunner(DataSource dataSource, final boolean autoCommit, final boolean stopOnError) throws SQLException
    {
        Preconditions.checkNotNull(dataSource, "SqlRunner requires an SQL datasource");
        setDataSource(dataSource);
        this.autoCommit = autoCommit;
        this.stopOnError = stopOnError;
    }

    public void runScript(final Reader reader) throws SQLException
    {
        Connection connection = getConnection();
        try
        {
            connection.setAutoCommit(this.autoCommit);
            runScript(connection, reader);
        }
        finally
        {
            try
            {
                closeConnection();
            }
            catch (SQLException e)
            {
                throw new SQLException("Could not close", e);
            }
        }
    }

    private void runScript(final Connection conn, final Reader reader)
    {
        StringBuffer command = new StringBuffer();
        try
        {
            final LineNumberReader lineReader = new LineNumberReader(reader);
            String line = null;
            while ((line = lineReader.readLine()) != null)
            {
                String trimmedLine = line.trim();

                if (trimmedLine.startsWith("--") || trimmedLine.startsWith("//") || trimmedLine.startsWith("#"))
                {
                    LOGGER.debug("Commented line: {}", trimmedLine);
                }
                else if (trimmedLine.endsWith(this.commandDelimiter))
                {
                    // Append line without delimiter
                    command.append(line.substring(0, line.lastIndexOf(this.commandDelimiter)));
                    command.append(" ");

                    LOGGER.debug("Command: {}", command);
                    Statement statememt = conn.createStatement();
                    executeStatement(command, statememt);

                    command.setLength(0);
                }
                else
                {
                    command.append(line);
                    command.append(" ");
                }
            }
            if (!this.autoCommit)
            {
                conn.commit();
            }
        }
        catch (final Exception e)
        {
            LOGGER.error("An error occurred on command {}", command, e);
        }
    }

    private void executeStatement(StringBuffer command, Statement stmt) throws SQLException
    {
        if (this.stopOnError)
        {
            stmt.execute(command.toString());
        }
        else
        {
            try
            {
                stmt.execute(command.toString());
            }
            catch (final SQLException e)
            {
                LOGGER.error("An error occurred on command {}", command, e);
            }
        }
        // if (this.autoCommit && !connection.getAutoCommit())
        // {
        // connection.commit();
        // }
    }
}
