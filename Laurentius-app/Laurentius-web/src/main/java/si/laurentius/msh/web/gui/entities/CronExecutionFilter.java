/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.gui.entities;

import java.util.Date;

/**
 *
 * @author sluzba
 */
public class CronExecutionFilter {

  /**
     *
     */
  protected String name;

  /**
     *
     */
  protected Date startTimestampFrom;

  /**
     *
     */
  protected Date startTimestampTo;

  /**
     *
     */
  protected String status;

  /**
     *
     */
  protected String type;

  /**
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @return
   */
  public Date getStartTimestampFrom() {
    return startTimestampFrom;
  }

  /**
   *
   * @return
   */
  public Date getStartTimestampTo() {
    return startTimestampTo;
  }

  /**
   *
   * @return
   */
  public String getStatus() {
    return status;
  }

  /**
   *
   * @return
   */
  public String getType() {
    return type;
  }

  /**
   *
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   *
   * @param startTimestampFrom
   */
  public void setStartTimestampFrom(Date startTimestampFrom) {
    this.startTimestampFrom = startTimestampFrom;
  }

  /**
   *
   * @param startTimestampTo
   */
  public void setStartTimestampTo(Date startTimestampTo) {
    this.startTimestampTo = startTimestampTo;
  }

  /**
   *
   * @param status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   *
   * @param type
   */
  public void setType(String type) {
    this.type = type;
  }

}
