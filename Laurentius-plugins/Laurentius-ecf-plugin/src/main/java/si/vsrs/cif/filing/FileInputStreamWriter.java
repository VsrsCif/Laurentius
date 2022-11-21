package si.vsrs.cif.filing;

import org.jboss.resteasy.core.ResteasyContext;
import org.jboss.resteasy.plugins.providers.FileProvider;
import org.jboss.resteasy.plugins.providers.FileRangeException;
import org.jboss.resteasy.plugins.providers.ProviderHelper;
import org.jboss.resteasy.plugins.server.Cleanable;
import org.jboss.resteasy.plugins.server.Cleanables;
import org.jboss.resteasy.resteasy_jaxrs.i18n.LogMessages;
import org.jboss.resteasy.spi.AsyncMessageBodyWriter;
import org.jboss.resteasy.spi.AsyncOutputStream;
import org.jboss.resteasy.util.MediaTypeHelper;
import org.jboss.resteasy.util.NoContent;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.CompletionStage;

public class FileInputStreamWriter implements
        AsyncMessageBodyWriter<FileInputStream>
{
    private static final String PREFIX = "pfx";

    private static final String SUFFIX = "sfx";

    private String _downloadDirectory = null; // by default temp dir, but
    // consider allowing it to be
    // defined at runtime


    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType)
    {
        return File.class.isAssignableFrom(type) && !MediaTypeHelper.isBlacklisted(mediaType); // catch subtypes
    }

    public long getSize(FileInputStream o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return 0;
    }

    public void writeTo(FileInputStream uploadFile, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException
    {
        LogMessages.LOGGER.debugf("Provider : %s,  Method : readFrom", getClass().getName());
        HttpHeaders headers = ResteasyContext.getContextData(HttpHeaders.class);
        if (headers == null)
        {
            writeIt(uploadFile, entityStream);
            return;
        }
        String range = headers.getRequestHeaders().getFirst("Range");
        if (range == null)
        {
            writeIt(uploadFile, entityStream);
            return;
        }
        range = range.trim();
        int byteUnit = range.indexOf("bytes=");
        if ( byteUnit < 0)
        {
            //must start with 'bytes'
            writeIt(uploadFile, entityStream);
            return;
        }
        range = range.substring("bytes=".length());
        if (range.indexOf(',') > -1)
        {
            // we don't support this
            writeIt(uploadFile, entityStream);
            return;
        }
        int separator = range.indexOf('-');
        if (separator < 0)
        {
            writeIt(uploadFile, entityStream);
            return;
        }

    }

    protected void writeIt(FileInputStream uploadFile, OutputStream entityStream) throws IOException
    {
        InputStream inputStream = new BufferedInputStream(uploadFile);

        try
        {
            ProviderHelper.writeTo(inputStream, entityStream);
        }
        finally
        {
            inputStream.close();
        }
    }

    public CompletionStage<Void> asyncWriteTo(FileInputStream uploadFile, Class<?> type, Type genericType,
                                              Annotation[] annotations, MediaType mediaType,
                                              MultivaluedMap<String, Object> httpHeaders,
                                              AsyncOutputStream entityStream)
    {
        LogMessages.LOGGER.debugf("Provider : %s,  Method : readFrom", getClass().getName());
        HttpHeaders headers = ResteasyContext.getContextData(HttpHeaders.class);
        if (headers == null)
        {
            return writeIt(uploadFile, entityStream);
        }
        String range = headers.getRequestHeaders().getFirst("Range");
        if (range == null)
        {
            return writeIt(uploadFile, entityStream);
        }
        range = range.trim();
        int byteUnit = range.indexOf("bytes=");
        if ( byteUnit < 0)
        {
            //must start with 'bytes'
            return writeIt(uploadFile, entityStream);
        }
        range = range.substring("bytes=".length());
        if (range.indexOf(',') > -1)
        {
            // we don't support this
            return writeIt(uploadFile, entityStream);
        }
        int separator = range.indexOf('-');

            return writeIt(uploadFile, entityStream);

    }

    protected CompletionStage<Void> writeIt(FileInputStream uploadFile, AsyncOutputStream entityStream)
    {
        InputStream inputStream = new BufferedInputStream(uploadFile);
        return ProviderHelper.writeToAndCloseInput(inputStream, entityStream);


    }

}