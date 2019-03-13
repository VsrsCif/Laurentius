/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.lce;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author sluzba
 */
public class DigestUtils {

  private static final SEDLogger LOG = new SEDLogger(DigestUtils.class);
  
  public static String getHexOldSha256Digest(byte[] buff) {
    assert buff != null : "Byte parameter is null";
    try {
      return getHexOldDigest(buff, DigestMethodCode.SHA256.getJcaCode());
    } catch (NoSuchAlgorithmException ex) {
      LOG.logError(String.format("Error caclulating digest for code %s",
              DigestMethodCode.SHA1.getJcaCode()), ex);
    }
    return null;
  }
  
  public static String getHexOldSha256Digest(File f) {
    assert f != null : "File parameter is null";
    try {
      return getHexOldDigest(f, DigestMethodCode.SHA256.getJcaCode());
    } catch (NoSuchAlgorithmException | IOException ex) {
      LOG.logError(String.format(
              "Error caclulating digest for code %s and file %s",
              DigestMethodCode.SHA1.getJcaCode(), f.getAbsolutePath()), ex);
    }
    return null;
  }
  
    public static String getBase64Sha256Digest(File f) {
        assert f != null : "File parameter is null";
        try {
            return getBase64Digest(f, DigestMethodCode.SHA256.getJcaCode());
        } catch (NoSuchAlgorithmException | IOException ex) {
            LOG.logError(String.format(
                    "Error caclulating digest for code %s and file %s",
                    DigestMethodCode.SHA1.getJcaCode(), f.getAbsolutePath()), ex);
        }
        return null;
    }

  public static String getHexSha1Digest(byte[] buff) {
    assert buff != null : "Byte parameter is null";
    try {
      return getHexOldDigest(buff, DigestMethodCode.SHA1.getJcaCode());
    } catch (NoSuchAlgorithmException ex) {
      LOG.logError(String.format("Error caclulating digest for code %s",
              DigestMethodCode.SHA1.getJcaCode()), ex);
    }
    return null;
  }

  public static String getHexSha1Digest(File f) {
    assert f != null : "File parameter is null";
    try {
      return getHexOldDigest(f, DigestMethodCode.SHA1.getJcaCode());
    } catch (NoSuchAlgorithmException | IOException ex) {
      LOG.logError(String.format(
              "Error caclulating digest for code %s and file %s",
              DigestMethodCode.SHA1.getJcaCode(), f.getAbsolutePath()), ex);
    }
    return null;
  }

  public static String getHexMD5Digest(byte[] buff) throws NoSuchAlgorithmException {
    assert buff != null : "Byte parameter is null";
    try {
      return getHexOldDigest(buff, DigestMethodCode.MD5.getJcaCode());
    } catch (NoSuchAlgorithmException ex) {
      LOG.logError(String.format("Error caclulating digest for code %s",
              DigestMethodCode.MD5.getJcaCode()), ex);
    }
    return null;

  }

  public static String getHexMD5Digest(File f) throws NoSuchAlgorithmException, IOException {
    assert f != null : "File parameter is null";
    try {
      return getHexOldDigest(f, DigestMethodCode.MD5.getJcaCode());
    } catch (NoSuchAlgorithmException | IOException ex) {
      LOG.logError(String.format(
              "Error caclulating digest for code %s and file %s",
              DigestMethodCode.MD5.getJcaCode(), f.getAbsolutePath()), ex);
    }
    return null;
  }

  public static String getHexOldDigest(byte[] buff, String jcaName) throws NoSuchAlgorithmException {
    byte[] bres = MessageDigest.getInstance(jcaName).digest(buff);
    return toHexString(bres);
  }

    public static String getBase64Digest(File f, String jcaName) throws NoSuchAlgorithmException, IOException {
        String val = null;
        try (InputStream is = new FileInputStream(f)) {
            val = getBase64Digest(is, jcaName);
        }
        return val;
    }
    
  public static String getHexOldDigest(File f, String jcaName) throws NoSuchAlgorithmException, IOException {
    String val = null;
    try (InputStream is = new FileInputStream(f)) {
      val = getHexOldDigest(is, jcaName);
    }
    return val;
  }

  public static String getHexOldDigest(InputStream is, String jcaName) throws NoSuchAlgorithmException, IOException {

    MessageDigest md = MessageDigest.getInstance(jcaName);
    md.reset();
    byte[] buffer = new byte[1024];
    int len = is.read(buffer);
    while (len != -1) {
      md.update(buffer, 0, len); // calculate Digest
      len = is.read(buffer);
    }

    byte[] hash = md.digest();
    return toHexString(hash);
  }
  
    public static String getBase64Digest(InputStream is, String jcaName) throws NoSuchAlgorithmException, IOException {

        MessageDigest md = MessageDigest.getInstance(jcaName);
        md.reset();
        byte[] buffer = new byte[1024];
        int len = is.read(buffer);
        while (len != -1) {
            md.update(buffer, 0, len); // calculate Digest
            len = is.read(buffer);
        }

        byte[] hash = md.digest();
        return Base64.getEncoder().encodeToString(hash);
    }

  public static String toHexString(byte[] buff) {
    // converting byte array to HexOldadecimal String
    StringBuilder sb = new StringBuilder(2 * buff.length);
    for (byte b : buff) {
      sb.append(String.format("%02x", b & 0xff));
    }
    return sb.toString();
  }

}
