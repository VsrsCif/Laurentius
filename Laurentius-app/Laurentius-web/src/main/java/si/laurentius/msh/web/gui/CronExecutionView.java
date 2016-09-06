/*
 * Copyright 2016, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.msh.web.gui;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import si.laurentius.cron.SEDTaskExecution;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDTaskStatus;
import si.laurentius.commons.interfaces.SEDDaoInterface;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "CronExecutionView")
public class CronExecutionView implements Serializable {

  private static final long serialVersionUID = 1L;

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @ManagedProperty(value = "#{userSessionData}")
  private UserSessionData userSessionData;

  /**
     *
     */
  protected CronExecutionModel mMailModel = null;

  /**
     *
     */
  protected SEDTaskExecution mcurrent;

  @PostConstruct
  private void init() {
    mMailModel = new CronExecutionModel(SEDTaskExecution.class, userSessionData, mDB);
  }

  /**
   *
   * @param messageBean
   */
  public void setUserSessionData(UserSessionData messageBean) {
    this.userSessionData = messageBean;
  }

  /**
   *
   * @return
   */
  public UserSessionData getUserSessionData() {
    return this.userSessionData;
  }

  /**
   *
   * @return
   */
  public SEDTaskExecution getCurrent() {
    return mcurrent;
  }

  /**
   *
   * @param mail
   */
  public void setCurrent(SEDTaskExecution mail) {
    this.mcurrent = mail;
  }

  /**
   *
   * @param om
   * @return
   */
  public int rowIndex(SEDTaskExecution om) {
    return mMailModel.getRowIndex();
  }

  /**
   *
   * @return
   */
  public CronExecutionModel getModel() {
    return (CronExecutionModel) mMailModel;
  }

  /**
   *
   * @param event
   */
  public void onRowSelect(SelectEvent event) {
    if (event != null) {
      setCurrent((SEDTaskExecution) event.getObject());
    } else {
      setCurrent(null);
    }
  }

  /**
   *
   * @param event
   */
  public void onRowUnselect(UnselectEvent event) {
    setCurrent(null);
  }

  /**
   *
   * @param status
   * @return
   */
  public String getStatusColor(String status) {
    return SEDTaskStatus.getColor(status);
  }

}
