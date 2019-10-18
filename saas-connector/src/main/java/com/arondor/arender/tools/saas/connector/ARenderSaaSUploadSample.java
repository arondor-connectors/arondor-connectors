package com.arondor.arender.tools.saas.connector;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 * Created by The ARender Team on 18/10/2019.
 */
public class ARenderSaaSUploadSample
{
    private static final String MODEL_PARAM_UUID = "UUID";

    private static final String DEFAULT_ARENDER_ID = "b64_I2RlZmF1bHQ=";

    private String arenderUrl;

    private String apiKey;

    public ARenderSaaSUploadSample()
    {
        arenderUrl = "https://saas.arender.io/";
        apiKey = "baseApiKey";
    }

    public String getARenderUUID(InputStream stream, String refId, String documentTitle) throws IOException
    {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(
                (new StringBuilder()).append(arenderUrl).append("arendergwt/uploadServlet?api-key=").append(apiKey)
                        .append("&uuid=").append(URLEncoder.encode(refId, "UTF-8")).toString());
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", stream, ContentType.APPLICATION_OCTET_STREAM, documentTitle);
        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        return (new String(read(response.getEntity().getContent()))).replace("|", "").trim();
    }

    /**
     * Check in ARender SaaS if the document with your has been already uploaded
     * @param refId : the id of your document
     * @return null if the document does not exist, the UUID of the document otherwise
     * @throws IOException
     */
    public String checkIfUUIDExists(String refId) throws IOException
    {
        return getCall(refId, "arendergwt/uploadServlet");
    }

    /**
     * Get the Structure of the document (number of page, dimension of each page
     * @param uuid the fetch UUID of your document
     * @return A JSON representing the structure of the document
     * @throws IOException
     */
    public String getDocumentLayout(String uuid) throws IOException
    {
        return getCall(uuid, "/arendergwt/documentLayout");
    }

    private String getCall(String uuid, String servletName) throws IOException
    {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet((new StringBuilder()).append(arenderUrl).append(servletName).append("?api-key=")
                .append(apiKey).append("&uuid=").append(URLEncoder.encode(uuid, "UTF-8")).toString());
        CloseableHttpResponse response = httpClient.execute(get);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK)
        {
            // the document already exists
            return (new String(read(response.getEntity().getContent()))).replace("|", "").trim();
        }
        else
        {
            return null;
        }
    }

    private byte[] read(InputStream is) throws IOException
    {
        try
        {
            return IOUtils.toByteArray(is);
        }
        finally
        {
            closeQuietly(is);
        }
    }

    protected void closeQuietly(Closeable closeable)
    {
        try
        {
            if (closeable != null)
            {
                closeable.close();
            }
        }
        catch (IOException ioexception)
        {
        }
    }

    public String getArenderUrl()
    {
        return arenderUrl;
    }

    public void setArenderUrl(String arenderUrl)
    {
        this.arenderUrl = arenderUrl;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }
}
