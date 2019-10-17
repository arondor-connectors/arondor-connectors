//package com.arondor.viewer.jdbc.annotation;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.log4j.Logger;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.arondor.viewer.client.api.annotation.Annotation;
//import com.arondor.viewer.client.api.annotation.Annotation.AnnotationType;
//import com.arondor.viewer.client.api.document.DocumentId;
//import com.arondor.viewer.client.api.geometry.PageRelativePosition;
//import com.arondor.viewer.common.document.id.DocumentIdFactory;
//import com.arondor.viewer.filenet.annotation.FileNetAnnotationConverter;
//import com.arondor.viewer.jdbc.util.SqlRunner;
//import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
//
//public class DocumentAnnotationAccessorJDBCTest
//{
//    private static final Logger LOGGER = Logger.getLogger(DocumentAnnotationAccessorJDBCTest.class);
//
//    private DocumentAnnotationAccessorJDBC annotationAccessor;
//
//    private SQLServerDataSource ds;
//
//    private DocumentId documentId = DocumentIdFactory.getInstance().generate();
//
//    @Before
//    public void setUp() throws Exception
//    {
//        annotationAccessor = new DocumentAnnotationAccessorJDBC(documentId);
//        ds = new SQLServerDataSource();
//        ds.setUser("ce_db_user");
//        ds.setPassword("filenet");
//        ds.setServerName("192.168.0.222");
//        ds.setPortNumber(1433);
//        ds.setDatabaseName("ARenderDB_TEST");
//
//        SqlRunner sqlRunner = new SqlRunner(ds, true, true);
//        InputStream resource = getClass().getClassLoader().getResourceAsStream("sql/init-table.sql");
//        Reader reader = new InputStreamReader(resource);
//        sqlRunner.runScript(reader);
//
//        annotationAccessor.setDataSource(ds);
//    }
//
//    @Test
//    public void testAddAndGetOneAnnotation() throws Exception
//    {
//        Annotation annotation = createAnnotation();
//        annotationAccessor.addAnnotation(annotation);
//        List<Annotation> annotations = annotationAccessor.getAnnotationsList();
//        assertEquals(1, annotations.size());
//        assertAnnotationEquals(annotation, annotations.get(0));
//    }
//
//    @Test
//    public void testGetAnnotationOK() throws Exception
//    {
//        Annotation annotation = createAnnotation();
//
//        annotationAccessor.addAnnotation(annotation);
//        Annotation fetchedAnnotation = getAnnotation(annotation.getId());
//        assertAnnotationEquals(annotation, fetchedAnnotation);
//    }
//
//    @Test
//    public void testUpdateAnnotation() throws Exception
//    {
//        Annotation annotation = createAnnotation();
//        annotationAccessor.addAnnotation(annotation);
//        PageRelativePosition updatedPosition = new PageRelativePosition(1, 7, 8, 9);
//        annotation.setPosition(updatedPosition);
//        annotationAccessor.addAnnotation(annotation);
//        List<Annotation> fetchedAnnotations = annotationAccessor.getAnnotationsList();
//        assertAnnotationEquals(annotation, fetchedAnnotations.get(0));
//    }
//
//    @Test
//    public void testDeleteAnnotation() throws Exception
//    {
//        Annotation annotation = createAnnotation();
//
//        annotationAccessor.addAnnotation(annotation);
//        List<Annotation> fetchedAnnotations = annotationAccessor.getAnnotationsList();
//        assertEquals(1, fetchedAnnotations.size());
//        annotationAccessor.deleteAnnotation(annotation);
//        fetchedAnnotations = annotationAccessor.getAnnotationsList();
//        assertEquals(0, fetchedAnnotations.size());
//    }
//
//    private void assertAnnotationEquals(Annotation expected, Annotation actual)
//    {
//        assertEquals(expected.getDocumentId().toString().toUpperCase(), actual.getDocumentId().toString().toUpperCase());
//        assertEquals(expected.getId().toString().toUpperCase(), actual.getId().toString().toUpperCase());
//        assertEquals(expected.getPosition(), actual.getPosition());
//    }
//
//    private Annotation createAnnotation()
//    {
//        Annotation annotation = new Annotation(1, AnnotationType.PostIt, new PageRelativePosition(1, 1, 1, 1), " ",
//                null);
//        annotation.setId(UUID.randomUUID().toString());
//        annotation.setDocumentId(documentId);
//        return annotation;
//    }
//
//    protected Annotation getAnnotation(String annotationId)
//    {
//        List<Annotation> annotations = new ArrayList<Annotation>();
//        Connection connection = null;
//        Statement stmt = null;
//        try
//        {
//            connection = ds.getConnection();
//            stmt = connection.createStatement();
//            ResultSet rs = stmt.executeQuery("select CONTENTSTRING from " + "ANNOTATIONS" + " where DOC_ID='"
//                    + documentId.toString() + "'");
//
//            while (rs.next())
//            {
//                String string = rs.getString(1);
//                return new FileNetAnnotationConverter().parseAnnotation(documentId, IOUtils.toInputStream(string));
//            }
//            rs.close();
//        }
//        catch (Exception e)
//        {
//            LOGGER.error("The annotation " + annotationId + " cannot be fetched form DB for documentId=" + documentId,
//                    e);
//        }
//        finally
//        {
//            // annotationAccessor.closeStatement(stmt);
//            // annotationAccessor.closeConnection(connection);
//        }
//        return annotations.size() > 1 ? annotations.get(0) : null;
//    }
//
//    @After
//    public void tearDown() throws SQLException
//    {
//        SqlRunner sqlRunner = new SqlRunner(ds, true, true);
//        InputStream resource = getClass().getClassLoader().getResourceAsStream("sql/clean-table.sql");
//        Reader reader = new InputStreamReader(resource);
//        sqlRunner.runScript(reader);
//    }
//
//    @Test
//    public void testAdaptStringWithCaseSensitiveEnabled() throws Exception
//    {
//        annotationAccessor.setCaseSensitive(true);
//        assertEquals("COUCOU", annotationAccessor.adapt("COUCOU"));
//    }
//
//    @Test
//    public void testAdaptStringWithCaseSensitiveDisabled() throws Exception
//    {
//        annotationAccessor.setCaseSensitive(false);
//        assertEquals("coucou", annotationAccessor.adapt("COUCOU"));
//    }
// }