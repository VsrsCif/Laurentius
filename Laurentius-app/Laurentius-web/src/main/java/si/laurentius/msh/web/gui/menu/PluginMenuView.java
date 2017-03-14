/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.gui.menu;

import java.io.Serializable;
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

@ManagedBean(name = "pluginMenuView")
@SessionScoped
public class PluginMenuView implements Serializable {

  private static final SEDLogger LOG = new SEDLogger(PluginMenuView.class);

  @ManagedProperty(value = "#{mainWindow}")
  private MainWindow mainWindow;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPluginManager;

  private TreeNode selectedNode;
  TreeNode mtnRootNode = null;

  public MainWindow getMainWindow() {
    return mainWindow;
  }

  public void setMainWindow(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }

  public TreeNode getRoot() {
    

    return updateMenu();
  }

  public TreeNode getSelectedNode() {
    if (selectedNode == null && mtnRootNode!= null && mtnRootNode.getChildCount()>0) {
      selectedNode = mtnRootNode.getChildren().get(0);
    }
    return selectedNode;
  }

  public void setSelectedNode(TreeNode selectedNode) {
    this.selectedNode = selectedNode;
  }

  public void onSelection() {
    if (selectedNode != null) {
      mainWindow.setCurrentPanel(AppConstant.S_PANEL_PLUGIN);
    }

  }

  public String getSelectedWebContext() {
    if (selectedNode != null) {
      LOG.formatedWarning("get selected web context %s",
              ((MenuItem) selectedNode.getData()).getWebUrl());
      return ((MenuItem) selectedNode.getData()).getWebUrl();

    }
    LOG.formatedWarning("get selected web context null");
    return null;
  }

  private TreeNode updateMenu() {
    if (mtnRootNode == null) {
      mtnRootNode = new DefaultTreeNode(
              new MenuItem("Plugin Menu", "ROOT", ""), null);


      for (Plugin p : mPluginManager.getRegistredPlugins()) {
        if (!Utils.isEmptyString(p.getWebContext()) && p.getMainMenu() != null) {

          TreeNode rootMI = new DefaultTreeNode(new MenuItem(p.getMainMenu().
                  getName(), null, "ui-icon-svg-plugin ui-icon-size-22", p.
                          getWebContext()), mtnRootNode);

          for (si.laurentius.plugin.def.MenuItem pmi : p.getMainMenu().
                  getMenuItems()) {
            TreeNode plugin = new DefaultTreeNode(
                    new MenuItem(pmi.getName(), null,
                            "ui-icon-svg-plugin ui-icon-size-16", String.format(
                                    "%s?page=%s&navigator=false", p.
                                            getWebContext(), pmi.getPageId())),
                    rootMI);
          }
        }
      }
    }

    return mtnRootNode;
  }
}
