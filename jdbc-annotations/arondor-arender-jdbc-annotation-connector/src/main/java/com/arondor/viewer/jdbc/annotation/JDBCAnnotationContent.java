package com.arondor.viewer.jdbc.annotation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.arondor.viewer.annotation.exceptions.AnnotationCredentialsException;
import com.arondor.viewer.annotation.exceptions.AnnotationNotAvailableException;
import com.arondor.viewer.annotation.exceptions.InvalidAnnotationFormatException;
import com.arondor.viewer.client.api.document.DocumentId;
import com.arondor.viewer.client.api.document.id.DocumentIdParameter;
import com.arondor.viewer.common.document.id.DocumentIdFactory;
import com.arondor.viewer.xfdf.annotation.SerializedAnnotationContent;

public class JDBCAnnotationContent extends JDBCDAO implements SerializedAnnotationContent
{
    private static final Logger LOGGER = Logger.getLogger(JDBCAnnotationContent.class);

    private DataSource dataSource;

    private String tableName = "ANNOTATIONS";

    private boolean caseSensitive = true;

    private boolean reverseDocumentId = false;

    private String useDocumentIdArgument = "documentId";

    private DocumentId documentId;

    public JDBCAnnotationContent(DocumentId documentId)
    {
        this.documentId = documentId;
    }

    @Override
    public InputStream get() throws InvalidAnnotationFormatException
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            LOGGER.debug("Getting all annotations for document=" + documentId);
            connection = dataSource.getConnection();
            stmt = connection.prepareStatement("select CONTENT from " + getTableName() + " where DOC_ID=?");
            stmt.setString(1, adaptDocumentId(documentId));
            rs = stmt.executeQuery();
            if (rs.next())
            {
                Blob blob = rs.getBlob(1);
                return new ByteArrayInputStream(IOUtils.toByteArray(blob.getBinaryStream()));
            }
            LOGGER.info("No annotation can be found for document=" + documentId);
        }
        catch (SQLException e)
        {
            LOGGER.error("Annotations cannot be fetched for documentId=" + documentId, e);
        }
        catch (IOException e)
        {
            LOGGER.error("Annotations cannot be fetched for documentId=" + documentId, e);
        }
        finally
        {
            close(rs);
            closeStatement(stmt);
            closeConnection(connection);
        }
        return null;
    }

    @Override
    public void update(InputStream content) throws InvalidAnnotationFormatException, AnnotationCredentialsException,
            AnnotationNotAvailableException
    {
        try
        {
            String contentAsString = IOUtils.toString(content);
            int updated = doUpdate(contentAsString);
            LOGGER.info(updated + " lines have been updated");
            if (updated < 1)
            {
                doCreate(contentAsString);
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Annotations cannot be inserted for documentId=" + documentId, e);
        }
    }

    private void doCreate(String contentAsString)
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        try
        {
            connection = dataSource.getConnection();
            String insert = "insert into " + getTableName() + " (DOC_ID,CONTENT) values (?,?) ";
            stmt = connection.prepareStatement(insert);
            stmt.setString(1, adaptDocumentId(documentId));
            stmt.setBlob(2, new ByteArrayInputStream(contentAsString.getBytes()));
            stmt.execute();
            LOGGER.debug("Anntotations have been inserted for document=" + documentId);
        }
        catch (SQLException e)
        {
            LOGGER.error("Annotations cannot be inserted for documentId=" + documentId, e);
        }
        finally
        {
            closeStatement(stmt);
            closeConnection(connection);
        }
    }

    private int doUpdate(String contentAsString)
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        try
        {
            connection = dataSource.getConnection();

            String insert = "update " + getTableName() + " set CONTENT=? where DOC_ID=?";
            stmt = connection.prepareStatement(insert);
            stmt.setBlob(1, new ByteArrayInputStream(contentAsString.getBytes()));
            stmt.setString(2, adaptDocumentId(documentId));
            return stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            LOGGER.error("Annotations cannot be updated for documentId=" + documentId, e);
        }
        finally
        {
            closeStatement(stmt);
            closeConnection(connection);
        }
        return 0;
    }

    protected String adaptDocumentId(DocumentId documentId)
    {
        String string;
        if (isReverseDocumentId())
        {
            string = getDocumentIdParameter(documentId);
        }
        else
        {
            string = documentId.toString();
        }
        if (!isCaseSensitive() && string != null)
        {
            return string.toLowerCase();
        }
        return string;
    }

    private String getDocumentIdParameter(DocumentId documentId)
    {
        List<DocumentIdParameter> parameters = DocumentIdFactory.getInstance().revert(documentId);
        for (DocumentIdParameter parameter : parameters)
        {
            if (parameter.getKey().equals(getUseDocumentIdArgument()))
            {
                return parameter.getValue();
            }
        }
        throw new IllegalArgumentException("No argument : " + getUseDocumentIdArgument() + " in " + parameters);
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public boolean isCaseSensitive()
    {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }

    public DocumentId getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(DocumentId documentId)
    {
        this.documentId = documentId;
    }

    public boolean isReverseDocumentId()
    {
        return reverseDocumentId;
    }

    public void setReverseDocumentId(boolean reverseDocumentId)
    {
        this.reverseDocumentId = reverseDocumentId;
    }

    public String getUseDocumentIdArgument()
    {
        return useDocumentIdArgument;
    }

    public void setUseDocumentIdArgument(String useDocumentIdArgument)
    {
        this.useDocumentIdArgument = useDocumentIdArgument;
    }
}