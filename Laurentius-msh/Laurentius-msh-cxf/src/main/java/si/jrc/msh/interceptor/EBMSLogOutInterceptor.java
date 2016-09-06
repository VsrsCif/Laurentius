package si.jrc.msh.interceptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;

import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.jrc.msh.utils.EBMSLogUtils;
import si.laurentius.commons.cxf.EBMSConstants;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author Jože Rihtaršič
 */
public class EBMSLogOutInterceptor extends AbstractSoapInterceptor {

  private static final SEDLogger LOG = new SEDLogger(EBMSLogOutInterceptor.class);

  /**
   *
   * @param phase
   */
  public EBMSLogOutInterceptor(String phase) {
    super(phase);
    addBefore(StaxOutInterceptor.class.getName());
  }

  /**
   *
   */
  public EBMSLogOutInterceptor() {
    this(Phase.PRE_STREAM);
  }

  /**
   *
   * @param message
   */
  @Override
  public void handleMessage(SoapMessage message)
      throws Fault {
    long l = LOG.logStart();

    final OutputStream os = message.getContent(OutputStream.class);
    boolean isRequestor = MessageUtils.isRequestor(message);
    if (os == null) {
      LOG.logWarn("Could not log message because it not contains OutputStream!", null);
      return;
    }
    // create store file
    File fStore;
    if (isRequestor) {
      MSHOutMail rq = SoapUtils.getMSHOutMail(message);
      fStore = EBMSLogUtils.getOutboundFileName(true, rq.getId(), null);
    } else {
      // get base from input log file
      String base =
          (String) message.getExchange().get(EBMSConstants.EBMS_CP_BASE_LOG_SOAP_MESSAGE_FILE);
      fStore = EBMSLogUtils.getOutboundFileName(false, null, base);
    }

    LOG.log("Out " + (isRequestor ? "request" : "response") + " stored to:" + fStore.getName());
    message.getExchange().put(EBMSConstants.EBMS_CP_BASE_LOG_SOAP_MESSAGE_FILE,
        EBMSLogUtils.getBaseFileName(fStore));
    message.getExchange().put(EBMSConstants.EBMS_CP_OUT_LOG_SOAP_MESSAGE_FILE, fStore);

    //  create FileOutputStream to log request 
    FileOutputStream fos;
    try {
      fos = new FileOutputStream(fStore);
    } catch (FileNotFoundException ex) {
      String errmsg =
          "Could not log outbound message to file: '" + fStore.getAbsolutePath() + "'! ";
      LOG.logError(l, errmsg, ex);
      return;
    }

    // create  CacheAndWriteOutputStream
    final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);

    message.setContent(OutputStream.class, newOut);
    newOut.registerCallback(new LoggingCallback(fos, message, os, fStore));

    LOG.logEnd(l);
  }

  private LoggingMessage setupBuffer(Message message, File fout) {

    final LoggingMessage buffer =
        new LoggingMessage(fout,  "OUT MESSAGE");

    Integer responseCode = (Integer) message.get(Message.RESPONSE_CODE);
    if (responseCode != null) {
      buffer.getResponseCode().append(responseCode);
    }

    String encoding = (String) message.get(Message.ENCODING);
    if (encoding != null) {
      buffer.getEncoding().append(encoding);
    }
    String httpMethod = (String) message.get(Message.HTTP_REQUEST_METHOD);
    if (httpMethod != null) {
      buffer.getHttpMethod().append(httpMethod);
    }
    String address = (String) message.get(Message.ENDPOINT_ADDRESS);
    if (address != null) {
      buffer.getAddress().append(address);
      String uri = (String) message.get(Message.REQUEST_URI);
      if (uri != null && !address.startsWith(uri)) {
        if (!address.endsWith("/") && !uri.startsWith("/")) {
          buffer.getAddress().append("/");
        }
        buffer.getAddress().append(uri);
      }
    }
    String ct = (String) message.get(Message.CONTENT_TYPE);
    if (ct != null) {
      buffer.getContentType().append(ct);
    }
    Object headers = message.get(Message.PROTOCOL_HEADERS);
    if (headers != null) {
      buffer.getHeader().append(headers);
    }
    return buffer;
  }

  class LoggingCallback implements CachedOutputStreamCallback {

    private final Message message;
    private final OutputStream origStream;
    private final OutputStream logOutputStream;
    private final File fout;

    public LoggingCallback(final OutputStream los, final Message msg, final OutputStream os,
        File fout) {
      this.logOutputStream = los;
      this.message = msg;
      this.origStream = os;
      this.fout = fout;
    }

    @Override
    public void onClose(CachedOutputStream cos) {
      long l = LOG.logStart();
      byte[] buf = new byte[1024];
      int len;
      InputStream is;
      try {
        is = cos.getInputStream();
        while ((len = is.read(buf)) > 0) {
          logOutputStream.write(buf, 0, len);
        }
      } catch (IOException ex) {
        LOG.logError(l, ex);
      }
      LoggingMessage buffer = setupBuffer(message, fout);
      LOG.log(buffer.toString());

      try {
        // empty out the cache
        cos.lockOutputStream();
        cos.resetOut(null, false);
      } catch (Exception ex) {
        LOG.logWarn(l, "Error clearing cache: " + ex.getMessage(), null);
      }
      message.setContent(OutputStream.class, origStream);
    }

    @Override
    public void onFlush(CachedOutputStream cos) {

    }

  }

}
