/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.tc.web;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.exception.StorageException;

import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.outbox.mail.MSHOutMail;

@SessionScoped
@ManagedBean(name = "tcFictionDeliveryTests")
public class TCFictionDeliveryTests extends TestCaseAbstract implements Serializable {

  private static final SEDLogger LOG = new SEDLogger(TCFictionDeliveryTests.class);
  
  BigInteger mailId;
  Integer daysBeforeNow = 16;

  public BigInteger getMailId() {
    return mailId;
  }

  public void setMailId(BigInteger mailId) {
    this.mailId = mailId;
  }

  public Integer getDaysBeforeNow() {
    return daysBeforeNow;
  }

  public void setDaysBeforeNow(Integer daysBeforeNow) {
    this.daysBeforeNow = daysBeforeNow;
  }
  

  public void changeSentDateAction() {
    if (getMailId() == null) {
      addError("Missing data", "Insert id of out mail!");
      return;
    }

    MSHOutMail mo = mDB.getMailById(MSHOutMail.class, getMailId());
    if (mo == null) {
         addError("Invalid data", String.format("Mail for id %d not exists!", getMailId()));
      return;
    }
    TestCaseMainView li =  (TestCaseMainView)getBean("TestCaseMainView");
    
    
    List<String> ubList =  li.getUserEBoxes();
    String sb = mo.getSenderEBox().substring(0, mo.getSenderEBox().indexOf('@'));
    if (!ubList.contains(sb)){
      addError("Invalid data",  String.format(
                      "Out mail sender '%s' do not belong to user's box list %s ",
                      sb, String.join(",", ubList)));
      return;
    }
    
    if (!Objects.equals(mo.getStatus(), SEDOutboxMailStatus.SENT.getValue())) {
      addError("Invalid data",  String.format(
                      "Out mail'%d' must be in 'SENT' status! (mail status %s!)",
                      getMailId(), mo.getStatus()));
      return;
    }
    
    
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_MONTH, -1*getDaysBeforeNow());
    mo.setSentDate(c.getTime());
    mo.setReceivedDate(c.getTime());
    try {
      mDB.updateOutMail(mo, "Fiction delivery test", getUserName());
    } catch (StorageException ex) {
      addError("Error",  ex.getMessage());
    }
    
    facesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
          String.format("Changed send date to out mail %d!",getMailId()),
            "New send date is: " + c.getTime().toString()));


  
  }

 

  
}
