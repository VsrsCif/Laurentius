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
import javax.naming.InitialContext;
import javax.naming.NamingException;
import si.laurentius.cron.SEDTaskType;
import si.laurentius.cron.SEDTaskTypeProperty;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.TaskExecutionInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDTaskTypeView")
public class AdminSEDTaskTypeView extends AbstractAdminJSFView<SEDTaskType> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDTaskTypeView.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  SEDTaskTypeProperty mSelTaksProp;

  /**
     *
     */
  @Override
  public void createEditable() {
    SEDTaskType ecj = new SEDTaskType();

    String type = "type_%03d";
    int i = 1;
    while (mdbLookups.getSEDTaskTypeByType(String.format(type, i)) != null) {
      i++;
    }
    ecj.setJndi("java:global[/application name]/module name/enterprise bean name[/interface name]");
    ecj.setType(String.format(type, i));
    ecj.setName("");
    ecj.setDescription("Enter JNDI and refresh data from EJB task!");

    setNew(ecj);
  }

  /**
     *
     */
  public void refreshDataFromEJB() {
    if (getEditable() == null || getEditable().getJndi() == null
        || getEditable().getJndi().isEmpty()) {
      return;
    }

    try {
      TaskExecutionInterface tproc = InitialContext.doLookup(getEditable().getJndi());
      getEditable().setDescription(tproc.getTaskDefinition().getDescription());
      getEditable().setName(tproc.getTaskDefinition().getName());
      getEditable().setType(tproc.getTaskDefinition().getType());
      getEditable().getSEDTaskTypeProperties().clear();
      if (!tproc.getTaskDefinition().getSEDTaskTypeProperties().isEmpty()) {
        getEditable().getSEDTaskTypeProperties().addAll(
            tproc.getTaskDefinition().getSEDTaskTypeProperties());
      }
    } catch (NamingException ex) {

    }

  }

  /**
     *
     */
  @Override
  public void removeSelected() {
    if (getSelected() != null) {
      mdbLookups.removeSEDTaskType(getSelected());
      setSelected(null);
    }

  }

  /**
     *
     */
  @Override
  public void persistEditable() {
    SEDTaskType ecj = getEditable();
    if (ecj != null) {
      mdbLookups.addSEDTaskType(ecj);
    }
  }

  /**
     *
     */
  @Override
  public void updateEditable() {
    SEDTaskType ecj = getEditable();
    if (ecj != null) {
      mdbLookups.updateSEDTaskType(ecj);
    }
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDTaskType> getList() {
    long l = LOG.logStart();
    List<SEDTaskType> lst = mdbLookups.getSEDTaskTypes();
    LOG.logEnd(l, lst != null ? lst.size() : "null");
    return lst;
  }

  /**
   *
   * @param key
   * @param name
   * @param mandatory
   * @return
   */
  public SEDTaskTypeProperty createTypeProperty(String key, String name, boolean mandatory) {
    SEDTaskTypeProperty sp = new SEDTaskTypeProperty();
    sp.setKey(key);
    sp.setDescription(name);
    sp.setMandatory(mandatory);
    return sp;
  }

  /**
     *
     */
  public void addTypeProperty() {

    if (getEditable() != null) {

      SEDTaskTypeProperty sp = createTypeProperty("newProp", "", true);
      getEditable().getSEDTaskTypeProperties().add(sp);
      setSelectedTaskProperty(sp);
    }

  }

  /**
     *
     */
  public void removeSelectedTypeProperty() {
    if (getEditable() != null && getSelectedTaskProperty() != null) {
      getEditable().getSEDTaskTypeProperties().remove(getSelectedTaskProperty());
    }
  }

  /**
   *
   * @return
   */
  public SEDTaskTypeProperty getSelectedTaskProperty() {
    return mSelTaksProp;
  }

  /**
   *
   * @param prop
   */
  public void setSelectedTaskProperty(SEDTaskTypeProperty prop) {
    this.mSelTaksProp = prop;
  }

}
