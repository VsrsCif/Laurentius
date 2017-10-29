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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import org.primefaces.model.DualListModel;
import si.laurentius.ebox.SEDBox;
import si.laurentius.application.SEDApplication;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("adminSEDApplicationView")
public class AdminSEDApplicationView extends AbstractAdminJSFView<SEDApplication> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDApplicationView.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  private DualListModel<SEDBox> msbCBDualList = new DualListModel<>();

  /**
   *
   * @return
   */
  public DualListModel<SEDBox> getCurrentPickupDualSEDBoxList() {

    List<String> sbIDs = new ArrayList<>();
    if (getEditable() != null) {
      getEditable().getSEDBoxes().stream().forEach((sb) -> {
        sbIDs.add(sb.getLocalBoxName());
      });
    }
    List<SEDBox> src = new ArrayList<>();
    List<SEDBox> trg = new ArrayList<>();
    mdbLookups.getSEDBoxes().stream().forEach((se) -> {
      if (sbIDs.contains(se.getLocalBoxName())) {
        trg.add(se);
      } else {
        src.add(se);
      }
    });

    return msbCBDualList = new DualListModel<>(src, trg);
  }

  @Override
  public boolean validateData() {
    SEDApplication su = getEditable();
    // test alias.
    if (Utils.isEmptyString(su.getApplicationId())) {
      addError("ApplicationId must not be empty!");
      return false;
    }

    if (isEditableNew() && mdbLookups.getSEDApplicationById(su.getApplicationId()) != null) {
      addError(String.format("User with id %s already exists!", su.getApplicationId()));
      return false;
    }

    if (!Utils.isEmptyString(su.getEmail()) && !Utils.isValidEmailAddress(su.
            getEmail())) {
      addError(String.format("Email '%s ' is invalid!", su.getEmail()));
      return false;
    }

    if (su.getActiveFromDate() == null) {
      addError("Active From Date must not be null!");
      return false;
    }

    if (su.getActiveToDate() != null && su.getActiveToDate().before(su.
            getActiveFromDate())) {
      addError("Active From Date must be before Active to date!");
      return false;
    }
    return true;
  }

  @Override
  public String getSelectedDesc() {
    if (getSelected() != null) {
      return getSelected().getApplicationId();
    }
    return null;
  }


  /**
   *
   * @param dl
   */
  public void setCurrentPickupDualSEDBoxList(DualListModel<SEDBox> dl) {
    msbCBDualList = dl;
  }

  /**
   *
   * @param username
   * @return
   */
  public SEDApplication getSEDApplicationById(String username) {
    List<SEDApplication> lst = mdbLookups.getSEDApplications();
    for (SEDApplication sb : lst) {
      if (sb.getApplicationId().equalsIgnoreCase(username)) {
        return sb;
      }
    }
    return null;

  }

  /**
   *
   */
  @Override
  public void createEditable() {
    long l = LOG.logStart();

    String sbname = "appl_%03d";
    int i = 1;
    while (getSEDApplicationById(String.format(sbname, i)) != null) {
      i++;
    }

    SEDApplication su = new SEDApplication();
    su.setApplicationId(String.format(sbname, i));
    su.setActiveFromDate(Calendar.getInstance().getTime());

    setNew(su);
    LOG.logEnd(l);
  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    SEDApplication sb = getSelected();
    if (sb != null) {
      bSuc = mdbLookups.removeSEDApplication(sb);
      setSelected(null);
      setSelected(null);      
    } else {
      addError("No item selected");
    }
    return bSuc;
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    SEDApplication sb = getEditable();
    boolean bsuc = false;
    if (sb != null) {
      sb.getSEDBoxes().clear();
      if (msbCBDualList.getTarget() != null && !msbCBDualList.getTarget().
              isEmpty()) {
        sb.getSEDBoxes().addAll(msbCBDualList.getTarget());
      }
      mdbLookups.addSEDApplication(sb);
      bsuc = true;

      setEditable(null);
    }
    return bsuc;
  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    SEDApplication sb = getEditable();
    boolean bsuc = false;
    if (sb != null) {
      sb.getSEDBoxes().clear();
      if (msbCBDualList.getTarget() != null && !msbCBDualList.getTarget().
              isEmpty()) {
        sb.getSEDBoxes().addAll(msbCBDualList.getTarget());
      }
      bsuc = true;
      mdbLookups.updateSEDApplication(sb);
      setEditable(null);
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDApplication> getList() {
    long l = LOG.logStart();
    List<SEDApplication>  lst = mdbLookups.getSEDApplications();    
    LOG.logEnd(l);
    return lst;
    
  }

}
