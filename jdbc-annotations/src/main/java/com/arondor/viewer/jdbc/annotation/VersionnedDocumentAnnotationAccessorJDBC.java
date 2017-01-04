//package com.arondor.viewer.jdbc.annotation;
//
//import java.io.InputStream;
//import java.io.Serializable;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import javax.sql.DataSource;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.log4j.Logger;
//
//import com.arondor.viewer.annotation.exceptions.AnnotationNotAvailableException;
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
// * JDBC referential. This annotation accessor allows to version all document
// * annotations
// */
//public class VersionnedDocumentAnnotationAccessorJDBC extends JDBCDAO implements DocumentAnnotationAccessor,
//        Serializable
//{
//    private static final long serialVersionUID = 1L;
//
//    private static final Logger LOGGER = Logger.getLogger(VersionnedDocumentAnnotationAccessorJDBC.class);
//
//    private static final FileNetAnnotationConverter converter = new FileNetAnnotationConverter();
//
//    private DataSource dataSource;
//
//    private String tableName = "VANNOTATIONS";
//
//    private URLExtraArgumentParser parser = new URLExtraArgumentParser();
//
//    private int versionTimeout = 1000;
//
//    private DocumentId documentId;
//
//    private String effectiveDocumentId;
//
//    private AnnotationVersion currentVersion;
//
//    private String userId;
//
//    public VersionnedDocumentAnnotationAccessorJDBC(DocumentId documentId)
//    {
//        this.documentId = documentId;
//        parseExtraArguments(documentId);
//    }
//
//    public VersionnedDocumentAnnotationAccessorJDBC(DocumentAccessor documentAccessor)
//    {
//        this(documentAccessor.getUUID());
//    }
//
//    protected void parseExtraArguments(DocumentId documentId)
//    {
//        effectiveDocumentId = parser.parse(documentId, "doc_id");
//        currentVersion = new AnnotationVersion(Integer.parseInt(parser.parse(documentId, "version")), 0);
//        userId = parser.parse(documentId, "user_id");
//
//        LOGGER.debug("New " + this.getClass().getName() + ", doc_id=" + effectiveDocumentId + ", version="
//                + currentVersion + ", user_id=" + userId);
//    }
//
//    public synchronized void addAnnotation(Annotation annotation) throws InvalidAnnotationException,
//            DocumentNotAvailableException, AnnotationFormatNotSupportedException
//    {
//        List<Annotation> existingAnnotations = getAnnotationsList();
//        updateAnnotations(annotation, existingAnnotations);
//
//        boolean appendCurrentVersion = hasToAppendCurrentVersion();
//        if (appendCurrentVersion && isAnnotationCreation(annotation))
//        {
//            List<Annotation> newAnnotation = new ArrayList<Annotation>();
//            newAnnotation.add(annotation);
//            saveAnnotations(newAnnotation, currentVersion.getId());
//        }
//        else if (appendCurrentVersion)
//        {
//            doUpdateAnnotation(annotation, currentVersion.getId());
//        }
//        else
//        {
//            int nextVersion = computeNextVersion();
//            saveAnnotations(existingAnnotations, nextVersion);
//        }
//    }
//
//    private boolean hasToAppendCurrentVersion()
//    {
//        return (System.currentTimeMillis() - currentVersion.getTimeStamp()) < versionTimeout;
//    }
//
//    private void updateAnnotations(Annotation newAnnotation, List<Annotation> existingAnnotations)
//    {
//        if (isAnnotationCreation(newAnnotation))
//        {
//            newAnnotation.setCreator(userId != null ? userId : "Unknown");
//            existingAnnotations.add(newAnnotation);
//        }
//        else
//        {
//            for (int idx = 0; idx < existingAnnotations.size(); idx++)
//            {
//                if (existingAnnotations.get(idx).getId().toUpperCase().equals(newAnnotation.getId().toUpperCase()))
//                {
//                    existingAnnotations.set(idx, newAnnotation);
//                    break;
//                }
//            }
//        }
//    }
//
//    private void saveAnnotations(List<Annotation> existingAnnotations, int version) throws InvalidAnnotationException
//    {
//        Connection connection = null;
//        PreparedStatement stmt = null;
//        try
//        {
//            connection = dataSource.getConnection();
//            String query = "insert into " + tableName
//                    + " (anno_id,doc_id,user_id,version, contentstring) values(?,?,?,?,?)";
//            stmt = prepare(connection, query);
//
//            for (Annotation annotation : existingAnnotations)
//            {
//                annotation.setLastUpdateDate(new Date());
//                InputStream serializeAnnotation = converter.serializeAnnotation(annotation.getDocumentId(), annotation,
//                        annotation.getId());
//
//                stmt.setString(1, annotation.getId());
//                stmt.setString(2, effectiveDocumentId);
//                stmt.setString(3, userId);
//                stmt.setInt(4, version);
//                stmt.setString(5, IOUtils.toString(serializeAnnotation));
//
//                stmt.executeUpdate();
//                LOGGER.debug("The annotation " + annotation.getId() + " has been created for documentId="
//                        + effectiveDocumentId + " and version=" + version);
//                serializeAnnotation.close();
//            }
//
//            currentVersion = new AnnotationVersion(version, System.currentTimeMillis());
//        }
//        catch (Exception e)
//        {
//            String msg = "Could not update annotations for id=" + effectiveDocumentId;
//            LOGGER.error(msg, e);
//            throw new InvalidAnnotationException(msg, e);
//        }
//        finally
//        {
//            closeStatement(stmt);
//            closeConnection(connection);
//        }
//    }
//
//    private void doUpdateAnnotation(Annotation annotation, int version) throws InvalidAnnotationException
//    {
//        Connection connection = null;
//        PreparedStatement stmt = null;
//        try
//        {
//            connection = dataSource.getConnection();
//
//            String query = "update " + tableName + " set contentstring=? where doc_id=? and anno_id=? and version=?";
//            stmt = prepare(connection, query);
//
//            annotation.setLastUpdateDate(new Date());
//            InputStream serializeAnnotation = converter.serializeAnnotation(annotation.getDocumentId(), annotation,
//                    annotation.getId());
//
//            stmt.setString(1, IOUtils.toString(serializeAnnotation));
//            stmt.setString(2, effectiveDocumentId);
//            stmt.setString(3, annotation.getId());
//            stmt.setInt(4, version);
//
//            stmt.executeUpdate();
//            LOGGER.debug("The annotation " + annotation.getId() + " has been updated for documentId="
//                    + effectiveDocumentId + " and version=" + version + ", query=" + query);
//            serializeAnnotation.close();
//
//            currentVersion = new AnnotationVersion(version, System.currentTimeMillis());
//        }
//        catch (Exception e)
//        {
//            String msg = "Could not update annotations for id=" + effectiveDocumentId;
//            LOGGER.error(msg, e);
//            throw new InvalidAnnotationException(msg, e);
//        }
//        finally
//        {
//            closeStatement(stmt);
//            closeConnection(connection);
//        }
//    }
//
//    protected PreparedStatement prepare(boolean isCreate) throws SQLException
//    {
//        String query = "";
//        if (isCreate)
//        {
//            query = "insert into " + tableName + " (anno_id,doc_id,user_id,version, contentstring) values(?,?,?,?,?)";
//        }
//        else
//        {
//            query = "update " + tableName + " set contentstring=? where doc_id=? and anno_id=? and version=?";
//        }
//        return super.prepare(dataSource.getConnection(), query);
//    }
//
//    protected int computeNextVersion() throws InvalidAnnotationException
//    {
//        Connection connection = null;
//        PreparedStatement stmt = null;
//        try
//        {
//            connection = dataSource.getConnection();
//            stmt = prepare(connection, "select max(VERSION) from " + tableName + " WHERE DOC_ID=? ");
//            stmt.setString(1, effectiveDocumentId);
//
//            ResultSet result = stmt.executeQuery();
//            int nextVersion = result.next() ? result.getInt(1) + 1 : 0;
//            LOGGER.debug("Next version for doc " + effectiveDocumentId + " is " + nextVersion);
//            result.close();
//            return nextVersion;
//        }
//        catch (SQLException e)
//        {
//            throw new InvalidAnnotationException("Could not compute next version for id=" + effectiveDocumentId, e);
//        }
//        finally
//        {
//            closeStatement(stmt);
//            closeConnection(connection);
//        }
//    }
//
//    public synchronized List<Annotation> getAnnotationsList() throws DocumentNotAvailableException,
//            AnnotationFormatNotSupportedException
//    {
//        return getAnnotations(currentVersion.getId());
//    }
//
//    protected List<Annotation> getAnnotations(int version) throws AnnotationFormatNotSupportedException,
//            DocumentNotAvailableException
//    {
//        ArrayList<Annotation> annotations = new ArrayList<Annotation>();
//        Connection connection = null;
//        PreparedStatement stmt = null;
//        try
//        {
//            connection = dataSource.getConnection();
//            stmt = prepare(connection, "select CONTENTSTRING from " + tableName + " where DOC_ID=? and VERSION=?");
//            stmt.setString(1, effectiveDocumentId);
//            stmt.setInt(2, version);
//            ResultSet rs = stmt.executeQuery();
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
//            String msg = "Annotations cannot be fetched for id=" + effectiveDocumentId + " and version=" + version;
//            LOGGER.error(msg, e);
//            throw new DocumentNotAvailableException(msg, e);
//        }
//        finally
//        {
//            closeStatement(stmt);
//            closeConnection(connection);
//        }
//        return annotations;
//    }
//
//    public synchronized void deleteAnnotation(Annotation annotation) throws InvalidAnnotationException,
//            DocumentNotAvailableException, AnnotationNotAvailableException
//    {
//        if (hasToAppendCurrentVersion())
//        {
//            doDeleteAnnotation(annotation, currentVersion.getId());
//        }
//        else
//        {
//            try
//            {
//                List<Annotation> existingAnnotations = getAnnotations(currentVersion.getId());
//                removeAnnotationFromList(annotation.getId(), existingAnnotations);
//                saveAnnotations(existingAnnotations, computeNextVersion());
//            }
//            catch (AnnotationFormatNotSupportedException e)
//            {
//                throw new AnnotationNotAvailableException("Fallback to AnnotationNotAvailableException: ", e);
//            }
//        }
//    }
//
//    private void doDeleteAnnotation(Annotation annotation, int version) throws InvalidAnnotationException
//    {
//        Connection connection = null;
//        PreparedStatement stmt = null;
//        try
//        {
//            connection = dataSource.getConnection();
//
//            String query = "delete from " + tableName + " where doc_id=? and anno_id=? and version=?";
//            stmt = prepare(connection, query);
//
//            annotation.setLastUpdateDate(new Date());
//            InputStream serializeAnnotation = converter.serializeAnnotation(annotation.getDocumentId(), annotation,
//                    annotation.getId());
//
//            stmt.setString(1, effectiveDocumentId);
//            stmt.setString(2, annotation.getId());
//            stmt.setInt(3, version);
//
//            stmt.executeUpdate();
//            LOGGER.debug("The annotation " + annotation.getId() + " has been deleted for documentId="
//                    + effectiveDocumentId + " and version=" + version + ", query=" + query);
//            serializeAnnotation.close();
//
//            currentVersion = new AnnotationVersion(version, System.currentTimeMillis());
//        }
//        catch (Exception e)
//        {
//            String msg = "Could not delete annotations for id=" + effectiveDocumentId + " and anno_id="
//                    + annotation.getId();
//            LOGGER.error(msg, e);
//            throw new InvalidAnnotationException(msg, e);
//        }
//        finally
//        {
//            closeStatement(stmt);
//            closeConnection(connection);
//        }
//    }
//
//    private void removeAnnotationFromList(String id, List<Annotation> existingAnnotations)
//    {
//        for (int i = 0; i < existingAnnotations.size(); i++)
//        {
//            if (existingAnnotations.get(i).getId().toUpperCase().equals(id.toUpperCase()))
//            {
//                existingAnnotations.remove(i);
//            }
//        }
//    }
//
//    private boolean isAnnotationCreation(Annotation annotation)
//    {
//        return annotation.getLastUpdateDate() == null;
//    }
//
//    protected AnnotationVersion getVersion()
//    {
//        return currentVersion;
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
//    public int getVersionTimeout()
//    {
//        return versionTimeout;
//    }
// }