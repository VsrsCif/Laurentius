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
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.msh.web.gui.dlg.DialogDelete;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminCronTaskDefView")
public class AdminSEDTaskTypeView extends AbstractAdminJSFView<CronTaskDef> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDTaskTypeView.class);


  CronTaskPropertyDef mSelTaksProp;

  /**
     *
     */
  @Override
  public void createEditable() {
    CronTaskDef ecj = new CronTaskDef();

    String type = "type_%03d";
    int i = 1;
    
    ecj.setJndi("java:global[/application name]/module name/enterprise bean name[/interface name]");
    ecj.setType(String.format(type, i));
    ecj.setName("");
    ecj.setDescription("Enter JNDI and refresh data from EJB task!");

    setNew(ecj);
  }

   @Override
  public boolean validateData() {
    
    return true;
  }
  /**
     *
     */
  public void refreshDataFromEJB() {
    if (getEditable() == null || getEditable().getJndi() == null
        || getEditable().getJndi().isEmpty()) {
      return;
    }
/*
    try {
      TaskExecutionInterface tproc = InitialContext.doLookup(getEditable().getJndi());
      getEditable().setDescription(tproc.getTaskDefinition().getDescription());
      getEditable().setName(tproc.getTaskDefinition().getName());
      getEditable().setType(tproc.getTaskDefinition().getType());
      getEditable().getCronTaskProperties().clear();
      if (!tproc.getTaskDefinition().getCronTaskDefProperties().isEmpty()) {
        getEditable().getCronTaskDefProperties().addAll(
            tproc.getTaskDefinition().getCronTaskDefProperties());
      }
    } catch (NamingException ex) {

    }*/

  }

  /**
     *
     */
  @Override
  public void removeSelected() {
    if (getSelected() != null) {
      //mdbLookups.removeCronTaskDef(getSelected());
      setSelected(null);
    }

  }

  /**
     *
     */
  @Override
  public boolean persistEditable() {
    CronTaskDef ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      //mdbLookups.addCronTaskDef(ecj);
      bsuc = true;
    }
    return bsuc;
  }

  /**
     *
     */
  @Override
  public boolean updateEditable() {
    CronTaskDef ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      //mdbLookups.updateCronTaskDef(ecj);
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<CronTaskDef> getList() {
    long l = LOG.logStart();
    //List<CronTaskDef> lst = mdbLookups.getCronTaskDefs();
    //LOG.logEnd(l, lst != null ? lst.size() : "null");
    //return lst;
    return null;
  }



}
