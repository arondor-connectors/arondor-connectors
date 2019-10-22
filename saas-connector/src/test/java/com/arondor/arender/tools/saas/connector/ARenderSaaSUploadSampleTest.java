package com.arondor.arender.tools.saas.connector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by The ARender Team on 18/10/2019.
 */
public class ARenderSaaSUploadSampleTest
{

    private static final String myApiKey = "ToChangeWithYourApiKey";

    private ARenderSaaSUploadSample aRenderSaaSUploadSample;

    @Before
    public void setUp()
    {
        aRenderSaaSUploadSample = new ARenderSaaSUploadSample();
    }

    @Test
    public void getARenderUUID() throws IOException
    {
        String documentTitle = "AccessiWeb_bonnes_pratiques_pdf_accessibles_5mars2009.doc";
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/" + documentTitle);
        String id = "IDOfYourDocument";
        aRenderSaaSUploadSample.setaRenderHMIApiKey(myApiKey);
        String aRenderUUID = aRenderSaaSUploadSample.getARenderUUID(fileInputStream, id, documentTitle);
        String checkedUUIDExisted = aRenderSaaSUploadSample.checkIfUUIDExists(id);
        Assert.assertNotNull(checkedUUIDExisted);
        String documentLayout = aRenderSaaSUploadSample.getDocumentLayout(aRenderUUID);
        Assert.assertNotNull(documentLayout);
    }
}