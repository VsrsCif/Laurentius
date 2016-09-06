/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.sec;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.Key;

/**
 *
 * @author sluzba
 */
public class SEDKey implements Key, Serializable {

  String algorithm;
  String format;
  BigInteger id;
  byte[] secretKey;

  /**
     *
     */
  public SEDKey() {}

  /**
   *
   * @param id
   * @param secretKey
   * @param algorithm
   * @param format
   */
  public SEDKey(BigInteger id, byte[] secretKey, String algorithm, String format) {
    this.id = id;
    this.secretKey = secretKey;
    this.algorithm = algorithm;
    this.format = format;
  }

  @Override
  public String getAlgorithm() {
    return algorithm;
  }

  @Override
  public byte[] getEncoded() {
    return secretKey;
  }

  @Override
  public String getFormat() {
    return format;
  }

  /**
   *
   * @return
   */
  public BigInteger getId() {
    return id;
  }

  /**
   *
   * @param algorithm
   */
  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  /**
   *
   * @param secretKey
   */
  public void setEncoded(byte[] secretKey) {
    this.secretKey = secretKey;
  }

  /**
   *
   * @param format
   */
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   *
   * @param id
   */
  public void setId(BigInteger id) {
    this.id = id;
  }

}
