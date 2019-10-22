package com.arondor.arender.tools.saas.connector;

import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The ARender Team on 21/10/2019.
 */
public class ARenderSaaSUtils
{
    public static byte[] read(InputStream is) throws IOException
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

    private static void closeQuietly(Closeable closeable)
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
}
