//package com.arondor.viewer.jdbc.annotation;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.ObjectOutputStream;
//import java.io.Serializable;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import javax.sql.DataSource;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.log4j.Logger;
//
//import com.arondor.viewer.client.api.annotation.Annotation;
//import com.arondor.viewer.client.api.annotation.AnnotationFormatNotSupportedException;
//import com.arondor.viewer.client.api.document.DocumentId;
//import com.arondor.viewer.client.api.document.DocumentNotAvailableException;
//import com.arondor.viewer.filenet.annotation.FileNetAnnotationConverter;
//import com.arondor.viewer.rendition.api.annotation.DocumentAnnotationAccessor;
//import com.arondor.viewer.rendition.api.annotation.InvalidAnnotationException;
//import com.arondor.viewer.rendition.api.document.DocumentAccessor;
//
///**
// * Annotation accessor allowing to access, store and delete annotations on a
// * JDBC referential
// */
//public class DocumentAnnotationAccessorJDBC extends JDBCDAO implements DocumentAnnotationAccessor, Serializable
//{
//    private static final long serialVersionUID = 1L;
//
//    private static final Logger LOGGER = Logger.getLogger(DocumentAnnotationAccessorJDBC.class);
//
//    private FileNetAnnotationConverter converter = new FileNetAnnotationConverter();
//
//    private final DocumentId documentId;
//
//    private DataSource dataSource;
//
//    private String tableName = "ANNOTATIONS";
//
//    private boolean caseSensitive = false;
//
//    public DocumentAnnotationAccessorJDBC(DocumentId documentId)
//    {
//        this.documentId = documentId;
//    }
//
//    public DocumentAnnotationAccessorJDBC(DocumentAccessor documentAccessor)
//    {
//        this(documentAccessor.getUUID());
//    }
//
//    @Override
//    public void addAnnotation(Annotation annotation) throws InvalidAnnotationException, DocumentNotAvailableException,
//            AnnotationFormatNotSupportedException
//    {
//        Connection connection = null;
//        Statement stmt = null;
//        try
//        {
//            connection = dataSource.getConnection();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ObjectOutputStream file = new ObjectOutputStream(baos);
//            file.writeObject(annotation);
//            file.close();
//            stmt = connection.createStatement();
//
//            boolean updated = false;
//            if (annotation.getLastUpdateDate() != null)
//            {
//                updated = udpateExistingAnnotation(annotation, stmt);
//            }
//
//            if (!updated)
//            {
//                createAndSaveAnnotation(annotation, stmt);
//            }
//        }
//        catch (Exception e)
//        {
//            LOGGER.error("The annotation " + annotation.getId() + " cannot be added for documentId=" + documentId, e);
//            throw new DocumentNotAvailableException("The annotation " + annotation.getId()
//                    + " cannot be added for documentId=" + documentId, e);
//        }
//        finally
//        {
//            closeStatement(stmt);
//            closeConnection(connection);
//        }
//    }
//
//    private void createAndSaveAnnotation(Annotation annotation, Statement stmt)
//            throws AnnotationFormatNotSupportedException, SQLException, IOException
//    {
//        annotation.setLastUpdateDate(new Date());
//        InputStream serializeAnnotation = converter.serializeAnnotation(annotation.getDocumentId(), annotation,
//                annotation.getId());
//        stmt.executeUpdate("insert into " + tableName + " (anno_id, doc_id , contentstring) values('"
//                + adapt(annotation.getId()) + "','" + adapt(documentId.toString()) + "','"
//                + IOUtils.toString(serializeAnnotation) + "' )");
//        LOGGER.debug("The annotation " + annotation.getId() + " has been created for documentId=" + documentId);
//    }
//
//    private boolean udpateExistingAnnotation(Annotation annotation, Statement stmt)
//            throws AnnotationFormatNotSupportedException, SQLException, IOException
//    {
//        annotation.setLastUpdateDate(new Date());
//        InputStream serializeAnnotation = converter.serializeAnnotation(annotation.getDocumentId(), annotation,
//                annotation.getId());
//
//        int result = stmt.executeUpdate("update " + tableName + " set CONTENTSTRING='"
//                + IOUtils.toString(serializeAnnotation) + "' WHERE ANNO_ID='" + adapt(annotation.getId())
//                + "' AND DOC_ID='" + adapt(documentId.toString()) + "'");
//        boolean updated = result == 1;
//
//        LOGGER.debug("The annotation " + annotation.getId() + " update for documentId=" + documentId + ", gave result="
//                + result + ", success=" + (updated ? "yes" : "no"));
//        return updated;
//    }
//
//    @Override
//    public List<Annotation> getAnnotationsList() throws DocumentNotAvailableException,
//            AnnotationFormatNotSupportedException
//    {
//        ArrayList<Annotation> annotations = new ArrayList<Annotation>();
//        Connection connection = null;
//        Statement stmt = null;
//        try
//        {
//            connection = dataSource.getConnection();
//            stmt = connection.createStatement();
//            ResultSet rs = stmt.executeQuery("select CONTENTSTRING from " + tableName + " where DOC_ID='"
//                    + adapt(documentId.toString()) + "'");
//
//            while (rs.next())
//            {
//                String string = rs.getString(1);
//                Annotation parseAnnotation = converter.parseAnnotation(documentId, IOUtils.toInputStream(string));
//                annotations.add(parseAnnotation);
//            }
//            rs.close();
//        }
//        catch (SQLException e)
//        {
//            LOGGER.error("Annotations cannot be fetched for documentId=" + documentId, e);
//        }
//        finally
//        {
//            closeStatement(stmt);
//            closeConnection(connection);
//        }
//        return annotations;
//    }
//
//    @Override
//    public void deleteAnnotation(Annotation annotation) throws InvalidAnnotationException,
//            DocumentNotAvailableException
//    {
//        Connection connection = null;
//        Statement stmt = null;
//        try
//        {
//            connection = dataSource.getConnection();
//            stmt = connection.createStatement();
//            stmt.executeUpdate("delete from " + tableName + " where anno_id='" + adapt(annotation.getId()) + "'");
//            LOGGER.debug("The annotation " + annotation.getId() + " has been deleted for documentId=" + documentId);
//        }
//        catch (Exception e)
//        {
//            LOGGER.error("The annotation " + annotation.getId() + " cannot be deleted for documentId=" + documentId, e);
//        }
//        finally
//        {
//            closeStatement(stmt);
//            closeConnection(connection);
//        }
//    }
//
//    protected String adapt(String string)
//    {
//        if (!isCaseSensitive() && string != null)
//        {
//            return string.toLowerCase();
//        }
//        return string;
//    }
//
//    public void setDataSource(DataSource dataSource)
//    {
//        this.dataSource = dataSource;
//    }
//
//    public void setTableName(String tableName)
//    {
//        this.tableName = tableName;
//    }
//
//    public boolean isCaseSensitive()
//    {
//        return caseSensitive;
//    }
//
//    public void setCaseSensitive(boolean caseSensitive)
//    {
//        LOGGER.info("Setting annotation accessor case " + (!caseSensitive ? "in" : "") + "sensitive");
//        this.caseSensitive = caseSensitive;
//    }
//
//    public FileNetAnnotationConverter getConverter()
//    {
//        return converter;
//    }
//
//    public void setConverter(FileNetAnnotationConverter converter)
//    {
//        this.converter = converter;
//    }
// }