package com.arondor.viewer.jdbc.annotation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public abstract class JDBCDAO
{
    private static final Logger LOGGER = Logger.getLogger(JDBCDAO.class);

    protected void closeStatement(Statement statement)
    {
        try
        {
            if (statement != null)
            {
                statement.close();
            }
        }
        catch (SQLException e)
        {
            LOGGER.error("The statement cannot be closed", e);
        }
    }

    protected void close(ResultSet rs)
    {
        try
        {
            if (rs != null)
            {
                rs.close();
            }
        }
        catch (SQLException e)
        {
            LOGGER.error("The result set cannot be closed", e);
        }
    }

    protected void closeConnection(Connection connection)
    {
        try
        {
            if (connection != null)
            {
                connection.close();
            }
        }
        catch (SQLException e)
        {
            LOGGER.error("The connection cannot be closed", e);
        }
    }

    protected PreparedStatement prepare(Connection connection, String sql) throws SQLException
    {
        return connection.prepareStatement(sql);
    }

}
