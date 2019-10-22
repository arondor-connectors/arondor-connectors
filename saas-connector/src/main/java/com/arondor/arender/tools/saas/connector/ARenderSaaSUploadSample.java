package com.arondor.arender.tools.saas.connector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 * Created by The ARender Team on 18/10/2019.
 */
public class ARenderSaaSUploadSample
{
    public static final String API_KEY_PARAM_VALUE = "api-key";

    public static final String UUID_PARAM_VALUE = "uuid";

    private String arenderUrl;

    private String aRenderHMIApiKey;

    public ARenderSaaSUploadSample()
    {
        arenderUrl = "https://saas.arender.io/";
        aRenderHMIApiKey = "baseApiKey";
    }

    public String getARenderUUID(InputStream stream, String refId, String documentTitle) throws IOException
    {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(
                (new StringBuilder()).append(arenderUrl).append("arendergwt/uploadServlet?" + API_KEY_PARAM_VALUE + "=").append(
                        aRenderHMIApiKey)
                        .append("&" + UUID_PARAM_VALUE + "=").append(URLEncoder.encode(refId, "UTF-8")).toString());
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", stream, ContentType.APPLICATION_OCTET_STREAM, documentTitle);
        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        return (new String(ARenderSaaSUtils.read(response.getEntity().getContent()))).replace("|", "").trim();
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
        HttpGet get = new HttpGet((new StringBuilder()).append(arenderUrl).append(servletName).append(
                "?" + API_KEY_PARAM_VALUE + "=")
                .append(aRenderHMIApiKey).append("&" + UUID_PARAM_VALUE + "=").append(URLEncoder.encode(uuid, "UTF-8")).toString());
        CloseableHttpResponse response = httpClient.execute(get);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK)
        {
            // the document already exists
            return (new String(ARenderSaaSUtils.read(response.getEntity().getContent()))).replace("|", "").trim();
        }
        else
        {
            return null;
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

    public String getaRenderHMIApiKey()
    {
        return aRenderHMIApiKey;
    }

    public void setaRenderHMIApiKey(String aRenderHMIApiKey)
    {
        this.aRenderHMIApiKey = aRenderHMIApiKey;
    }
}
