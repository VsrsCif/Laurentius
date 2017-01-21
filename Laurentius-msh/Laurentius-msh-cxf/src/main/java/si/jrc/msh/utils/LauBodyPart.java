/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.utils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

/**
 *
 * @author sluzba
 */
public class LauBodyPart  extends MimeBodyPart{

  @Override
  protected void updateHeaders()
      throws MessagingException {
    super.updateHeaders(); 
    setHeader("Content-Transfer-Encoding", "base64");
  }
  
  
  
}
