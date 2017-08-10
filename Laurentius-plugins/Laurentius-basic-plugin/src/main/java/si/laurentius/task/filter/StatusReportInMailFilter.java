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
public class StatusReportInMailFilter {



  protected List<String> receiverEBox;

  protected List<String> serviceList;

  protected List<String> statusList;

  protected Date statusDateFrom;
  protected Date statusDateTo;
  
  
  protected Date ReceivedDateFrom;
  protected Date ReceivedDateTo;
  
  protected Date SubmittedDateFrom;
  protected Date SubmittedDateTo;


  public List<String> getReceiverEBoxList() {
    return receiverEBox;
  }

  public void setReceiverEBoxList(List<String> receiverEBox) {
    this.receiverEBox = receiverEBox;
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

  public List<String> getReceiverEBox() {
    return receiverEBox;
  }

  public void setReceiverEBox(List<String> receiverEBox) {
    this.receiverEBox = receiverEBox;
  }

  public Date getReceivedDateFrom() {
    return ReceivedDateFrom;
  }

  public void setReceivedDateFrom(Date ReceivedDateFrom) {
    this.ReceivedDateFrom = ReceivedDateFrom;
  }

  public Date getReceivedDateTo() {
    return ReceivedDateTo;
  }

  public void setReceivedDateTo(Date ReceivedDateTo) {
    this.ReceivedDateTo = ReceivedDateTo;
  }

  public Date getSubmittedDateFrom() {
    return SubmittedDateFrom;
  }

  public void setSubmittedDateFrom(Date SubmittedDateFrom) {
    this.SubmittedDateFrom = SubmittedDateFrom;
  }

  public Date getSubmittedDateTo() {
    return SubmittedDateTo;
  }

  public void setSubmittedDateTo(Date SubmittedDateTo) {
    this.SubmittedDateTo = SubmittedDateTo;
  }

 

}
