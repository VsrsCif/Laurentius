/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.ws;

import java.io.StringWriter;
import java.util.Calendar;
import javax.annotation.Resource;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.apache.log4j.Logger;

/**
 *
 * @author sluzba
 */
public class JEELogInterceptor {

  private final String logFormat = "%s %s %s %s time: %d ms.";

  private final String logFormatBegin = "%s %s %s %s";
  private final Logger mlgLogger = Logger.getLogger(JEELogInterceptor.class);
  @Resource
  WebServiceContext mwsCtxt;

  /**
   *
   * @return
   */
  protected String getCurrrentRemoteIP() {
    String clientIP = "";
    if (mwsCtxt != null) {
      try {

        MessageContext msgCtxt = mwsCtxt.getMessageContext();
        HttpServletRequest req = (HttpServletRequest) msgCtxt.get(MessageContext.SERVLET_REQUEST);
        clientIP = req.getRemoteAddr();
      } catch (Exception exc) {
        mlgLogger.error("JEELogInterceptor.getCurrrentRemoteIP  ERROR", exc);
      }
    }
    return clientIP;
  }

  /**
   *
   * @param t
   * @return
   */
  public long getDuration(long t) {
    return Calendar.getInstance().getTimeInMillis() - t;
  }

  /**
   *
   * @return
   */
  public long getTime() {
    return Calendar.getInstance().getTimeInMillis();
  }

  /**
   *
   * @param context
   * @return
   * @throws Exception
   */
  @AroundInvoke
  public Object intercept(InvocationContext context) throws Exception {
    String ip = getCurrrentRemoteIP();
    String methodName = context.getMethod().getName();

    long l = getTime();
    mlgLogger.debug(String.format(logFormatBegin, methodName, ip, "BEGIN", ""));

    Object result = null;
    try {
      result = context.proceed();
    } catch (Exception e) {
      mlgLogger.error(
          String.format(logFormat, methodName, ip, "ERROR", e.getMessage(), getDuration(l)), e);
      mlgLogger.error("Parameters: " + paramsToString(context.getParameters()));
      throw e;
    }
    mlgLogger.info(String.format(logFormat, methodName, ip, "END", "", getDuration(l)));
    return result;
  }

  /**
   *
   * @param method
   * @param pip
   * @param lTime
   * @param strMessage
   * @param ex
   */
  public void logError(String method, String pip, long lTime, String strMessage, Exception ex) {
    mlgLogger.error(method + ": - ERROR MSG: '" + strMessage + "' ( " + (getTime() - lTime)
        + " ms )", ex);
  }

  /**
   *
   * @param method
   * @param lTime
   * @param ex
   */
  public void logError(String method, long lTime, Exception ex) {
    mlgLogger.error(method + ": - ERROR MSG: '" + (ex != null ? ex.getMessage() : "") + "' ( "
        + (getTime() - lTime) + " ms )", ex);
  }

  /**
   *
   * @param method
   * @param lTime
   * @param strMessage
   * @param ex
   */
  public void logWarn(String method, long lTime, String strMessage, Exception ex) {
    mlgLogger.warn(
        method + ": - Warn MSG: '" + strMessage + "' ( " + (getTime() - lTime) + " ms )", ex);
  }

  /**
   *
   * @param param
   * @return
   */
  public String paramsToString(final Object... param) {
    String strParams = null;
    if (param != null && param.length != 0) {
      StringWriter sw = new StringWriter();
      int i = 0;
      for (Object o : param) {
        if (i != 0) {
          sw.append(",");
        }
        sw.append(requestToString(o));
      }
      strParams = sw.toString();
    }
    return strParams;
  }

  /**
   *
   * @param obj
   * @return
   */
  public String requestToString(Object obj) {
    if (obj == null) {
      return "";
    }
    String strRes;
    try {

      StringWriter sw = new StringWriter();
      JAXBContext jc = JAXBContext.newInstance(obj.getClass());

      Marshaller m = jc.createMarshaller();
      m.marshal(obj, sw);
      strRes = sw.toString();
    } catch (JAXBException ex) {
      mlgLogger.warn("Error marshal object: '" + obj + "'. Error:  " + ex.toString() + ", "
          + ex.getMessage());
      strRes = obj.toString();
    }
    return strRes;
  }
}
