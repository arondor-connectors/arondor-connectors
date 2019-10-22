package com.arondor.arender.tools.saas.connector;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileInputStream;

/**
 * Created by The ARender Team on 21/10/2019.
 */
public class ARenderRenditionSaaSUpload
{
    private static final Logger LOGGER = Logger.getLogger(ARenderRenditionSaaSUpload.class);

    private String arenderUrl;

    private String aRenderRenditionApiKey;

    public ARenderRenditionSaaSUpload()
    {
        //TODO: will be changed to: https://rendition.saas.arender.io
        arenderUrl = "https://rendition.saas.arender.io/test/";
        aRenderRenditionApiKey = "baseApiKey";
    }

    /**
     *
     * @param fileInputStream: the file to convert to either PDF, MP3 or MP4
     * @param documentId: the document identifier of your choice
     * @param documentTitle: the title of the document to upload
     * @param mimeType: the mimeType of the document to upload
     * @return HTTP Code of the REST Call
     */
    public int uploadFile(FileInputStream fileInputStream, String documentId, String documentTitle, String mimeType)
    {
        RestTemplate template = new RestTemplate();
        final String uriString = UriComponentsBuilder
                .fromHttpUrl(arenderUrl + "document/" + documentId + "/upload/")
                .queryParam("api-key", aRenderRenditionApiKey)
                .queryParam("documentTitle", documentTitle)
                .queryParam("mimeType", mimeType).toUriString();
        HttpEntity<InputStreamResource> documentContent = new HttpEntity<InputStreamResource>(
                new InputStreamResource(fileInputStream),
                new HttpHeaders()
                {
                    {
                        set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
                    }
                });
        final ResponseEntity<Void> voidResponseEntity = template.postForEntity(uriString, documentContent, Void.class);
        return voidResponseEntity.getStatusCodeValue();
    }

    /**
     *
     * @param documentId: the UUID matching an already uploaded document (should have been uploaded less than one hour ago
     * @return the converted version of the upload Document (can be PDF, MP3 or MP4 depending on the orginal file
     */
    public byte[] getPDFFileFromRenditionSaaS(String documentId)
    {
        RestTemplate template = new RestTemplate();
        final String uriString = UriComponentsBuilder
                .fromHttpUrl(arenderUrl + "accessor/getContent/raw/" + documentId + "/RENDERED"
                        + "?api-key=" + aRenderRenditionApiKey).toUriString();
        final ResponseEntity<byte[]> responseEntity = template.getForEntity(uriString, byte[].class);
        if(HttpStatus.SC_OK == responseEntity.getStatusCodeValue())
        {
            return responseEntity.getBody();
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

    public String getaRenderRenditionApiKey()
    {
        return aRenderRenditionApiKey;
    }

    public void setaRenderRenditionApiKey(String aRenderRenditionApiKey)
    {
        this.aRenderRenditionApiKey = aRenderRenditionApiKey;
    }
}
