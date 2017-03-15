/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.gui.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Jože Rihtaršič
 */
public class CronExecutionFilter {

  /**
     *
     */
  protected String name;
  protected String plugin;
   protected String type;


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
  protected List<String> statusList = null;;

  /**
     *
     */
 
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
  public String getType() {
    return type;
  }

  public String getPlugin() {
    return plugin;
  }

  public void setPlugin(String plugin) {
    this.plugin = plugin;
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
   * @param type
   */
  public void setType(String type) {
    this.type = type;
  }

  public List<String> getStatusList() {
     if (statusList == null) {
      statusList = new ArrayList<>();
    }
    return statusList;
  }

  public void setStatusList(List<String> statusList) {
    this.statusList = statusList;
  }

  
  
}
