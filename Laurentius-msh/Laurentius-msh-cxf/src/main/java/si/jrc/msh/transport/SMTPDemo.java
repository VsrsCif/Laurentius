/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.transport;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author sluzba
 */
public class SMTPDemo {
  
  public static void main(String...str){
    try {
      MimeMessage m = new MimeMessage(null,  SMTPDemo.class.getResourceAsStream(""));
    } catch (MessagingException ex) {
      ex.printStackTrace();
    }
  
  }
  
}
