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
package si.laurentius.lce.sign.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import static java.io.File.createTempFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import static java.lang.String.format;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import static org.w3c.dom.Node.ELEMENT_NODE;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.utils.SEDLogger;
import static java.lang.System.getProperty;
import javax.xml.crypto.dsig.DigestMethod;

/**
 *
 * @author Jože Rihtaršič
 */
public class SiTsaTimeStamp implements XMLTimeStamp {

  private static final String HTTPHEADER_CONTENT_TYPE = "Content-Type";
  private static final String HTTPHEADER_CONTENT_TYPE_VALUE = "text/xml;charset=UTF-8";
  private static final String HTTPHEADER_SAOPACTION = "SOAPAction";

  private static final String TIMESTAMP_REQUEST =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
      " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
      " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
      "  <SOAP-ENV:Body>\n" +
      "    <tsa:service xmlns:tsa=\"urn:Entrust-TSA\">\n" +
      "      <ts:TimeStampRequest xmlns:ts=\"http://www.entrust.com/schemas/timestamp-protocol-20020207\">\n" +
      "        <ts:Digest>\n" +
      "          <ds:DigestMethod xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" " +
      " Algorithm=\"%s\"/>\n" +
      "          <ds:DigestValue xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">%s</ds:DigestValue>\n" +
      "        </ts:Digest>\n" +
      "        <ts:Nonce>%s</ts:Nonce>\n" +
      "      </ts:TimeStampRequest>\n" +
      "    </tsa:service>\n" +
      "  </SOAP-ENV:Body>\n" +
      "</SOAP-ENV:Envelope>";

  private static final String TSA_ALGORITHM = DigestMethod.SHA1;
                    

  private static final String XADES_XMLTimeStamp = "XMLTimeStamp";

  public static final SEDLogger LOG = new SEDLogger(SiTsaTimeStamp.class);

  // String mstrTimeStampServerUrl = "http://ts.si-tsa.sigov.si:80/verificationserver/timestamp";
  String mstrResultLogFolder = getProperty("java.io.tmpdir");
  String mstrTimeStampServerUrl = null;

