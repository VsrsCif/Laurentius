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

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;

/**
 * MISC Utils
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class Utils {

  /**
   * Thanks to:
   * https://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
   */
  private static final Pattern EMAIL_PATTEREN = Pattern.compile(
          "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
          + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

  /**
   *
   */
  private static Utils mInstance = null;

  /**
   *
   * @param strVal
   * @return
   */
  public static String getDomainFromAddress(String strVal) {
    if (isEmptyString(strVal)) {
      return "NO_DOMAIN";
    }
    if (strVal.contains("@")) {
      return strVal.substring(strVal.indexOf('@') + 1);
    }
    return strVal;

  }

  /**
   *
   * @return
   */
  public static synchronized Utils getInstance() {
    return mInstance = mInstance == null ? new Utils() : mInstance;
  }

  /**
   *
   * @param mim
   * @return
   */
  public static String getPModeIdFromInMail(MSHInMail mim) {
    if (mim == null) {
      return null;
    }
    return mim.getService() + ":" + getDomainFromAddress(mim.getSenderEBox());
  }

  /**
   *
   * @param mom
   * @return
   */
  public static String getPModeIdFromOutMail(MSHOutMail mom) {
    if (mom == null) {
      return null;
    }
    return mom.getService() + ":" + getDomainFromAddress(mom.getReceiverEBox());
  }

  /**
   *
   * @param strVal
   * @return
   */
  public static boolean isEmptyString(String strVal) {
    return strVal == null || strVal.trim().isEmpty();
  }
  
  public static boolean equalsEmptyString(String str1, String str2){
    return Objects.equals(str1, str2)
                ||  (isEmptyString(str1) // test equality for empty string
              &&  isEmptyString(str2));
              
  
  }

  public static String getUUID(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString();
  }

  public static String getUUIDWithDomain(String domain) {
    return UUID.randomUUID().toString() + "@" + domain;
  }

  public static String getInitCauseMessage(Throwable tw) {
    String msg = tw.getMessage();
    Throwable tst = tw;
    while ((tst = tst.getCause()) != null) {
      msg = tst.getMessage();
    }
    return msg;
  }

  public static Throwable getInitCause(Throwable tw) {
    String msg = tw.getMessage();
    Throwable tst = tw;
    while (tst.getCause() != null) {
      tst = tst.getCause();
    }
    return tst;
  }

  private Utils() {

  }

  /**
   * Returns java.util.UUID.randomUUID() as string.
   *
   * @return uuid string representation.
   */
  public String getGuidString() {
    return UUID.randomUUID().toString();
  }

  public static boolean isValidEmailAddress(String strVal) {
    if (isEmptyString(strVal)) {
      return false;
    } else {

      Matcher m = EMAIL_PATTEREN.matcher(strVal);
      return m.matches();
    }
  }

  /**
   * Thansk to aioobe
   * http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
   * @param bytes
   * @param si
   * @return 
   */
  public static String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

}
