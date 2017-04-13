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
package si.jrc.msh.plugin.tc.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.application.ViewHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import si.jrc.msh.plugin.tc.utils.TestUtils;
import si.laurentius.commons.SEDGUIConstants;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.user.SEDUser;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "TestCaseMainView")
public class TestCaseMainView {

  private static final SEDLogger LOG = new SEDLogger(TestCaseMainView.class);

  @Resource
  WebServiceContext context;

  @ManagedProperty(value = "#{loginManager}")
  private LoginManager loginManager;

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface mDBLookUp;

  TestUtils mTestUtils = new TestUtils();
  boolean showNavigator = true;
  String currentPanel = AppConstant.S_PANEL_STRESS_TEST;

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
  public SEDUser getUser() {
    long l = LOG.logStart();

    ExternalContext externalContext = facesContext().getExternalContext();
    SEDUser su
            = (SEDUser) externalContext.getSessionMap().get(
                    SEDGUIConstants.SESSION_USER_VARIABLE_NAME);
    if (su == null) {
      Principal principal = facesContext().getExternalContext().
              getUserPrincipal();
      if (principal != null) {
        LOG.formatedlog("User principal is %s", principal.getName());
        su = mDBLookUp.getSEDUserByUserId(principal.getName());
        externalContext.getSessionMap().put(
                SEDGUIConstants.SESSION_USER_VARIABLE_NAME, su);

      } else {
        LOG.log("Principal is NULL");
      }
    }
    return su;
  }

  public List<String> getUserEBoxes() {
    List<String> lst = new ArrayList<>();
    SEDUser usr = getUser();
    if (usr != null) {
      usr.getSEDBoxes().stream().forEach((sb) -> {
        lst.add(sb.getLocalBoxName());
      });
    }
    return lst;
  }

  /**
   *
   * @return
   */
   
  public String getLocalDomain(){
    return SEDSystemProperties.getLocalDomain();
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
  public String getClientIP() {
    return ((HttpServletRequest) externalContext().getRequest()).getRemoteAddr();
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

  public LoginManager getLoginManager() {
    return loginManager;
  }

  public void setLoginManager(LoginManager loginManager) {
    this.loginManager = loginManager;
  }

  public boolean isUserLocalBox(String locbox) {
    LOG.formatedWarning("RECEIVER BOX: %s", locbox);

    String[] bx = locbox.split("@");
    // allow to all to delete/manage  bad boxes
    if (bx.length != 2 || !bx[1].equals(SEDSystemProperties.getLocalDomain())) {
      return true;
    }
    return getUserEBoxes().contains(bx[0]);
  }

  public boolean isShowNavigator() {
    return showNavigator;
  }

  public void setShowNavigator(boolean showNavigator) {
    this.showNavigator = showNavigator;
  }

  /**
   *
   * @param event
   */
  public void onToolbarButtonAction(ActionEvent event) {
    if (event != null) {
      String res = (String) event.getComponent().getAttributes().get("panel");
      currentPanel = res;
    }
  }

  public void setCurrentPanel(String currentPanel) {
    this.currentPanel = currentPanel;
  }
  
  public String getCurrentPanel() {
    return this.currentPanel;
  }
  
  public boolean renderPanel(String panel){
    return !Utils.isEmptyString(currentPanel) && currentPanel.equalsIgnoreCase(
            panel);
  }
    
    
}
