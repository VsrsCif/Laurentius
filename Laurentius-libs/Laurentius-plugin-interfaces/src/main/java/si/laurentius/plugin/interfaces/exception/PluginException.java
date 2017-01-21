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
package si.laurentius.plugin.interfaces.exception;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class PluginException extends Exception {
  
  /**
     *
     */
  public enum PluginExceptionCode {

    NullPluginType(1, "Null plugin"),
    UnknownPluginType(2, "Unknown plugin type"),    
    InitPluginException(3, "Initialize plugin exception"),
    ;

    int iCode;
    String strDesc;

    PluginExceptionCode(int i, String desc) {
      iCode = i;
      strDesc = desc;
    }

    /**
     *
     * @return
     */
    public int getCode() {
      return iCode;
    }

    /**
     *
     * @return
     */
    public String getDesc() {
      return strDesc;
    }

  }

  int miCode;

  PluginExceptionCode nTEC;

  /**
   *
   * @param tc
   */
  public PluginException(PluginExceptionCode tc) {
    super(tc.getDesc());
    nTEC = tc;
  }

  /**
   *
   * @param tc
   * @param message
   */
  public PluginException(PluginExceptionCode tc, String message) {
    super(message);
    nTEC = tc;
  }

  /**
   *
   * @param tc
   * @param message
   * @param cause
   */
  public PluginException(PluginExceptionCode tc, String message, Throwable cause) {
    super(message, cause);
    nTEC = tc;
  }

  /**
   *
   * @param tc
   * @param cause
   */
  public PluginException(PluginExceptionCode tc, Throwable cause) {
    super(cause);
    nTEC = tc;
  }

  /**
   *
   * @param tc
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public PluginException(PluginExceptionCode tc, String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    nTEC = tc;
  }


}
