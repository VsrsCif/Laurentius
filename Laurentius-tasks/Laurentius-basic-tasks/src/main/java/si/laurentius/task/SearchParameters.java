/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.task;

import java.util.Date;

/**
 *
 * @author Jože Rihtaršič
 */
public class SearchParameters {

  Date receivedDateTo;

  Date submittedDateTo;

  /**
   *
   * @return
   */
  public Date getReceivedDateTo() {
    return receivedDateTo;
  }

  /**
   *
   * @return
   */
  public Date getSubmittedDateTo() {
    return submittedDateTo;
  }

  /**
   *
   * @param receivedDateTo
   */
  public void setReceivedDateTo(Date receivedDateTo) {
    this.receivedDateTo = receivedDateTo;
  }

  /**
   *
   * @param submittedDateTo
   */
  public void setSubmittedDateTo(Date submittedDateTo) {
    this.submittedDateTo = submittedDateTo;
  }
}
