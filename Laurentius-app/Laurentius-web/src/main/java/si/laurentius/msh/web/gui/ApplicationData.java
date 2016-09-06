package si.laurentius.msh.web.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.application.ViewHandler;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.xml.ws.WebServiceContext;
import org.primefaces.event.RowEditEvent;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.msh.web.abst.AbstractJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@ApplicationScoped
@ManagedBean(name = "ApplicationData")
public class ApplicationData extends AbstractJSFView {

  @Resource
  WebServiceContext context;

  @EJB(mappedName = SEDJNDI.JNDI_DBSETTINGS)
  private DBSettingsInterface mdbSettings;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface msedLookups;

  /**
     *
     */
  public void exportLookupsWithNoPasswords() {
    msedLookups.exportLookups(new File(System.getProperty(SEDSystemProperties.SYS_PROP_HOME_DIR)),
        false);
  }

  /**
     *
     */
  public void exportLookupsWithPasswords() {
    msedLookups.exportLookups(new File(System.getProperty(SEDSystemProperties.SYS_PROP_HOME_DIR)),
        true);
  }

  /**
   *
   * @return
   */
  public String getBuildVersion() {
    String strBuildVer = "";
    Manifest p;
    File manifestFile = null;
    String home = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
    manifestFile = new File(home, "META-INF/MANIFEST.MF");
    try (FileInputStream fis = new FileInputStream(manifestFile)) {
      p = new Manifest();
      p.read(fis);
      Attributes a = p.getMainAttributes();
      strBuildVer = a.getValue("Implementation-Build");
    } catch (IOException ex) {

    }
    return strBuildVer;
  }

  /**
   *
   * @return
   */
  public String getDomain() {
    return "@" + mdbSettings.getDomain();
  }

  /**
   *
   * @return
   */
  public String getHomeFolder() {
    return System.getProperty(SEDSystemProperties.SYS_PROP_HOME_DIR);
  }

  /**
   *
   * @return
   */
  public String getPModeFileName() {
    return mdbSettings.getPModeFileName();
  }

  /**
   *
   * @return
   */
  public List<String> getPlugins() {
    List<String> plLSt = new ArrayList<>();
    File fldPlugins =
        new File(System.getProperty(SEDSystemProperties.SYS_PROP_HOME_DIR) + File.separator
            + SEDSystemProperties.SYS_PROP_FOLDER_PLUGINS_DEF);
    if (fldPlugins.exists() && fldPlugins.isDirectory()) {
      for (File f : fldPlugins.listFiles((File dir, String name) -> name.toLowerCase().endsWith(
          ".jar"))) {
        plLSt.add(f.getName());
      }
    }
    return plLSt;
  }

  /**
   *
   * @return
   */
  public String getPluginsFolder() {
    return SEDSystemProperties.SYS_PROP_FOLDER_PLUGINS_DEF;

  }

  /**
   *
   * @return
   */
  public List<String> getSEDPropertyKeys() {

    Set<String> s = mdbSettings.getProperties().stringPropertyNames();
    List<String> lst = new ArrayList<>(s);
    Collections.sort(lst);
    return lst;

  }

  /**
   *
   * @param strVal
   * @return
   */
  public String getSEDPropertyValue(String strVal) {
    return mdbSettings.getProperties().getProperty(strVal);

  }

  /**
   *
   * @return
   */
  public String getSecurityFileName() {
    return SEDSystemProperties.SYS_PROP_CERT_DEF;
  }

  /**
   *
   * @return
   */
  public String getStorageFolder() {
    return SEDSystemProperties.SYS_PROP_FOLDER_STORAGE_DEF;

  }

  /**
   *
   * @return
   */
  public List<String> getSystemPropertyKeys() {
    Set<String> s = System.getProperties().stringPropertyNames();
    List<String> lst = new ArrayList<>(s);
    Collections.sort(lst);
    return lst;

  }

  /**
   *
   * @param strVal
   * @return
   */
  public String getSystemPropertyValue(String strVal) {
    return System.getProperty(strVal);

  }

  /**
   *
   * @param event
   */
  public void onCancel(RowEditEvent event) {
    // FacesMessage msg = new FacesMessage("Item Cancelled");
    // FacesContext.getCurrentInstance().addMessage(null, msg);
    // orderList.remove((OrderBean) event.getObject());
  }

  /**
   *
   * @param event
   */
  public void onEdit(RowEditEvent event) {
    // FacesMessage msg = new FacesMessage("Item Edited",((OrderBean) event.getObject()).getItem());
    // FacesContext.getCurrentInstance().addMessage(null, msg);
  }

  /**
     *
     */
  public void refreshMainPanel() {
    FacesContext facesContext = facesContext();
    String refreshpage = "MainPanel";
    ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
    UIViewRoot viewroot = viewHandler.createView(facesContext, refreshpage);
    viewroot.setViewId(refreshpage);
    facesContext.setViewRoot(viewroot);
  }

}
