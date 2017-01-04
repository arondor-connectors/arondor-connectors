package com.arondor.viewer.jdbc.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractDAO
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDAO.class);

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void init()
    {
    }

    private final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<Connection>();

    protected Connection getConnection() throws SQLException
    {
        if (threadLocalConnection.get() == null)
        {
            Connection connection = createConnection();
            threadLocalConnection.set(connection);
            LOGGER.trace("Creating new connection : " + threadLocalConnection.get());
        }
        else
        {
            LOGGER.trace("Reusing exisiting connection : " + threadLocalConnection.get());
        }
        if (threadLocalConnection.get().isClosed())
        {
            LOGGER.error("Connection is closed !");
        }
        return threadLocalConnection.get();
    }

    private Connection createConnection() throws SQLException
    {
        try
        {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            return connection;
        }
        catch (SQLException e)
        {
            LOGGER.error("SQL Exception", e);
            throw (e);
        }
    }

    protected void closeConnection() throws SQLException
    {
        if (threadLocalConnection.get() != null)
        {
            LOGGER.trace("Closing existing connection " + threadLocalConnection.get());
            threadLocalConnection.get().close();
            threadLocalConnection.set(null);
        }
    }

    protected CallableStatement createStatement(String statementSQL) throws SQLException
    {
        try
        {
            return getConnection().prepareCall(statementSQL);
        }
        catch (SQLException e)
        {
            throw new SQLException("The procedure cannot be prepared" + statementSQL);
        }
    }

    protected void closeStatement(Statement sp) throws SQLException
    {
        if (sp != null)
        {
            try
            {
                sp.close();
            }
            catch (SQLException e)
            {
                throw new SQLException("The stored procedure " + sp + " cannot be closes", e);
            }
        }
    }
}