  protected Document callTimestampService(String ireq, String wsldLocatin,
      String soapActionNamespace, String soapAction)
      throws SEDSecurityException {
    long t = LOG.logStart(ireq, wsldLocatin);

    long tCall;
    long tReceive;
    Document respDoc = null;
    HttpURLConnection conn = null;
    try {
      String strLocation = wsldLocatin;
      int iVal = strLocation.indexOf('?');
      if (iVal > 0) {
        strLocation = strLocation.substring(0, iVal);
      }
      URL url = new URL(strLocation);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setDoInput(true);
      if (soapAction == null) {
        conn.setRequestProperty(HTTPHEADER_SAOPACTION,
            soapAction == null ? "" : (soapActionNamespace + soapAction));
      }
      conn.setRequestProperty(HTTPHEADER_CONTENT_TYPE, HTTPHEADER_CONTENT_TYPE_VALUE);
      OutputStream os = conn.getOutputStream();
      // write post ----------------------------------------
      os.write(ireq.getBytes("UTF-8"));
      os.flush();
      tCall = LOG.getTime() - t;
      LOG.log("SvevSignatureUtils.callTimestampService: send request in " + tCall + "ms");
      // start receiving ----------------------------------------
      tReceive = LOG.getTime() - tCall;
      LOG.log("SvevSignatureUtils.callTimestampService: receive response in (" + tReceive + "ms)");
      InputStream httpIS = conn.getInputStream();
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      respDoc = dbf.newDocumentBuilder().parse(httpIS);
    } catch (SAXException | ParserConfigurationException ex) {
      LOG.logError( t,  ex.getMessage(), ex);
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CreateTimestampException,
          ex, ex.getMessage());
    } catch (IOException ex) {
      File fout;
      if (conn != null && conn.getErrorStream() != null) {
        fout = writeToFile(conn.getErrorStream(), getResultLogFolder(), "TS_ERROR", ".html");
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("SvevSignatureUtils.callTimestampService: ERROR\n");
        sb.append("\trequest:");
        sb.append(ireq);
        sb.append("\n\twsldLocatin:");
        sb.append(wsldLocatin);
        sb.append("\n\tsoapActionNamespace:");
        sb.append(soapActionNamespace);
        sb.append("\n\tsoapAction:");
        sb.append(soapAction);
        sb.append("\n\tmsg");
        sb.append(ex.getMessage());
        sb.append("SvevSignatureUtils.callTimestampService: ERROR\n");
        for (StackTraceElement st : ex.getStackTrace()) {
          sb.append("\n\t\t");
          sb.append(st.toString());
        }
        fout =
            writeToFile(new ByteArrayInputStream(sb.toString().getBytes()), getResultLogFolder(),
                "TS_ERROR", ".html");
      }
      StringWriter sw = new StringWriter();
      sw.append("SvevSoap.callService: Exception: SoapAction:'");
      sw.append(soapActionNamespace);
      sw.append(soapAction);
      sw.append("' location:'" + wsldLocatin + "' Exception message:");
      sw.append("\nResponse writen to: '");
      sw.append(fout.getAbsolutePath());
      sw.append("' ");
      sw.append("error message:");
      sw.append(ex.getMessage());
      sw.append("SvevSoap.callService: Header values'");
      if (conn != null) {
        Map<String, List<String>> mp = conn.getHeaderFields();
        mp.keySet().stream().forEach((String s) -> {
          sw.append(s + " : " + mp.get(s));
        });
      }
      LOG.logError(t, sw.toString(), ex);
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException,
          ex, ex.getMessage());
    } finally {
      if (conn != null) {
        try {
          conn.getInputStream().close();
        } catch (IOException ex) {
          // mlgLogger.error("SvevSoap.callService: Error closing socket: " + ex.getMessage());
        }
      }
    }
    LOG.logEnd(t);
    return respDoc;
  }

  /**
   *
   * @param hash
   * @return
   * @throws SEDSecurityException
   */
  @Override
  public Element getTimeStamp(String hash)
      throws SEDSecurityException {
    String reg = format(TIMESTAMP_REQUEST,TSA_ALGORITHM,  hash, Calendar.getInstance().getTimeInMillis());
    Document d = callTimestampService(reg, getTimeStampServerUrl(), null, null);
    setIdnessToElemetns(d.getDocumentElement());
    Element e = d.getElementById("TimeStampToken");
    if (e == null) {
      e = (Element) d.getElementsByTagName("dsig:Signature").item(0);
    }
    return e;
  }

  /**
   *
   * @return
   */
  @Override
  public String getTimeStampServerUrl() {
    return mstrTimeStampServerUrl;
  }

  /**
   * sets attribute ID as type ID.
   *
   * @param n XML Node
   */
  private void setIdnessToElemetns(Node n) {
    if (n.getNodeType() == ELEMENT_NODE) {
      Element e = (Element) n;
      if (e.hasAttribute("Id")) {
        e.setIdAttribute("Id", true);
      } else if (e.hasAttribute("id")) {
        e.setIdAttribute("id", true);
      }
      if (e.hasAttribute("ID")) {
        e.setIdAttribute("ID", true);
      }
      NodeList l = e.getChildNodes();
      for (int i = 0; i < l.getLength(); i++) {
        setIdnessToElemetns(l.item(i));
      }
    }
    
    
  }
    /**
   *
   * @return
   */
  public String getResultLogFolder() {
    return mstrResultLogFolder;
  }
  
  /**
   *
   * @param in
   * @param logFolder
   * @param fileNamePrefix
   * @param fileNameSuffix
   * @return
   */
  public File writeToFile(InputStream in, String logFolder, String fileNamePrefix,
      String fileNameSuffix) {
    FileOutputStream out = null;
    File f = null;
    try {

      f = createTempFile(fileNamePrefix, fileNameSuffix, new File(logFolder));
      out = new FileOutputStream(f);
      byte[] buffer = new byte[1024];
      int len = in.read(buffer);
      while (len != -1) {
        out.write(buffer, 0, len);
        len = in.read(buffer);
      }
    } catch (IOException ex) {
      String strMessage =
          "Error write to: '" + (f != null ? f.getAbsolutePath() : "null-file") + "' exception:" +
           ex.getMessage();
      //LOG.error(strMessage);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException ex) {
        //  LOG.error("Error closing file; '" + (f != null ? f.getAbsolutePath() : "null-file") +
        //       "' exception:" + ex.getMessage());
        }
      }
    }
    return f;
  }

}
