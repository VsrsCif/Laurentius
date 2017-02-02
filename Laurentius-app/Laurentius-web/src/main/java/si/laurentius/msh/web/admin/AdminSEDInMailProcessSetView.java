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

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.msh.web.gui.DialogDelete;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.plugin.processor.InMailProcessorDef;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessorSet;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDInMailProcessSetView")
public class AdminSEDInMailProcessSetView extends AbstractAdminJSFView<SEDProcessorSet> {

  private static final SEDLogger LOG = new SEDLogger(
          AdminSEDInMailProcessSetView.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPlgManager;

  SEDProcessorInstance selectedSPI = null;
   @ManagedProperty(value = "#{dialogDelete}")
  private DialogDelete dlgDelete;

  @Override
  public DialogDelete getDlgDelete() {
    return dlgDelete;
  }
  @Override
  public  void setDlgDelete(DialogDelete dlg){
    dlgDelete = dlg;
  }
  @Override
  public boolean validateData() {

    SEDProcessorSet cj = getEditable();
    if (Utils.isEmptyString(cj.getName())) {
      addError("Name must not be null ");
      return false;
    }
    if (cj.getSEDProcessorInstances().isEmpty()) {
      addError("No processing units!");
      return false;
    }
    return true;
  }

  /**
   *
   */
  @Override
  public void createEditable() {
    SEDProcessorSet ecj = new SEDProcessorSet();

    setNew(ecj);

    addNewInstanceToEditable();

  }

  /**
   *
   */
  @Override
  public void removeSelected() {
    if (getSelected() != null) {
      mdbLookups.removeSEDProcessorSet(getSelected());
      setSelected(null);
    }
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    boolean bsuc = false;
    SEDProcessorSet ecj = getEditable();
    if (ecj != null) {
      mdbLookups.addSEDProcessorSet(ecj);

      bsuc = true;
    }
    return bsuc;

  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    SEDProcessorSet ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      mdbLookups.updateSEDProcessorSet(ecj);

      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDProcessorSet> getList() {
    long l = LOG.logStart();
    List<SEDProcessorSet> lst = mdbLookups.getSEDProcessorSets();
    LOG.logEnd(l, lst != null ? lst.size() : "null");
    return lst;
  }

  public void addNewInstanceToEditable() {
    SEDProcessorSet sps = getEditable();
    if (sps != null) {
      SEDProcessorInstance spi = new SEDProcessorInstance();
      for (Plugin plg : mPlgManager.getRegistredPlugins()) {
        if (!plg.getInMailProcessorDeves().isEmpty()) {
          spi.setPlugin(plg.getType());
          spi.setPluginVersion(plg.getVersion());
          spi.setType(plg.getInMailProcessorDeves().get(0).getType());
          List<String> lstINS = mPlgManager.getInMailProcessorInstances(spi.
                  getPlugin(), spi.getType());
          spi.setInstance(lstINS.isEmpty() ? null : lstINS.get(0));

          break;
        }
      }

      sps.getSEDProcessorInstances().add(spi);
      setSelectedSPI(spi);
    }

  }

  public SEDProcessorInstance getSelectedSPI() {
    return selectedSPI;
  }

  public void setSelectedSPI(SEDProcessorInstance selectedSPI) {
    this.selectedSPI = selectedSPI;
  }

  public void removeEditableInstance() {
    if (getEditable() != null && selectedSPI != null) {
      getEditable().getSEDProcessorInstances().remove(selectedSPI);

    } else {
      addError("Select process instance!");
    }
  }
  public void selectedInstanceTop() {
    if (getEditable() != null && selectedSPI != null) {
      int idx = getEditable().getSEDProcessorInstances().indexOf(selectedSPI);
      if (idx >0) {
        getEditable().getSEDProcessorInstances().remove(selectedSPI);
        getEditable().getSEDProcessorInstances().add(0, selectedSPI);
      }

    } else {
      addError("Select process instance!");
    }
  }
  public void selectedInstanceUp() {
    if (getEditable() != null && selectedSPI != null) {
      int idx = getEditable().getSEDProcessorInstances().indexOf(selectedSPI);
      if (idx >0) {
        getEditable().getSEDProcessorInstances().remove(selectedSPI);
        getEditable().getSEDProcessorInstances().add(--idx, selectedSPI);
      }

    } else {
      addError("Select process instance!");
    }
  }
   public void selectedInstanceDown() {
    if (getEditable() != null && selectedSPI != null) {
      int idx = getEditable().getSEDProcessorInstances().indexOf(selectedSPI);
      if (idx < getEditable().getSEDProcessorInstances().size()-1) {
        getEditable().getSEDProcessorInstances().remove(selectedSPI);
        getEditable().getSEDProcessorInstances().add(++idx, selectedSPI);
      }

    } else {
      addError("Select process instance!");
    }
  }
  
   public void selectedInstanceBottom() {
    if (getEditable() != null && selectedSPI != null) {
      int idx = getEditable().getSEDProcessorInstances().indexOf(selectedSPI);
      if (idx < getEditable().getSEDProcessorInstances().size()-1) {
        getEditable().getSEDProcessorInstances().remove(selectedSPI);
        getEditable().getSEDProcessorInstances().add( selectedSPI);
      }

    } else {
      addError("Select process instance!");
    }
  }

  public void editEditableInstance() {
    if (getEditable() != null && getSelectedSPI() != null) {
      getEditable().getSEDProcessorInstances().remove(getSelectedSPI());

    } else {
      addError("Select process instance!");
    }
  }

  public String getProcessUnitsInfo(SEDProcessorSet sps) {
    StringWriter sw = new StringWriter();

    for (SEDProcessorInstance si : sps.getSEDProcessorInstances()) {
      sw.write(si.getType());
      sw.write(":");
      sw.write(si.getInstance());
      sw.write(",");
    }
    return sw.toString();

  }

  public List<InMailProcessorDef> selectedSPIProcessorList() {
    if (selectedSPI != null && !Utils.isEmptyString(selectedSPI.getPlugin())) {
      Plugin plg = mPlgManager.getPluginByType(selectedSPI.getPlugin());
      if (plg != null) {
        return plg.getInMailProcessorDeves();
      }
    }
    return Collections.emptyList();
  }

  public List<String> selectedSPIProcessorInstanceList() {

    if (selectedSPI != null && !Utils.isEmptyString(selectedSPI.getPlugin())
            && !Utils.isEmptyString(selectedSPI.getType())) {
      return mPlgManager.getInMailProcessorInstances(selectedSPI.getPlugin(),
              selectedSPI.getType());
    }
    return Collections.emptyList();
  }

}
