package si.laurentius.msh.web.gui;


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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.event.UnselectEvent;
import si.laurentius.user.SEDUser;
import si.laurentius.commons.SEDGUIConstants;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.abst.AbstractJSFView;
import si.laurentius.plugin.def.Plugin;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("userSessionData")
public class UserSessionData extends AbstractJSFView {

  private static final SEDLogger LOG = new SEDLogger(UserSessionData.class);
  @Inject
  private LoginManager loginManager;
  private String mstrCurrentSEDBox;

  /**
   *
   * @return
   */
  public String getCurrentSEDBox() {
    return mstrCurrentSEDBox == null && getUserEBoxes() != null && !getUserEBoxes().isEmpty() ? getUserEBoxes()
        .get(0) : mstrCurrentSEDBox;
  }

  /**
   *
   * @return
   */
  public LoginManager getLoginManager() {
    return loginManager;
  }

  /**
   *
   * @return
   */
  public SEDUser getUser() {
    long l = LOG.logStart();
    FacesContext context = facesContext();
    ExternalContext externalContext = context.getExternalContext();
    SEDUser su =
        (SEDUser) externalContext.getSessionMap().get(SEDGUIConstants.SESSION_USER_VARIABLE_NAME);
    if (su == null) {
      try {
        loginManager.logout();
      } catch (IOException ex) {
        LOG.logError(l, ex);
      }
    }
    return su;
  }

  /**
   *
   * @return
   */
  public List<String> getUserEBoxes() {
    List<String> lst = new ArrayList<>();
    SEDUser usr = getUser();
    if (usr != null) {
      getUser().getSEDBoxes().stream().forEach((sb) -> {
        lst.add(sb.getLocalBoxName());
      });
    }
    return lst;
  }
  public List<String> getUserEBoxesWithDomain() {
    List<String> lst = new ArrayList<>();
    SEDUser usr = getUser();
    if (usr != null) {
      getUser().getSEDBoxes().stream().forEach((sb) -> {
        lst.add(sb.getLocalBoxName() + "@" + SEDSystemProperties.getLocalDomain());
      });
    }
    return lst;
  }

  /**
     *
     */
  public void onReorder() {
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "List Reordered", null));
  }

  /**
   *
   * @param event
   */
  public void onSelect(SelectEvent event) {
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Item Selected", event
        .getObject().toString()));
  }

  /**
   *
   * @param event
   */
  public void onTransfer(TransferEvent event) {
    /*
     * StringBuilder builder = new StringBuilder(); for(Object item : event.getItems()) {
     * builder.append(((Theme) item).getName()).append("<br />"); }
     * 
     * FacesMessage msg = new FacesMessage(); msg.setSeverity(FacesMessage.SEVERITY_INFO);
     * msg.setSummary("Items Transferred"); msg.setDetail(builder.toString());
     * 
     * FacesContext.getCurrentInstance().addMessage(null, msg);
     */
  }

  /**
   *
   * @param event
   */
  public void onUnselect(UnselectEvent event) {
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Item Unselected", event
        .getObject().toString()));
  }

  /**
   *
   * @param strCurrBox
   */
  public void setCurrentSEDBox(String strCurrBox) {
    mstrCurrentSEDBox = strCurrBox;
  }

  /**
   *
   * @param loginManager
   */
  public void setLoginManager(LoginManager loginManager) {
    this.loginManager = loginManager;
  }
  
  public boolean showPluginForUser(Plugin plg){
    
     return getUser().isAdminRole() || plg.getWebRoles().contains(SEDGUIConstants.ROLE_USER);

  }

}
