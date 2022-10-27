/*
 * Copyright 2017, Supreme Court Republic of Slovenia
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.jrc.msh.utils;

import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.GZIPUtil;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.msh.outbox.payload.MSHOutPart;

import javax.activation.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

/**
 * @author Joze Rihtarsic
 */
public class MSHOutPartDataSource implements DataSource {

    /**
     * Logger for EBMSOutInterceptor class
     */
    protected final static SEDLogger LOG = new SEDLogger(
            MSHOutPartDataSource.class);

    /**
     * GZIP utils
     */
    protected final GZIPUtil mGZIPUtils = new GZIPUtil();

    MSHOutPart mop;
    File fullPath = null;
    boolean mbGZip = false;
    boolean mb64 = false;

    /**
     * @param op     - Message out part
     * @param gzip   - GZIP mime
     * @param base64 - if SMTP protocol supports only ascii characters
     */
    public MSHOutPartDataSource(MSHOutPart op, boolean gzip, boolean base64) throws StorageException {

        this.mop = op;
        this.mb64 = base64;
        this.mbGZip = gzip;
        try {
            init();
        } catch (IOException io) {
            String msg = String.format(
                    "Error preparing file %s: gzip compress: %s, base46: %s ", mop.
                            getFilepath(),
                    mbGZip ? "true" : "false",
                    mb64 ? "true" : "false");
            throw new StorageException(msg, io);
        }

    }

    private void init() throws IOException, StorageException {
        File sourcePath = StorageUtils.getFile(mop.getFilepath());
        // allays create a copy to temp file because file is deleted when sent
        fullPath = File.createTempFile("sent-", ".bin");
        StorageUtils.copyFile(sourcePath, fullPath, true);

        if (mbGZip) {
            fullPath = compressFileAndDelete(fullPath);
        }

        if (mb64) {
            fullPath = base64EncodeFileAndDelete(fullPath);
        }

    }

    /**
     * Method compress and delete old file
     *
     * @param fInput
     * @return
     * @throws IOException
     */
    private File compressFileAndDelete(File fInput)
            throws IOException {
        long l = LOG.logStart();
        // create temp file
        File fattCmp = File.createTempFile(fInput.getName(), ".gzip");
        mGZIPUtils.compressGZIP(fInput, fattCmp);
        fInput.delete();
        LOG.logEnd(l, "compress: " + fInput.getPath());
        return fattCmp;

    }

    private File base64EncodeFileAndDelete(File fInput)
            throws IOException {
        long l = LOG.logStart();
        // create temp file

        File fattB64 = File.createTempFile(fInput.getName(), ".b64");
        try (OutputStream os = Base64.getEncoder().wrap(
                new FileOutputStream(fattB64));
             FileInputStream fis = new FileInputStream(fInput)) {
            byte[] buf = new byte[1024];
            int read = 0;
            while ((read = fis.read(buf)) != -1) {
                os.write(buf, 0, read);
            }
            os.flush();
        }
        fInput.delete();
        LOG.logEnd(l, "base64 encode: " + fInput.getPath());
        return fattB64;
    }

    @Override
    public String getContentType() {
        return mbGZip ? MimeValue.MIME_GZIP.getMimeType() : mop.getMimeType();
    }

    @Override
    public InputStream getInputStream()
            throws IOException {
        return Files.newInputStream(fullPath.toPath(), StandardOpenOption.DELETE_ON_CLOSE);
    }

    @Override
    public String getName() {
        return mop.getName();
    }

    @Override
    public OutputStream getOutputStream()
            throws IOException {
        // class is read only wrapper
        return null;
    }
}
