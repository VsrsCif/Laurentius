/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.lce;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bouncycastle.util.encoders.Hex;



/**
 *
 * @author sluzba
 */
public class DigestUtils {
  
  public static String getHexSha1Digest(byte[] buff) throws NoSuchAlgorithmException{
   return getHexDigest(buff, DigestMethodCode.SHA1.getJcaCode());
  }
  
  public static String getHexMD5Digest(byte[] buff) throws NoSuchAlgorithmException{
   return getHexDigest(buff, DigestMethodCode.MD5.getJcaCode());
  }
  
  
  public static String getHexDigest(byte[] buff, String jcaName) throws NoSuchAlgorithmException{
   byte[] bres =  MessageDigest.getInstance(jcaName).digest(buff);
   return Hex.toHexString(bres);
  }
  
}
