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



  protected String senderEBox;

  protected List<String> serviceList;

  protected List<String> statusList;

  protected Date statusDateFrom;
  protected Date statusDateTo;

  public String getSenderEBox() {
    return senderEBox;
  }

  public void setSenderEBox(String senderEBox) {
    this.senderEBox = senderEBox;
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

 

}
