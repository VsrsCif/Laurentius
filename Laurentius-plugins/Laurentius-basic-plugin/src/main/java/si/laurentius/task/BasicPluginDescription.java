/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.task;

import java.io.File;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import si.laurentius.commons.SEDGUIConstants;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.plg.db.IMPDBInterface;
import si.laurentius.plg.web.AppConstant;
import si.laurentius.plugin.def.MenuItem;
import si.laurentius.plugin.interfaces.AbstractPluginDescription;
import si.laurentius.plugin.interfaces.PluginDescriptionInterface;
import si.laurentius.plugin.interfaces.exception.PluginException;
import si.laurentius.process.ProcessExecute;
import si.laurentius.process.ProcessExport;
import si.laurentius.process.ProcessXSLT;

/**
 *
 * @author Jože Rihtaršič
 */
@Singleton
@Startup
@Local(PluginDescriptionInterface.class)
public class BasicPluginDescription extends AbstractPluginDescription {
  
  @EJB
  private IMPDBInterface mDB;

  private static final SEDLogger LOG = new SEDLogger(
          BasicPluginDescription.class);

  /**
   *
   * @return
   */
  @Override
  public String getDesc() {
    return "Basic  plugin descriptor";
  }

  /**
   *
   * @return
   */
  @Override
  public String getName() {
    return "Basic tasks plugin";
  }

  @Override
  public MenuItem getProcessMenu() {
    MenuItem miRoot = super.getProcessMenu();
    miRoot.setName(getName());

    MenuItem mi = new MenuItem();
    mi.setName("Export");
    mi.setPageId(AppConstant.S_PANEL_IMP_EXPORT);

    MenuItem miExc = new MenuItem();
    miExc.setName("Execute");
    miExc.setPageId(AppConstant.S_PANEL_IMP_EXECUTE);

    MenuItem miXSLT = new MenuItem();
    miXSLT.setName("XSLT");
    miXSLT.setPageId(AppConstant.S_PANEL_IMP_XSLT);

    miRoot.getMenuItems().add(mi);
    miRoot.getMenuItems().add(miExc);
    miRoot.getMenuItems().add(miXSLT);

    return miRoot;
  }

  /**
   *
   * @return
   */
  @Override
  public String getType() {
    return "BasicTaskPlugin";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  /**
   *
   * @return
   */
  @Override
  public List<String> getWebPageRoles() {
    return Collections.singletonList(SEDGUIConstants.ROLE_ADMIN);
  }

  /**
   *
   * @return
   */
  @Override
  public String getWebUrlContext() {
    return "/laurentius-web/basic-plugin";
  }

  @PostConstruct
  private void postConstruct() {
    try {
      // and log further application specific info
      registerPluginComponentInterface(TaskArchive.class);
      registerPluginComponentInterface(TaskBackup.class);
      registerPluginComponentInterface(TaskFileSubmitter.class);
      registerPluginComponentInterface(TaskEmailInboxMailReport.class);
      registerPluginComponentInterface(TaskEmailStatusReport.class);

      registerPluginComponentInterface(ProcessExport.class);
      registerPluginComponentInterface(ProcessExecute.class);
      registerPluginComponentInterface(ProcessXSLT.class);

      // register plugin
      registerPlugin();

    } catch (PluginException ex) {
      LOG.logError("Error occured while registering plugin: " + ex.
              getMessage(), ex);
    }
    
    
  }
  
 

  /**
   *
   * @param initFolder
   * @param savePasswds
   */
  @Override
  public void exportData(File initFolder, boolean  savePasswds) {
    
    mDB.exportInitData(initFolder);
    
  }

}
