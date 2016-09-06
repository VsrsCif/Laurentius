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

import java.math.BigInteger;
import si.laurentius.cron.SEDTaskExecution;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.msh.web.abst.AbstractMailDataModel;
import si.laurentius.msh.web.gui.entities.CronExecutionFilter;

/**
 *
 * @author Jože Rihtaršič
 */
public class CronExecutionModel extends AbstractMailDataModel<SEDTaskExecution> {

  CronExecutionFilter imtFilter = new CronExecutionFilter();

  /**
   *
   * @param type
   * @param userSessionData
   * @param db
   */
  public CronExecutionModel(Class<SEDTaskExecution> type, UserSessionData userSessionData,
      SEDDaoInterface db) {
    super(type);
    setUserSessionData(userSessionData, db);
  }

  /**
   *
   * @param inMail
   * @return
   */
  @Override
  public Object getRowKey(SEDTaskExecution inMail) {
    return inMail.getId();
  }

  /**
   *
   * @param inMailId
   * @return
   */
  @Override
  public SEDTaskExecution getRowData(String inMailId) {
    BigInteger id = new BigInteger(inMailId);

    for (SEDTaskExecution player : getCurrentData()) {
      if (id.equals(player.getId())) {
        return player;
      }
    }

    return null;
  }

  /**
   *
   * @return
   */
  @Override
  public Object externalFilters() {
    if (imtFilter == null) {
      imtFilter = new CronExecutionFilter();
    }

    return imtFilter;
  }

  /**
   *
   * @return
   */
  public CronExecutionFilter getFilter() {
    return imtFilter;
  }

  /**
   *
   * @param imtFilter
   */
  public void setFilter(CronExecutionFilter imtFilter) {
    this.imtFilter = imtFilter;
  }

}
