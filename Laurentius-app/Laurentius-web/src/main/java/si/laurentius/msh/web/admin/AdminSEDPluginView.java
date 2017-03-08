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
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.plugin.eventlistener.OutMailEventListenerDef;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.processor.InMailProcessorDef;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDPluginView")
public class AdminSEDPluginView extends AbstractAdminJSFView<Plugin> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDPluginView.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPluginManager;

  Plugin selectedViewPlugin;
  boolean adminView = false;
  


  public List<Plugin> getPluginWitGUI() {
    List<Plugin> lst = new ArrayList<>();
    for (Plugin p : mPluginManager.getRegistredPlugins()) {
      if (!Utils.isEmptyString(p.getWebContext())) {
        lst.add(p);
      }
    }
    if (selectedViewPlugin == null && !lst.isEmpty()) {
      selectedViewPlugin = lst.get(0);
    }
    return lst;

  }

  public List<CronTaskDef> getCronTaskListForPlugin(String plugin) {
    return mPluginManager.getCronTasksForPlugin(plugin);

  }

  /**
   *
   */
  @Override
  public void createEditable() {

  }

  @Override
  public boolean validateData() {

    return true;
  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    return false;
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    return false;
  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    return false;
  }

  public List<CronTaskDef> getCurrentCronTasks() {
    Plugin ecj = getSelected();
    if (ecj != null) {
      return ecj.getCronTaskDeves();
    }
    return Collections.emptyList();
  }

  ;
  
  public List<MailInterceptorDef> getCurrentMailInterceptors() {
    Plugin ecj = getSelected();
    if (ecj != null) {
      return ecj.getMailInterceptorDeves();
    }
    return Collections.emptyList();

  }

  ;
  public List<OutMailEventListenerDef> getCurrentOutMailEventListeners() {
    Plugin ecj = getSelected();
    if (ecj != null) {
      return ecj.getOutMailEventListenerDeves();
    }
    return Collections.emptyList();
  }

  ;
  
  public List<InMailProcessorDef> getCurrentInMailProcessors() {
    Plugin ecj = getSelected();
    if (ecj != null) {
      return ecj.getInMailProcessorDeves();
    }
    return Collections.emptyList();

  }

  /**
   *
   * @return
   */
  @Override
  public List<Plugin> getList() {
    return mPluginManager.getRegistredPlugins();
  }

  /**
   *
   * @return
   */
  public String getSelectedWebContext() {
    return selectedViewPlugin != null
            ? selectedViewPlugin.getWebContext()
            : "";
  }

  /**
   * Onn selected view in Plugin widget tool
   *
   * @param event
   */
  public void onSelectedViewPluginAction(ActionEvent event) {
    long l = LOG.logStart();
    if (event != null) {
      Plugin res = (Plugin) event.getComponent().getAttributes().get(
              "pluginItem");
      selectedViewPlugin = res;
    } else {
      selectedViewPlugin = null;
    }
    LOG.logEnd(l);

  }

}
