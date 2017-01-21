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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import javax.activation.DataSource;
import si.laurentius.commons.utils.GZIPUtil;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.msh.outbox.payload.MSHOutPart;

/**
 *
 * @author Joze Rihtarsic
 */
public class MSHOutPartDataSource implements DataSource {

  /**
   * Logger for EBMSOutInterceptor class
   */
  protected final static SEDLogger LOG = new SEDLogger(MSHOutPartDataSource.class);

  /**
   * GZIP utils
   */
  protected final GZIPUtil mGZIPUtils = new GZIPUtil();

  MSHOutPart mop;
  File fullPath = null;
  boolean mbGZip = false;
  boolean mb64 = false;

  /**
   *
   * @param op - Message out part
   * @param gzip - GZIP mime
   * @param base64 - if SMTP protocol supports only ascii characters
   */
  public MSHOutPartDataSource(MSHOutPart op, boolean gzip, boolean base64){

    this.mop = op;
    this.mb64 = base64;
    this.mbGZip = gzip;
    fullPath = StorageUtils.getFile(mop.getFilepath());
  }

  public void init()
      throws IOException {
    fullPath = StorageUtils.getFile(mop.getFilepath());

    if (mb64) {
      fullPath = compressFile(fullPath);
    }

    if (mb64) {
      fullPath = base64EncodeFile(fullPath);
    }
  }

  private File compressFile(File fInput)
      throws IOException {
    long l = LOG.logStart();
    // create temp file
    File fattCmp = File.createTempFile(fInput.getName(), ".gzip");
    mGZIPUtils.compressGZIP(fullPath, fattCmp);
    return fattCmp;

  }

  private File base64EncodeFile(File fInput)
      throws IOException {
    long l = LOG.logStart();
    // create temp file
    
    File fattB64 = File.createTempFile(fInput.getName(), ".b64");
    try (OutputStream os = Base64.getEncoder().wrap(new FileOutputStream(fattB64));
        FileInputStream fis = new FileInputStream(fInput)) {
      byte[] buf = new byte[1024];
      int read = 0;
      while ((read = fis.read(buf)) != -1) {
        os.write(buf, 0, read);
      }
      os.flush();
    }
    return fattB64;

  }

  @Override
  public String getContentType() {
    return mop.getMimeType();
  }

  @Override
  public InputStream getInputStream()
      throws IOException {
    if (fullPath == null){
      init();
    }
    return new FileInputStream(fullPath);
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
