/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.client;

import javax.xml.soap.SOAPMessage;
import si.jrc.msh.exception.EBMSError;

/**
 *
 * @author sluzba
 */
public class Result {
  
  SOAPMessage result;
  String resultFile;
  String mimeType;
  EBMSError error;

  public SOAPMessage getResult() {
    return result;
  }

  public void setResult(SOAPMessage result) {
    this.result = result;
  }

  public String getResultFile() {
    return resultFile;
  }

  public void setResultFile(String resultFile) {
    this.resultFile = resultFile;
  }

  public EBMSError getError() {
    return error;
  }

  public void setError(EBMSError error) {
    this.error = error;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
  
  
  
  
}
