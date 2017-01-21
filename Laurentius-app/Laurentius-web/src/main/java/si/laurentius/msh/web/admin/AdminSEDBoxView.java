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
package si.laurentius.msh.web.admin;

import java.util.Calendar;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.ebox.SEDBox;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;


/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDBoxView")
public class AdminSEDBoxView extends AbstractAdminJSFView<SEDBox> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDBoxView.class);

  @EJB(mappedName = SEDJNDI.JNDI_DBSETTINGS)
  private DBSettingsInterface mdbSettings;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPMode;



  /**
   *
   * @param sedBox
   * @return
   */
  public SEDBox getSEDBoxByLocalName(String sedBox) {
    return mdbLookups.getSEDBoxByAddressName(sedBox);
  }

  /**
   *
   */
  @Override
  public void createEditable() {
    long l = LOG.logStart();

    String sbname = "name.%03d";
    int i = 1;
    while (getSEDBoxByLocalName(String.format(sbname, i)) != null) {
      i++;
    }
    SEDBox sbx = new SEDBox();
    sbx.setLocalBoxName(String.format(sbname, i));
    sbx.setActiveFromDate(Calendar.getInstance().getTime());
    setNew(sbx);
    LOG.logEnd(l);
  }

  /**
   *
   */
  @Override
  public void removeSelected() {
    SEDBox sb = getSelected();
    if (sb != null) {

      mdbLookups.removeSEDBox(sb);
      setSelected(null);

    }

  }



  /**
   *
   */
  @Override
  public boolean persistEditable() {
    SEDBox sb = getEditable();
    boolean bsuc = false;
    if (sb != null) {
      mdbLookups.addSEDBox(sb);
      setEditable(null);
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {

    SEDBox sb = getEditable();
    boolean bsuc = false;
    if (sb != null) {
      mdbLookups.updateSEDBox(sb);
      setEditable(null);
      return bsuc;

    }
    return bsuc;
  }

  @Override
  public boolean validateData() {

    return true;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDBox> getList() {
    return mdbLookups.getSEDBoxes();
  }

}
