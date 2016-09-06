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
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.msh.web.abst.AbstractMailDataModel;
import si.laurentius.msh.web.gui.entities.InMailTableFilter;

/**
 *
 * @author Jože Rihtaršič
 */
public class InMailDataModel extends AbstractMailDataModel<MSHInMail> {

  InMailTableFilter imtFilter = new InMailTableFilter();

  /**
   *
   * @param type
   * @param userSessionData
   * @param db
   */
  public InMailDataModel(Class<MSHInMail> type, UserSessionData userSessionData, SEDDaoInterface db) {
    super(type);
    setUserSessionData(userSessionData, db);
  }

  /**
   *
   * @param inMail
   * @return
   */
  @Override
  public Object getRowKey(MSHInMail inMail) {
    return inMail.getId();
  }

  /**
   *
   * @param inMailId
   * @return
   */
  @Override
  public MSHInMail getRowData(String inMailId) {
    BigInteger id = new BigInteger(inMailId);

    for (MSHInMail player : getCurrentData()) {
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
      imtFilter = new InMailTableFilter();
    }
    String strSedBox = getUserSessionData().getCurrentSEDBox();
    imtFilter.getReceiverEBoxList().clear();
    if (strSedBox == null || strSedBox.equalsIgnoreCase("ALL")) {
      imtFilter.getReceiverEBoxList().addAll(getUserSessionData().getUserEBoxes());

    } else {
      imtFilter.getReceiverEBoxList().add(strSedBox);
    }

    return imtFilter;
  }

  /**
   *
   * @return
   */
  public InMailTableFilter getFilter() {
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
