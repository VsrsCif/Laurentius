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
package si.jrc.msh.plugin.zpp.web;

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
import javax.faces.application.ViewHandler;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import org.primefaces.event.RowEditEvent;
import si.laurentius.commons.SEDSystemProperties;

/**
 *
 * @author Jože Rihtaršič
 */
@ApplicationScoped
@ManagedBean(name = "ZppPluginData")
public class ZppPluginData {

  @Resource
  WebServiceContext context;

  /**
   *
   * @return
   */
  protected ExternalContext externalContext() {
    return facesContext().getExternalContext();
  }

  /**
   *
   * @return
   */
  protected FacesContext facesContext() {
    return FacesContext.getCurrentInstance();
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
  public String getClientIP() {
    return ((HttpServletRequest) externalContext().getRequest()).getRemoteAddr();
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
