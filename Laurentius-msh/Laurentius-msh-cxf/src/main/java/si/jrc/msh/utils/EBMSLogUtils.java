package si.jrc.msh.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import si.jrc.msh.interceptor.EBMSOutInterceptor;
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_HOME_DIR;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import si.laurentius.commons.SEDSystemProperties;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class EBMSLogUtils {

  public static final String S_FORMAT_FILE = "%s_%010d-r%03d_%s-%s";
  public static final String S_FORMAT_FILE2 = "%s_%s_%s-%s";
  /**
   *
   */
  public static final String S_IN_PREFIX = "in";

  /**
   *
   */
  public static final String S_OUT_PREFIX = "out";

  /**
   *
   */
  public static final String S_PREFIX = "ebms";

  /**
   *
   */
  public static final String S_REQUEST_SUFFIX = "request.soap";

  /**
   *
   */
  public static final String S_RESPONSE_SUFFIX = "response.soap";
  private static final String S_ROOT_FOLDER = "ebms";

  /**
   *
   */
  protected static final SEDLogger LOG = new SEDLogger(EBMSOutInterceptor.class);

  /**
   * Method returs path for LocalDate ${laurentius.home}/storage/[year]/[month]/[day]
   *
   * @param ld - Local date
   * @return returs path
   */
  protected static synchronized Path dateStorageFolder(LocalDate ld) {
    return Paths.get(SEDSystemProperties.getLogFolder().getAbsolutePath(),
        S_ROOT_FOLDER,
        ld.getYear() + "",
        format("%02d", ld.getMonthValue()),
        format("%02d", ld.getDayOfMonth()));
  }

  private static synchronized File currentStorageFolder()
      throws StorageException {

    File f = dateStorageFolder(LocalDate.now()).toFile();
    if (!f.exists() && !f.mkdirs()) {
      throw new StorageException(String.format(
          "Error occurred while creating storage folder: '%s'", f.getAbsolutePath()));
    }
    return f;
  }

  public static File getLogFile(String filename)
      throws StorageException {
    return new File(currentStorageFolder(), filename);
  }

  /**
   *
   * @param f
   * @return
   */
  public static String getBaseFileName(File f) {
    String fn = f.getName();
    return fn.substring(fn.indexOf('_') + 1, fn.lastIndexOf('_'));
  }

  /**
   *
   * @param isRequestor
   * @param baseName
   * @return
   */
  public static File getInboundFileName(boolean isRequestor, String baseName) {
    File f = null;
    try {

      if (Utils.isEmptyString(baseName)) {
        f = getNewFile(S_PREFIX+ "_", "_" +S_IN_PREFIX + "-" +
            (isRequestor ? S_RESPONSE_SUFFIX : S_REQUEST_SUFFIX));
      } else {
        String filename = String.format(S_FORMAT_FILE2, S_PREFIX, baseName, S_IN_PREFIX,
            (isRequestor ? S_RESPONSE_SUFFIX : S_REQUEST_SUFFIX));

        f = EBMSLogUtils.getLogFile(filename);
        int i = 1;
        while (f.exists()) {
          f = EBMSLogUtils.getLogFile(filename + String.format(".%d03", i++));
        }
      }
    } catch (StorageException ex) {
      LOG.logError(0, ex);
    }
    return f;
  }

  private static File getNewFile(String prefix, String suffix)
      throws StorageException {
    File fStore;
    try {
      fStore = File.createTempFile(prefix, suffix, currentStorageFolder());
    } catch (IOException ex) {
      throw new StorageException("Error occurred while creating storage file", ex);
    }
    return fStore;
  }

  /**
   *
   * @param isRequestor
   * @param id
   * @param baseName
   * @return
   */
  public static File getOutboundFileName(boolean isRequestor, BigInteger id, String baseName) {
    File f = null;
    try {
      if (baseName != null) {
        String filename = String.format(S_FORMAT_FILE2, S_PREFIX, baseName, S_OUT_PREFIX,
            (isRequestor ? S_REQUEST_SUFFIX : S_RESPONSE_SUFFIX));

        f = EBMSLogUtils.getLogFile(filename);
        int i = 1;
        while (f.exists()) {
          f = EBMSLogUtils.getLogFile(filename + String.format(".%d03", i++));
        }
      } else {
        int iValRetry = 1;
        String filename = String.format(S_FORMAT_FILE, S_PREFIX, id, iValRetry++, S_OUT_PREFIX,
            (isRequestor ? S_REQUEST_SUFFIX : S_RESPONSE_SUFFIX));

        f = EBMSLogUtils.getLogFile(filename);
        while (f.exists()) {
          filename = String.format(S_FORMAT_FILE, S_PREFIX, id, iValRetry++, S_OUT_PREFIX,
              (isRequestor ? S_REQUEST_SUFFIX : S_RESPONSE_SUFFIX));
          f = EBMSLogUtils.getLogFile(filename);
        }
      }
    } catch (StorageException ex) {
      LOG.logError(0, ex);
    }
    return f;
  }

}
