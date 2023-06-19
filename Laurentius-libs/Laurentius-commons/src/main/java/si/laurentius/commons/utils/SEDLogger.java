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
package si.laurentius.commons.utils;

import java.io.StringWriter;
import static java.lang.Thread.currentThread;
import static java.util.Calendar.getInstance;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Common method logger.
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class SEDLogger {

  /**
   * Method returs first cause message in throwable stack. If Throwable is null,
   * null is returned.
   *
   * @param tw throwable
   * @return
   */

  private static final String MSG = ": msg.: '";
  private static final String MSG_FC = ", first cause: '";

  public static String getFirstCauseMessage(Throwable tw) {
    if (tw == null || tw.getCause() == null) {
      return null;
    }
    Throwable twCause = tw.getCause();
    while (twCause.getCause() != null) {
      twCause = twCause.getCause();
    }
    return twCause.getMessage();

  }

  int miMethodStack = 3;
  private final Logger mlgLogger;

  /**
   *
   * @param clzz
   */
  public SEDLogger(Class clzz) {
    mlgLogger = Logger.getLogger(
            clzz != null ? clzz.getName() : this.getClass().getName());
  }

  /**
   *
   * @param clzz
   * @param iLogStackMethodLevel
   */
  public SEDLogger(Class clzz, int iLogStackMethodLevel) {
    mlgLogger = Logger.getLogger(
            clzz != null ? clzz.getName() : this.getClass().getName());
    miMethodStack = iLogStackMethodLevel;
  }

  /**
   *
   * @return
   */
  protected String getCurrentMethodName() {
    return currentThread().getStackTrace().length > miMethodStack
            ? currentThread().getStackTrace()[miMethodStack]
                    .getMethodName() : "NULL METHOD";
  }

  /**
   *
   * @return
   */
  public long getTime() {
    return getInstance().getTimeInMillis();
  }

  /**
   *
   * @param param
   * @return
   */
  public long log(final Object... param) {
    long mlTime = getTime();
    String strParams = null;
    if (param != null && param.length != 0) {
      StringWriter sw = new StringWriter();
      int i = 0;
      for (Object o : param) {
        if (i != 0) {
          sw.append(",");
        }
        sw.append("'" + o + "' ");
        i++;
      }
      strParams = sw.toString();
    }

    mlgLogger.info(
            getCurrentMethodName() + ":" + (strParams != null ? strParams : ""));
    return mlTime;
  }

  /**
   * String formated log
   *
   * @param format - format form
   * @param param - params
   * @return
   */
  public long formatedlog(final String format, final Object... param) {
    long mlTime = getTime();
    mlgLogger.info(getCurrentMethodName() + ":" + String.format(format, param));
    return mlTime;
  }

  public long formatedDebug(final String format, final Object... param) {
    long mlTime = getTime();
    mlgLogger.debug(getCurrentMethodName() + ":" + String.format(format, param));
    return mlTime;
  }

  public long formatedWarning(final String format, final Object... param) {
    long mlTime = getTime();
    mlgLogger.warn(getCurrentMethodName() + ":" + String.format(format, param));
    return mlTime;
  }

  public long formatedError(final String format, final Object... param) {
    long mlTime = getTime();
    mlgLogger.error(getCurrentMethodName() + ":" + String.format(format, param));
    return mlTime;
  }

  /**
   *
   * @param lTime
   * @param param
   */
  public void logEnd(long lTime, final Object... param) {
    String strParams = "";
    if (param != null && param.length != 0) {
      StringWriter sw = new StringWriter();
      int i = 0;
      for (Object o : param) {
        sw.append((++i) + ".-> '" + o + "' ");
      }
      strParams = sw.toString();
    }
    long logTime = (getTime() - lTime);

    mlgLogger.log(logTime < 30000 ? Level.DEBUG : Level.WARN,
            getCurrentMethodName() + ": - END ( " + logTime + " ms) "
            + strParams);

  }
  
  public void logEnd(long lTime, String objectType, String objectId,  String message) {
    long logTime = (getTime() - lTime);
    
    String val = formatLine(getCurrentMethodName(), "END", objectType, objectId, logTime, message, null);
    mlgLogger.log(logTime < 30000 ? Level.DEBUG : Level.WARN,
           val);

  }

  /**
   *
   * @param lTime
   * @param strMessage
   * @param ex
   */
  public void logError(long lTime, String strMessage, Throwable ex) {

    mlgLogger.error(
            getCurrentMethodName() + MSG + strMessage + "'" + (ex == null
                    ? "." : MSG_FC + getFirstCauseMessage(ex) + "')") + "( "
            + (getTime() - lTime) + " ms )", ex);
  }

  public void logError(String strMessage, Throwable ex) {

    mlgLogger.error(
            getCurrentMethodName() + MSG + strMessage + "'" + (ex == null
                    ? "." : MSG_FC + getFirstCauseMessage(ex) + "')"), ex);
  }

  private String formatLine(String method, String logEvent, String objecType,
          String objectId, long lDurTime, String strMessage, Throwable ex) {

    StringBuilder sw = new StringBuilder();
    sw.append(method);
    sw.append(": ");
    sw.append(logEvent).append(" (").append(lDurTime).append(" ms ) ");

    sw.append(objecType).append(' ');
    sw.append(objectId).append(' ');
    sw.append(": ");

    // add message
    if (strMessage != null) {
      sw.append(" msg: '").append(strMessage).append("' ");
    }
    // add error message
    if (ex != null) {
      sw.append(" Err: '").append(ex).append("', FC: '").append(
              getFirstCauseMessage(ex)).append("'.");
    }

    return sw.toString();

  }

  /**
   *
   * @param lTime
   * @param ex
   */
  public void logError(long lTime, Throwable ex) {
    mlgLogger.error(getCurrentMethodName() + MSG
            + (ex != null ? ex.getMessage() : "") + MSG_FC + getFirstCauseMessage(
            ex) + "' ( "
            + (getTime() - lTime) + " ms )", ex);
  }

  /**
   *
   * @param param
   * @return
   */
  public long logStart(final Object... param) {
    long mlTime = getTime();
    String strParams = null;
    if (param != null && param.length != 0) {
      StringWriter sw = new StringWriter();
      int i = 0;
      for (Object o : param) {
        if (i != 0) {
          sw.append(",");
        }
        sw.append((++i) + ":'" + o + "'");
      }
      strParams = sw.toString();
    }

    mlgLogger.trace(getCurrentMethodName() + ": - BEGIN' "
            + (strParams != null ? " params: " + strParams : ""));
    return mlTime;
  }

  /**
   *
   * @param lTime
   * @param strMessage
   * @param ex
   */
  public void logWarn(long lTime, String strMessage, Exception ex) {
    mlgLogger.warn(getCurrentMethodName() + MSG + strMessage + MSG_FC
            + getFirstCauseMessage(ex) + "' ( "
            + (getTime() - lTime) + " ms )", ex);
  }

  /**
   *
   * @param lTime
   * @param strMessage
   * @param ex
   */
  public void logWarn(String strMessage, Exception ex) {
    mlgLogger.warn(getCurrentMethodName() + MSG + strMessage + MSG_FC
            + getFirstCauseMessage(ex) + "'.", ex);
  }

  public void logJAXBObject(String prefix, Object obj) {
    try {
      JAXBContext jc = JAXBContext.newInstance(obj.getClass());
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      StringWriter sw = new StringWriter();
      marshaller.marshal(obj, sw);
      mlgLogger.warn(prefix + sw, null);
    } catch (JAXBException ex) {
      mlgLogger.error("Error marshalling object", ex);
    }
  }

  public Logger getLogger() {
    return mlgLogger;

  }

}
