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
package si.digics.laurentius.plugin.zkp.exception;

import si.laurentius.commons.ebms.EBMSErrorCodeInterface;

/**
 *
 * @author Jože Rihtaršič
 */
public enum ZKPErrorCode implements EBMSErrorCodeInterface {

   
   IgnoredAlreadyReceivedMessage("SVEV:0201", "IgnoredAlreadyReceivedMessage", "warning",
      "Processing", "Message was already received in receivers duplicate detection time window! " +
          "According to receivers settings duplicate is eliminated!",
      "reliability"),
   
   
   ReceiverNotExists("SVEV:0202", "Receiver address not exists", "failure",
      "Content", "Message receiver not exists!",
      "application"),
   
    InvalidDeliveryAdvice("SVEV:0203", "InvalidDeliveryAdvice", "failure",
      "Processing", "Message does not match to eny delivery outgoing messages in this MSH! " +
          "According to receivers settings duplicate is eliminated!",
      "reliability"),
   
   
   ServerError("SVEV:0250", "Receiver address not exists", "failure",
      "Content", "Message receiver not exists!",
      "application"),
  
           
           
           ;
           
         
  
  
  


  String code;
  String name;
  String severity;
  String category;
  String description;
  String origin;

  ZKPErrorCode(String cd, String nm, String sv, String ct, String desc, String org) {
    code = cd;
    name = nm;
    severity = sv;
    category = ct;
    description = desc;
    origin = org;
  }

  /**
   *
   * @return
   */
  @Override
  public String getCode() {
    return code;
  }

  /**
   *
   * @return
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   *
   * @return
   */
  @Override
  public String getSeverity() {
    return severity;
  }

  /**
   *
   * @return
   */
  @Override
  public String getCategory() {
    return category;
  }

  /**
   *
   * @return
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   *
   * @return
   */
  @Override
  public String getOrigin() {
    return origin;
  }

}
