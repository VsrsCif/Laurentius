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
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.msh.web.abst.AbstractMailDataModel;
import si.laurentius.msh.web.gui.entities.OutMailTableFilter;

/**
 *
 * @author Jože Rihtaršič
 */
public class OutMailDataModel extends AbstractMailDataModel<MSHOutMail> {

  OutMailTableFilter outFilter = new OutMailTableFilter();

  /**
   *
   * @param type
   * @param messageBean
   * @param db
   */
  public OutMailDataModel(Class<MSHOutMail> type, UserSessionData messageBean, SEDDaoInterface db) {
    super(type);
    setUserSessionData(messageBean, db);
  }

  /**
   *
   * @param inMail
   * @return
   */
  @Override
  public Object getRowKey(MSHOutMail inMail) {
    return inMail.getId();
  }

  /**
   *
   * @param inMailId
   * @return
   */
  @Override
  public MSHOutMail getRowData(String inMailId) {
    BigInteger id = new BigInteger(inMailId);

    for (MSHOutMail player : getCurrentData()) {
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
    if (outFilter == null) {
      outFilter = new OutMailTableFilter();
    }
    String strSedBox = getUserSessionData().getCurrentSEDBox();
    outFilter.getSenderEBoxList().clear();
    if (strSedBox == null || strSedBox.equalsIgnoreCase("ALL")) {
      outFilter.getSenderEBoxList().addAll(getUserSessionData().getUserEBoxes());

    } else {
      outFilter.getSenderEBoxList().add(strSedBox);
    }

    return outFilter;
  }

  /**
   *
   * @return
   */
  public OutMailTableFilter getFilter() {
    return outFilter;
  }

  /**
   *
   * @param imtFilter
   */
  public void setFilter(OutMailTableFilter imtFilter) {
    this.outFilter = imtFilter;
  }

}
