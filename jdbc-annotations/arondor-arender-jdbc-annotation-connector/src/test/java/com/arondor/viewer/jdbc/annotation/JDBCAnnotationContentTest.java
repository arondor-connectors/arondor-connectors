package com.arondor.viewer.jdbc.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.arondor.viewer.client.api.document.DocumentId;
import com.arondor.viewer.client.api.document.DocumentIdParser;

public class JDBCAnnotationContentTest
{
    private JDBCAnnotationContent content;

    private static DataSource ds;

    @BeforeClass
    public static void initDB() throws Exception
    {
        EmbeddedDatabaseBuilder dsBuilder = new EmbeddedDatabaseBuilder();
        dsBuilder.setType(EmbeddedDatabaseType.HSQL);
        dsBuilder.addScript("classpath:hsqldb/annotations.sql");
        ds = dsBuilder.build();
    }

    @Before
    public void setUp() throws Exception
    {
        DocumentId id = DocumentIdParser.fromString(UUID.randomUUID().toString());
        content = new JDBCAnnotationContent(id);
        content.setDataSource(ds);
    }

    @Test
    public void testGetEmptyAnnotationContent() throws Exception
    {
        assertNull(content.get());
    }

    @Test
    public void testGetAfterUpdate() throws Exception
    {
        byte[] bytes = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        content.update(new ByteArrayInputStream(bytes));
        assertEquals(bytes.length, IOUtils.toByteArray(content.get()).length);
    }

    @Test
    public void testGetAfterTwoUpdate() throws Exception
    {
        byte[] bytes = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        content.update(new ByteArrayInputStream(bytes));
        assertEquals(bytes.length, IOUtils.toByteArray(content.get()).length);
        byte[] bytes2 = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9 };
        content.update(new ByteArrayInputStream(bytes2));
        assertEquals(bytes2.length, IOUtils.toByteArray(content.get()).length);
    }

    @Test
    public void testEncoding() throws Exception
    {
        byte[] bytes = "é€_è*ù.%/".getBytes();
        content.update(new ByteArrayInputStream(bytes));
        assertEquals(bytes.length, IOUtils.toByteArray(content.get()).length);
        assertEquals("é€_è*ù.%/", new String(IOUtils.toByteArray(content.get())));
    }
}
