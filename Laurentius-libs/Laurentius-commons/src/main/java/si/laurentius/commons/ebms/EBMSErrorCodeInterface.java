/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.ebms;

/**
 *
 * @author sluzba
 */
public interface EBMSErrorCodeInterface {

  /**
   *
   * @return
   */
  String getCategory();

  /**
   *
   * @return
   */
  String getCode();

  /**
   *
   * @return
   */
  String getDescription();

  /**
   *
   * @return
   */
  String getName();

  /**
   *
   * @return
   */
  String getOrigin();

  /**
   *
   * @return
   */
  String getSeverity();
  
}
