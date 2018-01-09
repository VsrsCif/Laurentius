/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.lce.sign.pdf;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class SignatureInfo {
  X509Certificate signerCert;
  Calendar date;
  boolean isSignatureValid  = false;
  List<String> errorMessages = new ArrayList<>();

  public X509Certificate getSignerCert() {
    return signerCert;
  }

  public void setSignerCert(X509Certificate signerCert) {
    this.signerCert = signerCert;
  }

  public Calendar getDate() {
    return date;
  }

  public void setDate(Calendar date) {
    this.date = date;
  }

  public boolean isIsSignatureValid() {
    return isSignatureValid;
  }

  public void setIsSignatureValid(boolean isSignatureValid) {
    this.isSignatureValid = isSignatureValid;
  }

  public List<String> getErrorMessages() {
    return errorMessages;
  }

  public void setErrorMessages(List<String> errorMessages) {
    this.errorMessages = errorMessages;
  }
  
  public boolean isSignerCertEquals(X509Certificate crt){
    return signerCert!=null&& crt != null && signerCert.equals(crt);
  
  
  
  }
  
  
  
}
