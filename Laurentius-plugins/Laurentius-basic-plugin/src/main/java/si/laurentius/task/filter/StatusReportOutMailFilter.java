  /*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.task.filter;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Jože Rihtaršič
 */
public class StatusReportOutMailFilter {



  protected List<String> senderEBoxList;

  protected List<String> serviceList;

  protected List<String> statusList;

  protected Date statusDateFrom;
  protected Date statusDateTo;
  
   protected Date submittedDateFrom;
  protected Date submittedDateTo;

  public List<String> getSenderEBoxList() {
    return senderEBoxList;
  }

  public void setSenderEBoxList(List<String> senderEBox) {
    this.senderEBoxList = senderEBox;
  }

  public List<String> getServiceList() {
    return serviceList;
  }

  public void setServiceList(List<String> serviceList) {
    this.serviceList = serviceList;
  }

  public List<String> getStatusList() {
    return statusList;
  }

  public void setStatusList(List<String> statusList) {
    this.statusList = statusList;
  }

  public Date getStatusDateFrom() {
    return statusDateFrom;
  }

  public void setStatusDateFrom(Date statusDateFrom) {
    this.statusDateFrom = statusDateFrom;
  }

  public Date getStatusDateTo() {
    return statusDateTo;
  }

  public void setStatusDateTo(Date statusDateTo) {
    this.statusDateTo = statusDateTo;
  }

  public Date getSubmittedDateFrom() {
    return submittedDateFrom;
  }

  public void setSubmittedDateFrom(Date submittedDateFrom) {
    this.submittedDateFrom = submittedDateFrom;
  }

  public Date getSubmittedDateTo() {
    return submittedDateTo;
  }

  public void setSubmittedDateTo(Date submittedDateTo) {
    this.submittedDateTo = submittedDateTo;
  }

 

}
