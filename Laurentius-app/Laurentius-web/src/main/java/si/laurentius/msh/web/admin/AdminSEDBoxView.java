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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import si.laurentius.application.SEDApplication;
import si.laurentius.ebox.SEDBox;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.user.SEDUser;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("adminSEDBoxView")
public class AdminSEDBoxView extends AbstractAdminJSFView<SEDBox> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDBoxView.class);
  String ePattern = "^(?:[a-z0-9!#$%&'*+\\/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+\\/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")$";
  Pattern regExpPattern = Pattern.compile(ePattern);

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  /**
   *
   * @param sedBox
   * @return
   */
  public SEDBox getSEDBoxByLocalName(String sedBox) {
    return mdbLookups.getSEDBoxByLocalName(sedBox);
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
  public boolean removeSelected() {
    boolean bSuc = false;
    SEDBox sb = getSelected();
    if (sb != null) {
      List<String> lstusr = new ArrayList<>();
      List<SEDUser> lst = mdbLookups.getSEDUsers();
      for (SEDUser u : lst) {
        for (SEDBox s : u.getSEDBoxes()) {
          if (Objects.equals(s.getLocalBoxName(), sb.getLocalBoxName())) {
            lstusr.add(u.getUserId());
            break;
          }
        }
      }
      if (!lstusr.isEmpty()) {
        addError("To delete, remove box from users: " + String.join(",", lstusr));
      } else {
        mdbLookups.removeSEDBox(sb);
        setSelected(null);
        bSuc = true;
      }
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
      bsuc = true;
    }
    return bsuc;
  }

  @Override
  public boolean validateData() {

    SEDBox sb = getEditable();
    // test alias.
    if (sb == null) {
      addError("No editable selected!");
      return false;
    }

    // test alias.
    if (Utils.isEmptyString(sb.getLocalBoxName())) {
      addError("Name must not be empty!");
      return false;
    }

    if (isEditableNew() && mdbLookups.getSEDBoxByLocalName(sb.getLocalBoxName()) != null) {
      addError(String.format("Sedbox %s already exists!", sb.getLocalBoxName()));
      return false;
    }

    Matcher m = regExpPattern.matcher(sb.getLocalBoxName());
    if (!m.matches()) {
      addError("Local part box name is not valid!");
      return false;
    }

    if (sb.getActiveFromDate() == null) {
      addError("Active From Date must not be null!");
      return false;
    }

    if (sb.getActiveToDate() != null && sb.getActiveToDate().before(sb.
            getActiveFromDate())) {
      addError("Active From Date must be before Active to date!");
      return false;
    }

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

  @Override
  public String getSelectedDesc() {
    if (getSelected() != null) {
      return String.format("%s@%s", getSelected().getLocalBoxName(),
              SEDSystemProperties.getLocalDomain());
    }
    return null;
  }



  public List<SEDUser> getEditableUsers() {
    SEDBox ed = getEditable();
    if (ed != null) {
      List<SEDUser> lstusr = new ArrayList<>();
      List<SEDUser> lst = mdbLookups.getSEDUsers();
      for (SEDUser u : lst) {
        for (SEDBox s : u.getSEDBoxes()) {
          if (Objects.equals(s.getLocalBoxName(), ed.getLocalBoxName())) {
            lstusr.add(u);
            break;
          }
        }
      }
      return lstusr;
    } else {
      return Collections.emptyList();
    }

  }
  
  public List<SEDApplication> getEditableApplications() {
    SEDBox ed = getEditable();
    if (ed != null) {
      List<SEDApplication> lstusr = new ArrayList<>();
      List<SEDApplication> lst = mdbLookups.getSEDApplications();
      for (SEDApplication u : lst) {
        for (SEDBox s : u.getSEDBoxes()) {
          if (Objects.equals(s.getLocalBoxName(), ed.getLocalBoxName())) {
            lstusr.add(u);
            break;
          }
        }
      }
      return lstusr;
    } else {
      return Collections.emptyList();
    }

  }
;

}
