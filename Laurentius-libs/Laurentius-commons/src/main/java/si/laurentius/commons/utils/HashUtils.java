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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.String.format;
import java.security.MessageDigest;
import static java.security.MessageDigest.getInstance;
import java.security.NoSuchAlgorithmException;
import si.laurentius.commons.exception.HashException;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class HashUtils {

  
  public static final String MD_ALGORITHM_MD5 = "MD5";
  public static final String MD_ALGORITHM_SHA_1 = " SHA-1";
  public static final String MD_ALGORITHM_SHA_256 = " SHA-256";
  

  MessageDigest mdMD5 = null;

  /**
   * Method returs MD5 value of file
   * @param file   
   * @return MD5 value in Hexadecimal String
   * @throws HashException is thrown if file can not be readed.
   */
  public String getMD5Hash(File file) throws HashException {
    try (FileInputStream fis = new FileInputStream(file)) {
      return getMD5Hash(fis);
    } catch (IOException ex) {
      throw new HashException("Error reading file '" + file.getAbsolutePath() + "'.", ex);
    }
  }

/**
   * Method returs MD5 value of file
   * @param filePath   
   * @return MD5 value in Hexadecimal String
   * @throws HashException is thrown if file can not be readed.
   */
  public String getMD5Hash(String filePath) throws HashException {
    try (FileInputStream fis = new FileInputStream(filePath)) {
      return getMD5Hash(fis);
    } catch (IOException ex) {
      throw new HashException("Error reading file '" + filePath + "'.", ex);
    }
  }

  /**
   * Method returs MD5 hash alogorith
   * @param is
   * @return MD5 value in Hexadecimal String
   * @throws HashException
   */
  public String getMD5Hash(InputStream is) throws HashException {
    String strHash = null;
    try {
      MessageDigest md5 = getMD5MessageDigest();
      md5.reset();
      byte[] buffer = new byte[1024];
      int len = is.read(buffer);
      while (len != -1) {
        md5.update(buffer, 0, len); // calculate MD5Digest
        len = is.read(buffer);
      }
      byte[] hash = md5.digest();

      // converting byte array to Hexadecimal String
      StringBuilder sb = new StringBuilder(2 * hash.length);
      for (byte b : hash) {
        sb.append(format("%02x", b & 0xff));
      }
      strHash = sb.toString();
    } catch (NoSuchAlgorithmException ex) {
      throw new HashException("System error. Check deployment, missing MD5 (MessageDigest) algoritem.", ex);
    } catch (IOException ex) {
      throw new HashException("Error reading inputstream", ex);
    }
    return strHash;
  }

  private MessageDigest getMD5MessageDigest() throws NoSuchAlgorithmException {
    return mdMD5 == null ? (mdMD5 = getInstance(MD_ALGORITHM_MD5)) : mdMD5;
  }

}
