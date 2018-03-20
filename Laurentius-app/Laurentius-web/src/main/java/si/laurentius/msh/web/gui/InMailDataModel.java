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
import java.util.Calendar;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.msh.table.mail.TableInMail;
import si.laurentius.msh.table.mail.TableOutMail;
import si.laurentius.msh.web.abst.AbstractMailDataModel;
import si.laurentius.msh.web.gui.entities.InMailTableFilter;

/**
 *
 * @author Jože Rihtaršič
 */
public class InMailDataModel extends AbstractMailDataModel<TableInMail> {

  InMailTableFilter imtFilter = null;

  /**
   *
   * @param type
   * @param userSessionData
   * @param db
   */
  public InMailDataModel(Class<TableInMail> type, UserSessionData userSessionData,
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
  public Object getRowKey(TableInMail inMail) {
    return inMail.getId();
  }

  /**
   *
   * @param inMailId
   * @return
   */
  @Override
  public TableInMail getRowData(String inMailId) {
    BigInteger id = new BigInteger(inMailId);

    for (TableInMail player : getCurrentData()) {
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

    return getFilter();
  }

  /**
   *
   * @return
   */
  public InMailTableFilter getFilter() {
    if (imtFilter == null) {
      imtFilter = new InMailTableFilter();
      imtFilter.getReceiverEBoxList().addAll(getUserSessionData().
              getUserEBoxesWithDomain());
      imtFilter.getStatusList().addAll(SEDInboxMailStatus.listOfValues());
      // do not check deleted values - user's request
      imtFilter.getStatusList().remove(SEDInboxMailStatus.DELETED.getValue());
      Calendar c = Calendar.getInstance();
      c.add(Calendar.DATE, -30);
      c.clear(Calendar.MILLISECOND);
      c.clear(Calendar.SECOND);
      c.clear(Calendar.MINUTE);
      c.clear(Calendar.HOUR);
      imtFilter.setReceivedDateFrom(c.getTime());
    }
    return imtFilter;
  }

  /**
   *
   * @param imtFilter
   */
  public void setFilter(InMailTableFilter imtFilter) {
    this.imtFilter = imtFilter;
  }

}
