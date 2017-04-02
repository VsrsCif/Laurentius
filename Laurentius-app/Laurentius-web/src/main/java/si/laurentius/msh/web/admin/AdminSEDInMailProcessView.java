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

import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessor;
import si.laurentius.process.SEDProcessorRule;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDInMailProcessView")
public class AdminSEDInMailProcessView extends AbstractAdminJSFView<SEDProcessor> {

  private static final SEDLogger LOG = new SEDLogger(
          AdminSEDInMailProcessView.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  private PModeInterface mPMode;

  @Override
  public boolean validateData() {
      SEDProcessor cj = getEditable();
    if (Utils.isEmptyString(cj.getName())) {
      addError("Name must not be null ");
      return false;
    }
    if (isEditableNew() && mdbLookups.getSEDProcessorByName(cj.getName()) != null) {
      addError("Name: '" + cj.getName() + "' already exists!");
      return false;
    }

    if (cj.getSEDProcessorInstances().isEmpty()) {
      addError("At least one processor instances must be defined!");
      return false;
    }
    return true;

  }

  /**
   *
   */
  @Override
  public void createEditable() {
     String sbname = "proc_%03d";
    int i = 1;

    while (mdbLookups.getSEDProcessorByName(String.format(sbname, i)) != null) {
      i++;
    }
    SEDProcessor ecj = new SEDProcessor();
    ecj.setName(String.format(sbname, i));
    ecj.setActive(true);
    ecj.setDeliveredOnSuccess(true);

    setNew(ecj);
  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    if (getSelected() != null) {
      bSuc = mdbLookups.removeSEDProcessor(getSelected());
      setSelected(null);

    }
    return bSuc;
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    boolean bsuc = false;
    String sbname = "proc_%03d";
    int i = 1;

    while (mdbLookups.getSEDProcessorByName(String.format(sbname, i)) != null) {
      i++;
    }

    SEDProcessor ecj = getEditable();
    ecj.setName(String.format(sbname, i));
    ecj.setActive(true);
    ecj.setDeliveredOnSuccess(true);

    if (ecj != null) {
      mdbLookups.addSEDProcessor(ecj);

      bsuc = true;
    }
    return bsuc;

  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    SEDProcessor ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      mdbLookups.updateSEDProcessor(ecj);
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDProcessor> getList() {
    long l = LOG.logStart();
    List<SEDProcessor> lst = mdbLookups.getSEDProcessors();
    LOG.logEnd(l, lst != null ? lst.size() : "null");
    return lst;
  }

  public boolean addInstanceToEditable(SEDProcessorInstance spi) {
    boolean bsuc = false;
    SEDProcessor pr = getEditable();
    if (pr != null) {
      bsuc = pr.getSEDProcessorInstances().add(spi);
    } else {
      addError("No editable process rule!");
    }
    return bsuc;
  }

  public boolean removeInstanceFromEditable(SEDProcessorInstance spi) {
    boolean bsuc = false;
    SEDProcessor pr = getEditable();
    if (pr != null) {
      bsuc = pr.getSEDProcessorInstances().remove(spi);
    } else {
      addError("No editable process rule!");
    }
    return bsuc;
  }

  public boolean updateInstanceFromEditable(SEDProcessorInstance spiOld,
          SEDProcessorInstance spiNew) {
    boolean bsuc = false;
    SEDProcessor pr = getEditable();
    if (pr != null) {
      int i = pr.getSEDProcessorInstances().indexOf(spiOld);
      pr.getSEDProcessorInstances().remove(i);
      pr.getSEDProcessorInstances().add(i, spiNew);
      bsuc = true;

    } else {
      addError("No editable process rule!");
    }
    return bsuc;
  }

  public boolean addRuleToEditable(SEDProcessorRule spi) {
    boolean bsuc = false;
    SEDProcessor pr = getEditable();
    if (pr != null) {
      bsuc = pr.getSEDProcessorRules().add(spi);
    } else {
      addError("No editable process rule!");
    }
    return bsuc;
  }

  public boolean removeRuleFromEditable(SEDProcessorRule spi) {
    boolean bsuc = false;
    SEDProcessor pr = getEditable();
    if (pr != null) {
      bsuc = pr.getSEDProcessorRules().remove(spi);
    } else {
      addError("No editable process rule!");
    }
    return bsuc;
  }

  public boolean updateRuleFromEditable(SEDProcessorRule spiOld,
          SEDProcessorRule spiNew) {
    boolean bsuc = false;
    SEDProcessor pr = getEditable();
    if (pr != null) {
      int i = pr.getSEDProcessorRules().indexOf(spiOld);
      pr.getSEDProcessorRules().remove(i);
      pr.getSEDProcessorRules().add(i, spiNew);
      bsuc = true;

    } else {
      addError("No editable process rule!");
    }
    return bsuc;
  }

  public String getRuleDesc(SEDProcessor pr) {
    String strVal = "";
    if (pr != null && pr.getSEDProcessorRules().size() > 0) {
      strVal = pr.getSEDProcessorRules().stream().
              map((prr) -> prr.getProperty() + " " + prr.getPredicate() + " " + prr.
              getValue() + ",").
              reduce(strVal,
                      String::concat);
    }
    return strVal;

  }


 @Override
  public String getSelectedDesc() {
    SEDProcessor sel =  getSelected();
    if (sel!=null) {
      return sel.getName();
    }
    return  null;
  }
}
