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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.xml.ws.WebServiceContext;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.SEDNetworkUtilsInterface;
import si.laurentius.msh.web.abst.AbstractJSFView;
import si.laurentius.property.SEDProperty;

/**
 *
 * @author Jože Rihtaršič
 */
@ApplicationScoped
@Named("ApplicationData")
public class ApplicationData extends AbstractJSFView {

  @Resource
  WebServiceContext context;

  @EJB(mappedName = SEDJNDI.JNDI_DBSETTINGS)
  private DBSettingsInterface mdbSettings;
  
   @EJB(mappedName = SEDJNDI.JNDI_NETWORK)
  private SEDNetworkUtilsInterface mdNetUtils;
    

  public String getExportDataFolder() {
    return SEDSystemProperties.getInitFolder().getAbsolutePath();
  }

  /**
   *
   * @return
   */
  public String getBuildVersion() {
    String strBuildVer = "";
    Manifest p;
    File manifestFile = null;
    String home = FacesContext.getCurrentInstance().getExternalContext().
            getRealPath("/");
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
    return "@" + SEDSystemProperties.getLocalDomain();
  }
  
  public List<SEDProperty> getSEDProperties(){
    return mdbSettings.getSEDProperties();
  }

  

  /**
   *
   * @return
   */
  public String getStorageFolder() {
    return SEDSystemProperties.getStorageFolder().getAbsolutePath();

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
   */
  public void refreshMainPanel() {
    FacesContext facesContext = facesContext();
    String refreshpage = "MainPanel";
    ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
    UIViewRoot viewroot = viewHandler.createView(facesContext, refreshpage);
    viewroot.setViewId(refreshpage);
    facesContext.setViewRoot(viewroot);
  }
  
  public boolean isNetworkConnected(){
    return mdNetUtils.isConnectedToNetwork();
  }
  
  public boolean isInternetConnected(){
    return mdNetUtils.isConnectedToInternet();
  }

}
