package com.arondor.arender.tools.saas.connector;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by The ARender Team on 21/10/2019.
 */
public class ARenderRenditionSaaSUploadTest
{
    private static final String myApiKey = "ToChangeWithYourApiKey";

    private static final String UUID = "myUUID";

    private ARenderRenditionSaaSUpload aRenderRenditionSaaSUpload;

    @Before
    public void setUp()
    {
        aRenderRenditionSaaSUpload = new ARenderRenditionSaaSUpload();
    }

    @Test
    public void testUploadToRenditionSaaS() throws FileNotFoundException
    {
        String documentTitle = "AccessiWeb_bonnes_pratiques_pdf_accessibles_5mars2009.doc";
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/" + documentTitle);
        String mimeType = "application/msword";
        aRenderRenditionSaaSUpload.setaRenderRenditionApiKey(myApiKey);
        int statusCode = aRenderRenditionSaaSUpload.uploadFile(fileInputStream, UUID, documentTitle, mimeType);
        Assert.assertEquals(HttpStatus.SC_OK, statusCode);
    }

    @Test
    public void testGetPDFileFromRenditionSaaS() throws FileNotFoundException
    {
        testUploadToRenditionSaaS();
        byte[] pdfFileFromRenditionSaaS = aRenderRenditionSaaSUpload.getPDFFileFromRenditionSaaS(UUID);
        Assert.assertNotNull(pdfFileFromRenditionSaaS);
        Assert.assertTrue(pdfFileFromRenditionSaaS.length > 0);
        Assert.assertEquals(272102, pdfFileFromRenditionSaaS.length);
    }

}