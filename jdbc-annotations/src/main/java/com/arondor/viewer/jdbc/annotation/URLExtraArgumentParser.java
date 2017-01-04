package com.arondor.viewer.jdbc.annotation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;

import com.arondor.viewer.client.api.document.DocumentId;
import com.arondor.viewer.client.api.document.id.DocumentIdParameter;
import com.arondor.viewer.common.document.id.DocumentIdFactory;

public class URLExtraArgumentParser
{
    /**
     * Parses the supplied {@link DocumentId} in order to extract a parameter.
     * 
     * This parsers uses <code>#</code> character to separated each parameter
     * and <code>:</code> to separate parameter name from its value. <br/>
     * <br/>
     * <u>Example</u>: <code>#doc_id:434#version:23#user_id:Victor</code>
     * 
     * @param documentId
     * @param paramName
     * @return
     */
    public String parse(DocumentId documentId, String paramName)
    {
        try
        {
            List<DocumentIdParameter> parsed = DocumentIdFactory.getInstance().revert(documentId);
            String decodedURL = URLDecoder.decode(parsed.get(0).getValue(), Charset.defaultCharset().name());
            String extraArgs[] = decodedURL.split("#");

            for (int arg = 1; arg < extraArgs.length; arg++)
            {
                if (!extraArgs[arg].contains(":"))
                {
                    continue;
                }
                String extraArg[] = extraArgs[arg].split(":");
                String key = extraArg[0];
                String value = extraArg[1];
                if (paramName.equals(key))
                {
                    return value;
                }
            }
            return null;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("The param " + paramName + " could not be extracted from documentId="
                    + documentId);
        }
    }
}
