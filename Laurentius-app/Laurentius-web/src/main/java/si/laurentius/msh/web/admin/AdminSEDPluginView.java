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
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTask;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.interceptor.SEDInterceptorRule;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PModePartyInfo;
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
@ManagedBean(name = "adminSEDPluginView")
public class AdminSEDPluginView extends AbstractAdminJSFView<Plugin> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDPluginView.class);

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
                            getId(), s, "Service"), pluginTypeItem);
                  });
        }

        if (did.getPModeData().getSecurities() != null) {
          TreeNode pluginTypeItem = new CheckboxTreeNode(new PluginTreeItem(
                  "Securities", null, "Securities"), pmodeItems);
          pluginTypeItem.setExpanded(true);
          did.getPModeData().getSecurities().
                  forEach((s) -> {
                    TreeNode item = new CheckboxTreeNode(new PluginTreeItem(s.
                            getId(), s, "Security"), pluginTypeItem);
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
                            getId(), s, "ReceptionAwareness"), pluginTypeItem);
                  });
        }

        if (did.getPModeData().getPModes() != null) {
          TreeNode pluginTypeItem = new CheckboxTreeNode(new PluginTreeItem(
                  "PModes", null, "PModes"), pmodeItems);
          pluginTypeItem.setExpanded(true);
          did.getPModeData().getPModes().
                  forEach((s) -> {
                    TreeNode item = new CheckboxTreeNode(new PluginTreeItem(s.
                            getId(), s, "PModes"), pluginTypeItem);
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
                            getName(), s, "Interceptor"), pluginTypeItem);
                  });
        }
        if (did.getPluginData().getSEDCronJobs() != null) {
          TreeNode pluginTypeItem = new CheckboxTreeNode(new PluginTreeItem(
                  "CronJobs", null, "CronJobs"), pluginItems);
          pluginTypeItem.setExpanded(true);
          did.getPluginData().getSEDCronJobs().
                  forEach((s) -> {
                    TreeNode item = new CheckboxTreeNode(new PluginTreeItem(s.
                            getName(), s, "CronJob"), pluginTypeItem);
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
      LOG.logWarn("No selected plugin items to install!", null);
      return;
    }
    List<PartyIdentitySet> lstLPI = new ArrayList<>();
    mPModeManager.getPartyIdentitySets().stream().
            filter((pi) -> (pi.getIsLocalIdentity())).
            forEachOrdered((pi) -> {
              lstLPI.add(pi);
            });

    for (TreeNode tn : selectedPluginItems) {
      PluginTreeItem item = (PluginTreeItem) tn.getData();
      if (item.getPluginItem() == null) {
        continue;
      }

      if (item.getPluginItem() instanceof Service) {
        Service s = (Service) item.getPluginItem();
        if (mPModeManager.getServiceById(s.getId()) == null) {
          mPModeManager.addService(s);
        }
      }
      if (item.getPluginItem() instanceof Security) {
        Security sec = (Security) item.getPluginItem();
        if (mPModeManager.getSecurityById(sec.getId()) == null) {
          mPModeManager.addSecurity(sec);
        }
      }

      if (item.getPluginItem() instanceof ReceptionAwareness) {
        ReceptionAwareness rc = (ReceptionAwareness) item.getPluginItem();

        if (mPModeManager.getReceptionAwarenessById(rc.getId()) == null) {
          mPModeManager.addReceptionAwareness(rc);
        }
      }

      if (item.getPluginItem() instanceof PMode) {
        PMode p = (PMode) item.getPluginItem();

        if (mPModeManager.getPModeById(p.getId()) == null) {
          Service sv = mPModeManager.getServiceById(p.getServiceIdRef());
          if (sv == null) {
            LOG.formatedWarning(
                    "Could not add PMode %s, because service %s is not registred. Bad plugin init data?",
                    p.getId(), p.getServiceIdRef());
            continue;
          }

          PMode pClone = XMLUtils.deepCopyJAXB(p);

          if (!lstLPI.isEmpty()) {
            pClone.setLocalPartyInfo(new PModePartyInfo());
            pClone.getLocalPartyInfo().setPartyIdentitySetIdRef(lstLPI.get(0).
                    getId());
            if (!lstLPI.get(0).getTransportProtocols().isEmpty()) {
              pClone.getLocalPartyInfo().setPartyDefTransportIdRef(
                      lstLPI.get(0).getTransportProtocols().get(0).getId());

              if (sv.getExecutor() != null) {
                pClone.getLocalPartyInfo().getRoles().add(sv.getExecutor().
                        getRole());
              }
              if (sv.getInitiator() != null) {
                pClone.getLocalPartyInfo().getRoles().add(sv.getInitiator().
                        getRole());
              }
            }

            PMode.ExchangeParties eps = new PMode.ExchangeParties();
            for (PartyIdentitySet ps : lstLPI) {
              PMode.ExchangeParties.PartyInfo pi = new PMode.ExchangeParties.PartyInfo();
              pi.setPartyIdentitySetIdRef(ps.getId());
              if (!ps.getTransportProtocols().isEmpty()) {
                pi.setPartyDefTransportIdRef(
                        ps.getTransportProtocols().get(0).
                                getId());
              }
              if (sv.getExecutor() != null) {
                pi.getRoles().add(sv.getExecutor().getRole());
              }
              if (sv.getInitiator() != null) {
                pi.getRoles().add(sv.getInitiator().getRole());
              }

              eps.getPartyInfos().add(pi);

            }
            pClone.setExchangeParties(eps);
          }
          mPModeManager.addPMode(pClone);
        }

      }

      if (item.getPluginItem() instanceof SEDInterceptor) {
        SEDInterceptor si = (SEDInterceptor) item.getPluginItem();

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
        }
      }

      if (item.getPluginItem() instanceof SEDCronJob) {
        SEDCronJob cj = (SEDCronJob) item.getPluginItem();

        cj.setId(null);
        cj.getSEDTasks().forEach((tsk) -> {
          tsk.setId(null);
          tsk.getSEDTaskProperties().
                  forEach((sr) -> {
                    sr.setId(null);
                  });

        });
        mdbLookups.addSEDCronJob(cj);

      }

    }
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
