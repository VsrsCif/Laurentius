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
package si.laurentius.commons;

import java.io.File;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import si.laurentius.commons.utils.StringFormater;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class SEDSystemProperties {

  private static final int IN_BRACKET = 2;
  private static final int NORMAL = 0;
  private static final int SEEN_DOLLAR = 1;
  
  /**
   * System property configuration folder .
   */
  public static final String SYS_PROP_CONF_DIR = "laurentius.conf.dir";
  /**
   * Crl list folders
   */
  public static final String SYS_PROP_CRL_DIR = "laurentius.crl.dir";
  /**
   * System property for database dialect
   */
  public static final String SYS_PROP_DB_DIALECT = "laurentius.hibernate.dialect";
  /**
   * System property for creating/updating database objects
   *
   */
  public static final String SYS_PROP_DB_HBM2DLL = "laurentius.hibernate.hbm2ddl.auto";
  /**
   * System property for out qeue workers.
   *
   */
  public static final String SYS_PROP_EXECUTION_WORKERS
          = "si.laurentius.msh.execution.workers.count";

  /**
   * System property for SED home directory.
   */
  public static final String SYS_PROP_HOME_DIR = "laurentius.home";
  /**
   * System property for initialize data from init folder
   *
   */
  public static final String SYS_PROP_INITIALIZE = "laurentius.init";

  /**
   * System property initialization folder .
   */
  public static final String SYS_PROP_INIT_DIR = "laurentius.init.dir";
  /**
   *
   */
  public static final String SYS_PROP_JNDI_JMS_PREFIX = "laurentius.jndi.jms.prefix";
  /**
   * System property for JNID prefix: wildfly: java:/jms/ jetty: java:comp/env/
   * junit test: ''
   *
   * <p>
   * If system property is not given, max 5 outgoing workers are initiated.
   * Workers handle outbox messages.
   * </p>
   */
  public static final String SYS_PROP_JNDI_PREFIX = "laurentius.jndi.prefix";
  public static final String SYS_PROP_KEYSTORE_FILE = "laurentius.certstore.file";
  public static final String SYS_PROP_KEYSTORE_TYPE = "laurentius.certstore.type";
  /**
   * System property for domain.
   */
  public static final String SYS_PROP_LAU_DOMAIN = "laurentius.domain";

  /**
   * System property log folder .
   */
  public static final String SYS_PROP_LOG_DIR = "laurentius.log.dir";

  /**
   * System property log folder .
   */
  public static final String SYS_PROP_PLUGINS_DIR = "laurentius.plugins.dir";

  /**
   * System property for pmode configuration file.
   */
  public static final String SYS_PROP_PMODE_FILE = "laurentius.pmode";
  /**
   *
   */
  public static final String SYS_PROP_QUEUE_SENDER_WORKERS
          = "si.laurentius.msh.sender.workers.count";

  public static final String SYS_PROP_ROOT_CA_FILE = "laurentius.root_ca.file";
  public static final String SYS_PROP_ROOT_CA_TYPE = "laurentius.root_ca.type";
  
  /**
   * System property for set security folder (keystor, crl list, root ca ).
   */
  public static final String SYS_PROP_SECURITY_DIR = "laurentius.security.dir";
  /**
   * System property storage folder .
   */
  public static final String SYS_PROP_STORAGE_DIR = "laurentius.storage.dir";

  /**
   * JAVA SYSTEM PROXY SETTINGS
   */
  public static final String PROXY_HTTP_HOST = "http.proxyHost";
  public static final String PROXY_HTTP_PORT = "http.proxyPort";
  public static final String PROXY_HTTP_NO_PROXY = "http.nonProxyHost";
  public static final String PROXY_HTTPS_HOST = "https.proxyHost";
  public static final String PROXY_HTTPS_PORT = "https.proxyPort";
  public static final String PROXY_HTTPS_NO_PROXY = "https.nonProxyHosts";
  public static final String PROXY_FTP_HOST = "ftp.proxHost";
  public static final String PROXY_FTP_PORT = "ftp.proxyPort";
  public static final String PROXY_FTP_NO_PROXY = "ftp.nonProxyHosts";
  public static final String SYS_PROP_WORK_FREE_DAYS = "laurentius.work.free.days";


  private static final Map<String, String> S_DEF_VALUES = new HashMap<>();
  private static final Map<String, File> S_INIT_FILES_FOLDERS = new HashMap<>();
  

  static {
    S_DEF_VALUES.put(SYS_PROP_HOME_DIR, System.getProperty(
            "user.dir") + File.separator + "laurentius-home");
    S_DEF_VALUES.put(SYS_PROP_CONF_DIR, "${laurentius.home}/conf");
    S_DEF_VALUES.put(SYS_PROP_SECURITY_DIR, "${laurentius.conf.dir}/security");
    S_DEF_VALUES.put(SYS_PROP_CRL_DIR, "${laurentius.security.dir}/crl");
    S_DEF_VALUES.put(SYS_PROP_INIT_DIR, "${laurentius.conf.dir}/init");
    S_DEF_VALUES.put(SYS_PROP_LOG_DIR, "${laurentius.home}/log");
    S_DEF_VALUES.put(SYS_PROP_STORAGE_DIR, "${laurentius.home}/storage");
    S_DEF_VALUES.put(SYS_PROP_PLUGINS_DIR, "${laurentius.home}/plugins");

    S_DEF_VALUES.put(SYS_PROP_PMODE_FILE, "pmode-conf.xml");
    S_DEF_VALUES.put(SYS_PROP_KEYSTORE_FILE, "laurentius.jks");
    S_DEF_VALUES.put(SYS_PROP_KEYSTORE_TYPE, "JKS");   
    S_DEF_VALUES.put(SYS_PROP_ROOT_CA_FILE, "root-ca.jks");
    S_DEF_VALUES.put(SYS_PROP_ROOT_CA_TYPE, "JKS");   

    S_DEF_VALUES.put(SYS_PROP_LAU_DOMAIN, "test-laurentius.org");

    if (getProperty(SYS_PROP_QUEUE_SENDER_WORKERS) == null) {
      setProperty(SYS_PROP_QUEUE_SENDER_WORKERS, "5");
    }
    if (getProperty(SYS_PROP_EXECUTION_WORKERS) == null) {
      setProperty(SYS_PROP_EXECUTION_WORKERS, "5");
    }

  }

  public static synchronized void clear() {

    System.getProperties().remove(SYS_PROP_CONF_DIR);
    System.getProperties().remove(SYS_PROP_HOME_DIR);
    System.getProperties().remove(SYS_PROP_INIT_DIR);
    System.getProperties().remove(SYS_PROP_LOG_DIR);
    System.getProperties().remove(SYS_PROP_SECURITY_DIR);
    System.getProperties().remove(SYS_PROP_STORAGE_DIR);
    S_INIT_FILES_FOLDERS.clear();
  }

  public static File getCRLFolder() {
    return getFile(SYS_PROP_CRL_DIR, true);

  }

  public static File getCertstoreFile() {
    return getFile(getSecurityFolder(), SYS_PROP_KEYSTORE_FILE,
            false);
  }
  public static String getCertstoreType() {
    return System.getProperty(SYS_PROP_KEYSTORE_TYPE,
            S_DEF_VALUES.get(SYS_PROP_KEYSTORE_TYPE));
  }

  public static File getConfFolder() {
    return getFile(SYS_PROP_CONF_DIR, true);

  }

  public static String getDefValue(String key) {
    return S_DEF_VALUES.get(key);
  }

  private static synchronized File getFile(String property,
          boolean isFolder) {
    return getFile(null, property, isFolder);
  }

  private static synchronized File getFile(File parent, String property,
          boolean isFolder) {

    if (!S_INIT_FILES_FOLDERS.containsKey(property)) {
      String val = System.getProperty(property, S_DEF_VALUES.
              getOrDefault(property,
                      ""));
      int iTry = 10;
      while (val.contains("${")) {
        val = replaceProperties(val);
        if (--iTry < 0) {
          throw new IllegalArgumentException(
                  String.format(
                          "System property '%s' value '%s' could not be normalized!",
                          property, val));
        }

      }
      File f = new File(parent, val);
      if (isFolder && !f.exists()) {
        if (!f.mkdirs()) {
          throw new IllegalArgumentException(
                  String.format(
                          "Could not create folder '%s' for system property '%s'!",
                          val, property));
        };
      }
      // replace system property
      System.setProperty(property, val);
      // cache file
      S_INIT_FILES_FOLDERS.put(property, f);

    }
    return S_INIT_FILES_FOLDERS.get(property);
  }

  public static File getHomeFolder() {
    return getFile(SYS_PROP_HOME_DIR, true);

  }

  public static File getInitFolder() {
    return getFile(SYS_PROP_INIT_DIR, true);

  }

  public static String getLocalDomain() {
    return System.getProperty(SYS_PROP_LAU_DOMAIN,
            S_DEF_VALUES.get(SYS_PROP_LAU_DOMAIN));
  }

  public static File getLogFolder() {
    return getFile(SYS_PROP_LOG_DIR, true);
  }

  public static File getPModeFile() {
    return getFile(getConfFolder(), SYS_PROP_PMODE_FILE,
            false);
  }

  public static File getPluginsFolder() {
    return getFile(SYS_PROP_PLUGINS_DIR, true);

  }

  public static File getRootCAStoreFile() {
    return getFile(getSecurityFolder(), SYS_PROP_ROOT_CA_FILE,
            false);
  }
  public static String getRootCAStoreType() {
    return System.getProperty(SYS_PROP_ROOT_CA_TYPE,
            S_DEF_VALUES.get(SYS_PROP_ROOT_CA_TYPE));
  }
  
  public static File getSecurityCRLFolder() {
    return getFile(SYS_PROP_CRL_DIR, true);

  }

  public static File getSecurityFolder() {
    return getFile(SYS_PROP_SECURITY_DIR, true);

  }

  public static File getStorageFolder() {
    return getFile(SYS_PROP_STORAGE_DIR, true);
  }
  
  
  public static boolean isWorkFreeDay(){
     String wfd = System.getProperty(SYS_PROP_WORK_FREE_DAYS, "");
     String cdISO = StringFormater.geFormatedISO8601CurrentDate();    
    return wfd.contains(cdISO);
  }

  public static boolean isInitData() {
    return System.getProperty(SYS_PROP_INITIALIZE, "false").equalsIgnoreCase(
            "true");
  }

  /**
   * Method is "borrowed" from org.jboss.util.StringPropertyReplacer; Go through
   * the input string and replace any occurance of ${p} with the
   * System.getProperty(p) value. If there is no such property p defined, then
   * the ${p} is replaced with "".
   *
   *
   * @param string - the string with possible ${} references
   * @return the input string with all property references replaced if any. If
   * there are no valid references the input string will be returned.
   */
  public static String replaceProperties(final String string) {
    final char[] chars = string.toCharArray();
    StringBuilder buffer = new StringBuilder();
    boolean properties = false;
    int state = NORMAL;
    int start = 0;
    for (int i = 0; i < chars.length; ++i) {
      char c = chars[i];

      // Dollar sign outside brackets
      if (c == '$' && state != IN_BRACKET) {
        state = SEEN_DOLLAR;
      } // Open bracket immediatley after dollar
      else if (c == '{' && state == SEEN_DOLLAR) {
        buffer.append(string.substring(start, i - 1));
        state = IN_BRACKET;
        start = i - 1;
      } // No open bracket after dollar
      else if (state == SEEN_DOLLAR) {
        state = NORMAL;
      } // Closed bracket after open bracket
      else if (c == '}' && state == IN_BRACKET) {
        // No content
        if (start + 2 == i) {
          buffer.append("${}"); // REVIEW: Correct?
        } else // Collect the system property
        {
          String key = string.substring(start + 2, i);
          properties = true;
          buffer.append(getProperty(key, S_DEF_VALUES.getOrDefault(key, "")));
        }
        start = i + 1;
        state = NORMAL;
      }
    }

    // No properties
    if (properties == false) {
      return string;
    }

    // Collect the trailing characters
    if (start != chars.length) {
      buffer.append(string.substring(start, chars.length));
    }
    // Done
    return buffer.toString();
  }
}
