/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.gui.menu;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.web.gui.AppConstant;
import si.laurentius.msh.web.gui.MainWindow;
import si.laurentius.plugin.def.Plugin;

@ManagedBean(name = "settingsMenuView")
@SessionScoped
public class SettingsMenuView implements Serializable {

  private static final SEDLogger LOG = new SEDLogger(SettingsMenuView.class);

  @ManagedProperty(value = "#{mainWindow}")
  private MainWindow mainWindow;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPluginManager;

  private TreeNode root;

  private TreeNode selectedNode;

  @PostConstruct
  public void init() {
    root = createMenu();
  }

  public MainWindow getMainWindow() {
    return mainWindow;
  }

  public void setMainWindow(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }

  public TreeNode getRoot() {
    return root;
  }

  public TreeNode getSelectedNode() {
    return selectedNode;
  }

  public void setSelectedNode(TreeNode selectedNode) {
    this.selectedNode = selectedNode;
  }

  public void onSelection() {
    if (selectedNode != null) {
      mainWindow.setCurrentPanel(((MenuItem) selectedNode.getData()).getType());
    }

  }

  public TreeNode createMenu() {
    TreeNode root = new DefaultTreeNode(
            new MenuItem("Settings Menu", "ROOT", ""), null);

    TreeNode custom = new DefaultTreeNode(new MenuItem("Custom",
            AppConstant.S_PANEL_SETT_CUSTOM,
            "ui-icon-svg-settings ui-icon-size-22"), root);
    TreeNode sedbox = new DefaultTreeNode(new MenuItem("SED-Boxes",
            AppConstant.S_PANEL_ADMIN_EBOXES, "ui-icon-svg-box ui-icon-size-22"),
            root);
    TreeNode users = new DefaultTreeNode(new MenuItem("Users",
            AppConstant.S_PANEL_ADMIN_USERS, "ui-icon-svg-users ui-icon-size-22"),
            root);
    
     TreeNode appl = new DefaultTreeNode(new MenuItem("Applications",
            AppConstant.S_PANEL_ADMIN_APPL, "ui-icon-svg-cms ui-icon-size-22"),
            root);

    //-certicates
    TreeNode certs = new DefaultTreeNode(new MenuItem("Certificates",
            AppConstant.S_PANEL_SETT_CERTS,
            "ui-icon-svg-certificate ui-icon-size-22"), root);
    certs.setExpanded(true);
    TreeNode keystore = new DefaultTreeNode(new MenuItem("Keystore",
            AppConstant.S_PANEL_SETT_CERTS,
            "ui-icon-svg-key ui-icon-size-22"), certs);
    TreeNode rootCA = new DefaultTreeNode(new MenuItem("RootCA",
            AppConstant.S_PANEL_SETT_CERT_ROOT_CA,
            "ui-icon-svg-certificate ui-icon-size-22"), certs);
    TreeNode CRL = new DefaultTreeNode(new MenuItem("CRL",
            AppConstant.S_PANEL_SETT_CERT_CRL,
            "ui-icon-svg-crl ui-icon-size-22"), certs);

    //- PMODES
    TreeNode pmodeSettings = new DefaultTreeNode(new MenuItem("SettingsPMode",
            AppConstant.S_PANEL_SETT_PMODE, "ui-icon-svg-pmode ui-icon-size-22"),
            root);
    pmodeSettings.setExpanded(true);
    TreeNode pmodesrv = new DefaultTreeNode(new MenuItem("PModeServiceDefinitions",
            AppConstant.S_PANEL_SETT_PMODE_SERVICES,
            "ui-icon-svg-service ui-icon-size-22"),
            pmodeSettings);
    TreeNode pmodeparties = new DefaultTreeNode(new MenuItem("Parties",
            AppConstant.S_PANEL_SETT_PMODE_PARTIES,
            "ui-icon-svg-party ui-icon-size-22"),
            pmodeSettings);
    TreeNode pmodesec = new DefaultTreeNode(new MenuItem("SecurityPolicies",
            AppConstant.S_PANEL_SETT_PMODE_SECURITIES,
            "ui-icon-svg-security ui-icon-size-22"),
            pmodeSettings);
    TreeNode spRA = new DefaultTreeNode(new MenuItem(
            "ReceptionAwarenessPatterns",
            AppConstant.S_PANEL_SETT_PMODE_AS4_RA,
            "ui-icon-svg-reliability ui-icon-size-22"),
            pmodeSettings);
    TreeNode pmode = new DefaultTreeNode(new MenuItem("PModes",
            AppConstant.S_PANEL_SETT_PMODE, "ui-icon-svg-pmode ui-icon-size-22"),
            pmodeSettings);

    //- Plugins
    TreeNode addons = new DefaultTreeNode(new MenuItem("Plugins",
            AppConstant.S_PANEL_ADMIN_PLUGIN,
            "ui-icon-svg-plugin ui-icon-size-22"), root);
    addons.setExpanded(true);
    TreeNode interceptors = new DefaultTreeNode(new MenuItem("Interceptors",
            AppConstant.S_PANEL_INTERCEPTOR,
            "ui-icon-svg-interceptor ui-icon-size-22"), addons);
    interceptors.setExpanded(true);
    
    TreeNode inmail = new DefaultTreeNode(new MenuItem("InMailRules",
            AppConstant.S_PANEL_INMAIL_PROCESS,
            "ui-icon-svg-process ui-icon-size-22"), addons);
     inmail.setExpanded(true);
    TreeNode crontask = new DefaultTreeNode(new MenuItem("Scheduler",
            AppConstant.S_PANEL_ADMIN_CRON,
            "ui-icon-svg-cron-exec ui-icon-size-22"), addons);

    for (Plugin p : mPluginManager.getRegistredPlugins()) {
      if (!Utils.isEmptyString(p.getWebContext()) && p.getProcessMenu() != null) {
     
        for (si.laurentius.plugin.def.MenuItem pmi : p.getProcessMenu().
                getMenuItems()) {
          TreeNode plugin = new DefaultTreeNode(
                  new MenuItem(pmi.getName(), AppConstant.S_SETTINGS_PLUGIN,
                          "ui-icon-svg-plugin ui-icon-size-16", String.format(
                                  "%s?page=%s&navigator=false", p.
                                          getWebContext(), pmi.getPageId())),
                  inmail);
        }
        
       
      }
    }

    return root;
  }

  public String getSelectedWebContext() {
    if (selectedNode != null) {
      return ((MenuItem) selectedNode.getData()).getWebUrl();

    }
    return null;
  }
}
