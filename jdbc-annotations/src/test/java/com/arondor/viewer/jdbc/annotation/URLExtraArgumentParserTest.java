package com.arondor.viewer.jdbc.annotation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.arondor.viewer.client.api.document.DocumentId;
import com.arondor.viewer.client.api.document.id.DocumentIdParameter;
import com.arondor.viewer.common.document.id.DocumentIdFactory;
import com.arondor.viewer.common.document.id.URLDocumentIdParameter;

public class URLExtraArgumentParserTest
{
    private URLExtraArgumentParser parser;

    @Before
    public void setUp()
    {
        parser = new URLExtraArgumentParser();
    }

    @Test
    public void testParseVersionFromNonEncodedURL() throws Exception
    {
        String version = "23";
        String url = "http://myhost:myport/mydocument23#doc_id:434#version:" + version + "#user_id:Victor";
        DocumentId documentId = buildDocumentId(new URLDocumentIdParameter("url", url));
        assertEquals(version, parser.parse(documentId, "version"));
    }

    @Test
    public void testParseUserIdFromNonEncodedURL() throws Exception
    {
        String userId = "Victor";
        String url = "http://myhost:myport/mydocument23#doc_id:434#version:23#user_id:" + userId;
        DocumentId documentId = buildDocumentId(new URLDocumentIdParameter("url", url));
        assertEquals(userId, parser.parse(documentId, "user_id"));
    }

    @Test
    public void testParseVersionFromEncodedURL() throws Exception
    {
        String version = "23";
        String url = "http%3A%2F%2Fmyhost%3Amyport%2Fmydocument23%23doc_id%3A434%23version%3A" + version
                + "%23user_id%3A";
        DocumentId documentId = buildDocumentId(new URLDocumentIdParameter("url", url));
        assertEquals(version, parser.parse(documentId, "version"));
    }

    @Test
    public void testParseUserIdFromEncodedURL() throws Exception
    {
        String userId = "Victor";
        String url = "http%3A%2F%2Fmyhost%3Amyport%2Fmydocument23%23doc_id%3A434%23version%3A23%23user_id%3A" + userId;
        DocumentId documentId = buildDocumentId(new URLDocumentIdParameter("url", url));
        assertEquals(userId, parser.parse(documentId, "user_id"));
    }

    private DocumentId buildDocumentId(URLDocumentIdParameter urlDocumentIdParameter)
    {
        List<DocumentIdParameter> parameters = new ArrayList<DocumentIdParameter>();
        String docUrl = "http://myhost:myport/mydocument23#doc_id:434#version:23#user_id:Victor";
        parameters.add(new URLDocumentIdParameter("url", docUrl));
        return DocumentIdFactory.getInstance().generate(parameters);
    }
}
