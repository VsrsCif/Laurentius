/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.mju.plugin;

/**
 *
 * @author sluzba
 */
public class ZPPException  extends Exception {

  /**
   *
   * @param message
   */
  public ZPPException(String message) {
    super(message);
  }

  /**
   *
   * @param message
   * @param cause
   */
  public ZPPException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   *
   * @param cause
   */
  public ZPPException(Throwable cause) {
    super(cause);
  }

  /**
   *
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public ZPPException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
