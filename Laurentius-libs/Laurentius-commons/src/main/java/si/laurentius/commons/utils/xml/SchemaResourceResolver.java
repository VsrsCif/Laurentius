/*
 * Copyright 2015, Supreme Court Republic of Slovenia
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
package si.laurentius.commons.utils.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class SchemaResourceResolver implements LSResourceResolver {

  private static final String XML_NAMESPACE = "http://www.w3.org/TR/REC-xml";
  private static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

  private URI getTargetURI(String baseURI, String relativePath) {
    URI targetURI = null;

    try {
      targetURI = (new URI(baseURI)).resolve(relativePath);
    } catch (URISyntaxException ex) {
      throw new RuntimeException("Could not resolve target URI  (baseURI:'" + baseURI +
           "' + path: '" + relativePath + "' )- " + ex.getMessage());
    }

    return targetURI;
  }

  private final String contextFolder;

  /**
   *
   * @param contextFolder
   */
  public SchemaResourceResolver(String contextFolder) {
    this.contextFolder = contextFolder;
  }

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId,
      String systemId, String baseURI) {
    if (!XSD_NAMESPACE.equals(type) && !XML_NAMESPACE.equals(type)) {
      throw new IllegalArgumentException("Unexpected resource type [" + type + "], expected is [" +
           XSD_NAMESPACE + " or " + XML_NAMESPACE + " ].");
    }
    if (systemId == null) {
      throw new IllegalArgumentException("Unexpected resource system-id [" + systemId + "].");
    }
    URI targetURI = getTargetURI(contextFolder, systemId);
    String targetFullName = targetURI.getPath();
    LSInput input = null;
    try {
      input =
          new SchemaInput(baseURI, publicId, systemId, getClass().getResourceAsStream(
              targetFullName));
    } catch (Exception ex) {
      throw new RuntimeException("Could not open resource stream -" + targetFullName + ", Error: " +
           ex.getMessage());
    }

    return input;
  }

}

class SchemaInput implements LSInput {

  final private String baseURI;

  private final BufferedInputStream inputStream;
  Logger mlog = getLogger(SchemaInput.class);
  private String publicId;
  private String systemId;

  public SchemaInput(String baseURI, String publicId, String sysId, InputStream input) {
    this.baseURI = baseURI;
    this.publicId = publicId;
    this.systemId = sysId;
    this.inputStream = new BufferedInputStream(input);
  }

  @Override
  public String getBaseURI() {
    return baseURI;
  }

  @Override
  public InputStream getByteStream() {
    return null;
  }

  @Override
  public boolean getCertifiedText() {
    return false;
  }

  @Override
  public Reader getCharacterStream() {
    return null;
  }

  @Override
  public String getEncoding() {
    return null;
  }

  public BufferedInputStream getInputStream() {
    return inputStream;
  }

  @Override
  public String getPublicId() {
    return publicId;
  }

  @Override
  public String getStringData() {
    StringWriter sw = new StringWriter();
    synchronized (inputStream) {
      try {        
        InputStreamReader in = new InputStreamReader(inputStream);
        char[] bf = new char[1024];        
        int n = 0;
        while (-1 != (n = in.read(bf))) {
          sw.write(bf, 0, n);
        }
      } catch (IOException e) {
        mlog.error("Error reading resource: '" + baseURI + "'. Error: " + e.getMessage(), e);
        return null;
      }
    }
    return sw.toString();
  }

  @Override
  public String getSystemId() {
    return systemId;
  }

  @Override
  public void setBaseURI(String baseURI) {
  }

  @Override
  public void setByteStream(InputStream byteStream) {
  }

  @Override
  public void setCertifiedText(boolean certifiedText) {
  }

  @Override
  public void setCharacterStream(Reader characterStream) {
  }

  @Override
  public void setEncoding(String encoding) {
  }

  @Override
  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  @Override
  public void setStringData(String stringData) {
  }

  @Override
  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

}
