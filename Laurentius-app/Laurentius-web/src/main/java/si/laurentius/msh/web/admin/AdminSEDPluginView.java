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
import java.util.Objects;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.ActionEvent;
import org.primefaces.context.RequestContext;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.ReceptionAwareness;
import si.laurentius.msh.pmode.Security;
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.msh.web.gui.entities.PluginTreeItem;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.def.DefaultInitData;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.plugin.eventlistener.OutMailEventListenerDef;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.processor.InMailProcessorDef;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("adminSEDPluginView")
public class AdminSEDPluginView extends AbstractAdminJSFView<Plugin> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDPluginView.class);
  private static final String TREE_ITEM_PMODE="PMode";
  private static final String TREE_ITEM_PMODE_SERVICE="Service";
  private static final String TREE_ITEM_PMODE_PARTY="Party";
  private static final String TREE_ITEM_PMODE_SECURITY="Security";
  private static final String TREE_ITEM_PMODE_RA="ReceptionAwareness";
  private static final String TREE_ITEM_CRON_TASK="CronTask";
  private static final String TREE_ITEM_INTERCEPTOR="Interceptor";
  private static final String TREE_ITEM_PROCESSOR="InitPluginFolder";
   
  private static final String INIT_PLUGIN_FOLDER="InitPluginFolder";

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPluginManager;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  private PModeInterface mPModeManager;

  Plugin selectedViewPlugin;
  boolean adminView = false;

  private TreeNode[] selectedPluginItems;
  TreeNode rootPluginItems = null;

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

  public void initializeSelectedPluginDataItems() {
    if (rootPluginItems != null) {
      // pmode/plugin
      for (TreeNode pType : rootPluginItems.getChildren()) {
        // type
        for (TreeNode itemType : pType.getChildren()) {
          itemType.getChildren().clear();
        }
        pType.getChildren().clear();
      }
      rootPluginItems.getChildren().clear();
    } else {
      rootPluginItems = new CheckboxTreeNode(new PluginTreeItem("PluginItems",
              null, "PluginItems"), null);
      rootPluginItems.setExpanded(true);
    }

    Plugin slct = getSelected();
    if (slct != null && slct.getDefaultInitData() != null) {
      DefaultInitData did = slct.getDefaultInitData();
      if (did.getPModeData() != null) {
        // add services
        TreeNode pmodeItems = new CheckboxTreeNode(new PluginTreeItem(
                "PModeItems", null, "PModeItems"), rootPluginItems);
        pmodeItems.setExpanded(true);
        if (did.getPModeData().getServices() != null) {
          TreeNode pluginTypeItem = new CheckboxTreeNode(new PluginTreeItem(
                  "Services", null, "Services"), pmodeItems);
          pluginTypeItem.setExpanded(true);

          did.getPModeData().getServices().
                  forEach((s) -> {
                    TreeNode item = new CheckboxTreeNode(new PluginTreeItem(s.
                            getId(), s, TREE_ITEM_PMODE_SERVICE), pluginTypeItem);
                  });
        }
        
        if (did.getPModeData().getPartyIdentitySets() != null) {
          TreeNode pluginTypeItem = new CheckboxTreeNode(new PluginTreeItem(
                  "Parties", null, "Parties"), pmodeItems);
          pluginTypeItem.setExpanded(true);

          did.getPModeData().getPartyIdentitySets().
                  forEach((s) -> {
                    TreeNode item = new CheckboxTreeNode(new PluginTreeItem(s.
                            getId(), s, TREE_ITEM_PMODE_PARTY), pluginTypeItem);
                  });
        }

        if (did.getPModeData().getSecurities() != null) {
          TreeNode pluginTypeItem = new CheckboxTreeNode(new PluginTreeItem(
                  "Securities", null, "Securities"), pmodeItems);
          pluginTypeItem.setExpanded(true);
          did.getPModeData().getSecurities().
                  forEach((s) -> {
                    TreeNode item = new CheckboxTreeNode(new PluginTreeItem(s.
                            getId(), s, TREE_ITEM_PMODE_SECURITY), pluginTypeItem);
                  });
        }

        if (did.getPModeData().getReceptionAwarenesses() != null) {
          TreeNode pluginTypeItem = new CheckboxTreeNode(new PluginTreeItem(
                  "ReceptionAwarenesses", null, "ReceptionAwarenesses"),
                  pmodeItems);
          pluginTypeItem.setExpanded(true);
          did.getPModeData().getReceptionAwarenesses().
                  forEach((s) -> {
                    TreeNode item = new CheckboxTreeNode(new PluginTreeItem(s.
                            getId(), s, TREE_ITEM_PMODE_RA), pluginTypeItem);
                  });
        }

        if (did.getPModeData().getPModes() != null) {
          TreeNode pluginTypeItem = new CheckboxTreeNode(new PluginTreeItem(
                  "PModes", null, "PModes"), pmodeItems);
          pluginTypeItem.setExpanded(true);
          did.getPModeData().getPModes().
                  forEach((s) -> {
                    TreeNode item = new CheckboxTreeNode(new PluginTreeItem(s.
                            getId(), s, TREE_ITEM_PMODE), pluginTypeItem);
                  });
        }
      }

      if (did.getPluginData() != null) {
        // add services
        TreeNode pluginItems = new CheckboxTreeNode(new PluginTreeItem(
                "PluginItems", null, "PluginItems"), rootPluginItems);
        pluginItems.setExpanded(true);
        if (did.getPluginData().getSEDInterceptors() != null) {
          TreeNode pluginTypeItem = new CheckboxTreeNode(new PluginTreeItem(
                  "Interceptors", null, "Interceptors"), pluginItems);
          pluginTypeItem.setExpanded(true);
          did.getPluginData().getSEDInterceptors().
                  forEach((s) -> {
                    TreeNode item = new CheckboxTreeNode(new PluginTreeItem(s.
                            getName(), s, TREE_ITEM_INTERCEPTOR), pluginTypeItem);
                  });
        }
        if (did.getPluginData().getSEDCronJobs() != null) {
          TreeNode pluginTypeItem = new CheckboxTreeNode(new PluginTreeItem(
                  "CronJobs", null, "CronJobs"), pluginItems);
          pluginTypeItem.setExpanded(true);
          did.getPluginData().getSEDCronJobs().
                  forEach((s) -> {
                    TreeNode item = new CheckboxTreeNode(new PluginTreeItem(s.
                            getName(), s, TREE_ITEM_CRON_TASK), pluginTypeItem);
                  });
        }
        
        if (did.getPluginData().getSEDProcessors() != null) {
          TreeNode pluginTypeItem = new CheckboxTreeNode(new PluginTreeItem(
                  "Processors", null, "Processors"), pluginItems);
          pluginTypeItem.setExpanded(true);
          did.getPluginData().getSEDProcessors().
                  forEach((s) -> {
                    TreeNode item = new CheckboxTreeNode(new PluginTreeItem(s.
                            getName(), s, TREE_ITEM_PROCESSOR), pluginTypeItem);
                  });
        }
      }
      
     
    }
  }

  public TreeNode getRootPluginItems() {
    return rootPluginItems;
  }

  public TreeNode[] getSelectedPluginItems() {
    return selectedPluginItems;
  }

  public void setSelectedPluginItems(TreeNode[] selectedPluginItems) {
    this.selectedPluginItems = selectedPluginItems;
  }

  public void installSelectedPluginItems() {

    if (selectedPluginItems == null) {
      String msg = "No selected plugin items to install";
      LOG.logWarn(msg, null);
      addError(msg);
      return;
    }


    for (TreeNode tn : selectedPluginItems) {
      PluginTreeItem item = (PluginTreeItem) tn.getData();
      if (item.getPluginItem() == null) {
        continue;
      }

      if (item.getPluginItem() instanceof Service) {
        installService((Service) item.getPluginItem());
      }
      if (item.getPluginItem() instanceof Security) {
        installSecurity((Security) item.getPluginItem());
      }

      if (item.getPluginItem() instanceof ReceptionAwareness) {
        installReceptionAwareness((ReceptionAwareness) item.getPluginItem());
      }

      // add parties
      if (item.getPluginItem() instanceof PartyIdentitySet) {
        installPartyIdentitySet((PartyIdentitySet) item.getPluginItem());
      }

      if (item.getPluginItem() instanceof PMode) {
        installPMode((PMode) item.getPluginItem());
      }

      if (item.getPluginItem() instanceof SEDInterceptor) {
        installSEDInterceptor((SEDInterceptor) item.getPluginItem());
      }

      if (item.getPluginItem() instanceof SEDCronJob) {
        installSEDCronJob((SEDCronJob) item.getPluginItem());

      }


    }
    // submit message to dialog  to close 
    RequestContext.getCurrentInstance().addCallbackParam("saved", true);

  }

  private boolean installService(Service s) {
    boolean bAdd = false;
    if (mPModeManager.getServiceById(s.getId()) == null) {
      mPModeManager.addService(s);
      bAdd = true;
    }
    return bAdd;
  }

  private boolean installSecurity(Security sec) {
    boolean bAdd = false;
    if (mPModeManager.getSecurityById(sec.getId()) == null) {
      mPModeManager.addSecurity(sec);
      bAdd = true;
    }
    return bAdd;
  }

  private boolean installReceptionAwareness(ReceptionAwareness rc) {
    boolean bAdd = false;
    if (mPModeManager.getReceptionAwarenessById(rc.getId()) == null) {
      mPModeManager.addReceptionAwareness(rc);
      bAdd = true;
    }
    return bAdd;
  }

  private boolean installPartyIdentitySet(PartyIdentitySet ps) {
    boolean bAdd = false;
    if (mPModeManager.getPartyIdentitySetById(ps.getId()) == null) {
      mPModeManager.addPartyIdentitySet(ps);
      bAdd = true;
    }
    return bAdd;
  }

  private boolean installSEDInterceptor(SEDInterceptor si) {
    boolean bAdd = false;

    if (mdbLookups.getSEDInterceptorByName(si.getName()) == null) {
      si.setId(null);
      si.getSEDInterceptorRules().
              forEach((sr) -> {
                sr.setId(null);
              });

      si.getSEDInterceptorInstance().getSEDInterceptorProperties().
              forEach((sr) -> {
                sr.setId(null);
              });

      mdbLookups.addSEDInterceptor(si);
      bAdd = true;
    }
    return bAdd;
  }

  private boolean installSEDCronJob(SEDCronJob cj) {
    boolean bAdd = false;

    if (mdbLookups.getSEDCronJobByName(cj.getName()) == null) {

      cj.setId(null);
      cj.getSEDTasks().forEach((tsk) -> {
        tsk.setId(null);
        tsk.getSEDTaskProperties().
                forEach((sr) -> {
                  sr.setId(null);
                });

      });
      mdbLookups.addSEDCronJob(cj);
      bAdd = true;
    }
    return bAdd;
  }

  private boolean installPMode(PMode p) {
    boolean bAdd = false;

    if (mPModeManager.getPModeById(p.getId()) == null) {
      Service sv = mPModeManager.getServiceById(p.getServiceIdRef());
      if (sv == null) {
        LOG.formatedWarning(
                "Could not add PMode %s, because service %s is not registred. Bad plugin init data?",
                p.getId(), p.getServiceIdRef());
        return bAdd;
      }
      mPModeManager.addPMode(p);
      bAdd = true;
    }

    return bAdd;
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
