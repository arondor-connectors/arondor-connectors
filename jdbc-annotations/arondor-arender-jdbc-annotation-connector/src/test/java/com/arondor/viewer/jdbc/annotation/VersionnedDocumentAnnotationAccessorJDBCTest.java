//package com.arondor.viewer.jdbc.annotation;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
//import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
//import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
//
//import com.arondor.viewer.client.api.annotation.Annotation;
//import com.arondor.viewer.client.api.annotation.Annotation.AnnotationType;
//import com.arondor.viewer.client.api.document.DocumentId;
//import com.arondor.viewer.client.api.document.id.DocumentIdParameter;
//import com.arondor.viewer.client.api.geometry.PageRelativePosition;
//import com.arondor.viewer.common.document.id.DocumentIdFactory;
//import com.arondor.viewer.common.document.id.URLDocumentIdParameter;
//
//public class VersionnedDocumentAnnotationAccessorJDBCTest
//{
//    private VersionnedDocumentAnnotationAccessorJDBC annotationAccessor;
//
//    private EmbeddedDatabase ds;
//
//    private DocumentId documentId;
//
//    @Before
//    public void setUp() throws Exception
//    {
//        ds = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL)
//                .addScript("classpath:hsqldb/versionned-annotations.sql").build();
//        annotationAccessor = buildAnnotationAccessor(0);
//    }
//
//    private VersionnedDocumentAnnotationAccessorJDBC buildAnnotationAccessor(int version)
//    {
//        List<DocumentIdParameter> parameters = new ArrayList<DocumentIdParameter>();
//        String docUrl = "http://myhost:myport/mydocument23#doc_id:434#version:" + version + "#user_id:Victor";
//        parameters.add(new URLDocumentIdParameter("url", docUrl));
//        documentId = DocumentIdFactory.getInstance().generate(parameters);
//        VersionnedDocumentAnnotationAccessorJDBC annotationAccessor = new VersionnedDocumentAnnotationAccessorJDBC(
//                documentId);
//        annotationAccessor.setDataSource(ds);
//        return annotationAccessor;
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
//        assertEquals(1, annotationAccessor.getVersion().getId());
//    }
//
//    @Test
//    public void testAddTwoAnnotationsWithSleep() throws Exception
//    {
//        Annotation annotation1 = createAnnotation();
//        annotationAccessor.addAnnotation(annotation1);
//
//        sleep();
//
//        Annotation annotation2 = createAnnotation();
//        annotation2.setPosition(new PageRelativePosition(1, 7, 8, 9));
//        annotationAccessor.addAnnotation(annotation2);
//
//        List<Annotation> fetchedAnnotations = annotationAccessor.getAnnotations(1);
//        assertAnnotationEquals(annotation1, fetchedAnnotations.get(0));
//
//        fetchedAnnotations = annotationAccessor.getAnnotationsList();
//        assertAnnotationEquals(annotation1, fetchedAnnotations.get(0));
//        assertAnnotationEquals(annotation2, fetchedAnnotations.get(1));
//        assertEquals(2, annotationAccessor.getVersion().getId());
//    }
//
//    @Test
//    public void testAddTwoAnnotationsInTheSameVersion() throws Exception
//    {
//        Annotation annotation1 = createAnnotation();
//        annotationAccessor.addAnnotation(annotation1);
//
//        Annotation annotation2 = createAnnotation();
//        annotation2.setPosition(new PageRelativePosition(1, 7, 8, 9));
//        annotationAccessor.addAnnotation(annotation2);
//
//        List<Annotation> fetchedAnnotations = annotationAccessor.getAnnotationsList();
//        assertEquals(2, fetchedAnnotations.size());
//        assertAnnotationEquals(annotation1, fetchedAnnotations.get(0));
//        assertAnnotationEquals(annotation2, fetchedAnnotations.get(1));
//        assertEquals(1, annotationAccessor.getVersion().getId());
//    }
//
//    @Test
//    public void testAddAndUpdateAnnotationsInSameVersion() throws Exception
//    {
//        Annotation annotation1 = createAnnotation();
//        annotationAccessor.addAnnotation(annotation1);
//
//        annotation1.setPosition(new PageRelativePosition(1, 7, 8, 9));
//        annotationAccessor.addAnnotation(annotation1);
//
//        List<Annotation> fetchedAnnotations = annotationAccessor.getAnnotationsList();
//        assertEquals(fetchedAnnotations, annotationAccessor.getAnnotations(1));
//        assertEquals(1, fetchedAnnotations.size());
//        assertAnnotationEquals(annotation1, fetchedAnnotations.get(0));
//        assertEquals(1, annotationAccessor.getVersion().getId());
//    }
//
//    @Test
//    public void testDoUpdateAnnotationMaintainHistory() throws Exception
//    {
//        Annotation annotation = createAnnotation();
//        annotationAccessor.addAnnotation(annotation);
//        sleep();
//        Annotation annotationUpdated = buildUpdatedAnnotation(annotation);
//        annotationAccessor.addAnnotation(annotationUpdated);
//
//        List<Annotation> fetchedAnnotations = annotationAccessor.getAnnotationsList();
//        assertEquals(1, fetchedAnnotations.size());
//        assertAnnotationEquals(annotationUpdated, fetchedAnnotations.get(0));
//        assertEquals(2, annotationAccessor.getVersion().getId());
//
//        fetchedAnnotations = annotationAccessor.getAnnotations(1);
//        assertEquals(1, fetchedAnnotations.size());
//        assertAnnotationEquals(annotation, fetchedAnnotations.get(0));
//    }
//
//    @Test
//    public void testDoDeleteAnnotationInDistinctVersions() throws Exception
//    {
//        Annotation annotation = createAnnotation();
//        annotationAccessor.addAnnotation(annotation);
//        sleep();
//
//        checkVersion(1, annotation);
//
//        annotationAccessor.deleteAnnotation(annotation);
//        assertEquals("The current version should be: 2", 2, annotationAccessor.getVersion().getId());
//        checkVersion(0);
//        checkVersion(1, annotation);
//        checkVersion(2);
//    }
//
//    @Test
//    public void testDoDeleteAnnotationInSameVersion() throws Exception
//    {
//        Annotation annotation = createAnnotation();
//        annotationAccessor.addAnnotation(annotation);
//        checkVersion(1, annotation);
//
//        annotationAccessor.deleteAnnotation(annotation);
//        checkVersion(1);
//    }
//
//    @Test
//    public void testFunctionalScenario() throws Exception
//    {
//        Annotation annotation = createAnnotation();
//        annotationAccessor.addAnnotation(annotation);
//        sleep();
//
//        Annotation annotationUpdated = buildUpdatedAnnotation(annotation);
//        annotationAccessor.addAnnotation(annotationUpdated);
//        sleep();
//
//        annotationAccessor.deleteAnnotation(annotation);
//
//        assertEquals(3, annotationAccessor.getVersion().getId());
//        List<Annotation> fetchedAnnotations = annotationAccessor.getAnnotations(1);
//        assertEquals(1, fetchedAnnotations.size());
//        fetchedAnnotations = annotationAccessor.getAnnotations(2);
//        assertEquals(1, fetchedAnnotations.size());
//
//        fetchedAnnotations = annotationAccessor.getAnnotations(3);
//        assertEquals(0, fetchedAnnotations.size());
//    }
//
//    @Test
//    public void testFunctionalScenario2() throws Exception
//    {
//        // STEP A
//        assertEquals(0, annotationAccessor.getAnnotations(0).size());
//
//        // STEP B
//        Annotation annotation1 = createAnnotation();
//        annotationAccessor.addAnnotation(annotation1);
//        sleep();
//        assertEquals(1, annotationAccessor.getAnnotations(1).size());
//
//        // STEP C
//        Annotation annotation2 = createAnnotation();
//        annotationAccessor.addAnnotation(annotation2);
//        assertEquals("The current version should be: 2", 2, annotationAccessor.getVersion().getId());
//        checkVersion(2, annotation1, annotation2);
//        checkVersion(0);
//        checkVersion(1, annotation1);
//
//        // STEP D
//        annotationAccessor = buildAnnotationAccessor(1);
//        assertEquals("The current version should be: 1", 1, annotationAccessor.getVersion().getId());
//        checkVersion(1, annotation1);
//        checkVersion(0);
//        checkVersion(2, annotation1, annotation2);
//
//        // STEP E
//        Annotation annotation3 = createAnnotation();
//        annotationAccessor.addAnnotation(annotation3);
//        Annotation annotation4 = createAnnotation();
//        annotationAccessor.addAnnotation(annotation4);
//        assertEquals("The current version should be: 3", 3, annotationAccessor.getVersion().getId());
//        checkVersion(3, annotation1, annotation3, annotation4);
//        checkVersion(0);
//        checkVersion(1, annotation1);
//        checkVersion(2, annotation1, annotation2);
//
//        // STEP F
//        annotationAccessor = buildAnnotationAccessor(0);
//        assertEquals("The current version should be: 0", 0, annotationAccessor.getVersion().getId());
//        checkVersion(0);
//        checkVersion(1, annotation1);
//        checkVersion(2, annotation1, annotation2);
//        checkVersion(3, annotation1, annotation3, annotation4);
//
//        // STEP G
//        Annotation annotation5 = createAnnotation();
//        annotationAccessor.addAnnotation(annotation5);
//        Annotation annotation6 = createAnnotation();
//        annotationAccessor.addAnnotation(annotation6);
//        assertEquals("The current version should be: 4", 4, annotationAccessor.getVersion().getId());
//        checkVersion(4, annotation5, annotation6);
//        checkVersion(0);
//        checkVersion(1, annotation1);
//        checkVersion(2, annotation1, annotation2);
//        checkVersion(3, annotation1, annotation3, annotation4);
//
//        annotationAccessor.deleteAnnotation(annotation1);
//    }
//
//    private Annotation buildUpdatedAnnotation(Annotation annotation)
//    {
//        Annotation annotationUpdated = createAnnotation();
//        annotationUpdated.setId(annotation.getId());
//        annotationUpdated.setLastUpdateDate(new Date());
//        annotationUpdated.setPosition(new PageRelativePosition(56, 45, 78, 95));
//        return annotationUpdated;
//    }
//
//    private void checkVersion(int version, Annotation... annotations) throws Exception
//    {
//        List<Annotation> annotationVersion = annotationAccessor.getAnnotations(version);
//        assertEquals(annotations.length, annotationVersion.size());
//        for (int i = 0; i < annotations.length; i++)
//        {
//            assertAnnotationEquals(annotations[i], annotationVersion.get(i));
//        }
//    }
//
//    private void assertAnnotationEquals(Annotation expected, Annotation actual)
//    {
//        assertNotNull(actual.getDocumentId());
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
//    private void sleep()
//    {
//        try
//        {
//            Thread.sleep(1000);
//        }
//        catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    @After
//    public void cleanup() throws Exception
//    {
//        ds.getConnection().prepareCall("drop table VANNOTATIONS").execute();
//        ds.shutdown();
//    }
// }
