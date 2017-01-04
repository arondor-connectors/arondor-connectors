package com.arondor.viewer.jdbc.annotation;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.junit.Test;

import com.arondor.viewer.client.api.document.DocumentId;
import com.arondor.viewer.client.api.document.id.DocumentIdParameter;
import com.arondor.viewer.common.document.id.DocumentIdFactory;
import com.arondor.viewer.common.document.id.URLDocumentIdParameter;
import com.google.common.collect.Lists;

public class VersionedDocumentAnnotationAccessorJDBCTest
{
    @Test
    public void testParseDocumentIdURLParameter() throws UnsupportedEncodingException
    {
        // http://unfccc.int/resource/docs/convkp/kpeng.pdf

        String version = "1";
        String versionParameter = "version";
        String url = "http://unfccc.int/resource/docs/convkp/kpeng.pdf?" + versionParameter + "=" + version;
        String encodedURL = "http%3A%2F%2Funfccc.int%2Fresource%2Fdocs%2Fconvkp%2Fkpeng.pdf%3Fversion%3D" + version;
        String decodedURL = parseURLParameter(encodedURL, version);

        assertEquals(url, decodedURL);

        String versionNumber = decodedURL.split(versionParameter + "=")[1];
        assertEquals(version, versionNumber);
    }

    private String parseURLParameter(String urlValue, String version) throws UnsupportedEncodingException
    {
        DocumentIdParameter param = new URLDocumentIdParameter("url", Lists.newArrayList(urlValue));
        List<DocumentIdParameter> parameters = Lists.newArrayList(param);
        DocumentId documentId = DocumentIdFactory.getInstance().generate(parameters);

        List<DocumentIdParameter> parsed = DocumentIdFactory.getInstance().revert(documentId);
        assertEquals(1, parsed.size());
        assertEquals("url", parsed.get(0).getKey());
        assertEquals(urlValue, parsed.get(0).getValue());
        String decodedURL = URLDecoder.decode(parsed.get(0).getValue(), "UTF-8");
        return decodedURL;
    }
}