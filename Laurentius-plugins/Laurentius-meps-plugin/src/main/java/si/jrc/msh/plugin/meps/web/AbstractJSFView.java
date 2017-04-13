/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.meps.web;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.context.RequestContext;

/**
 *
 * @author Jože Rihtaršič
 */
abstract class AbstractJSFView {

  public static final String CB_PARA_SAVED = "saved";
  public static final String CB_PARA_REMOVED = "removed";
  public static final String CB_PARA_SUCCESS = "success";
  
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

  protected void addError(String desc) {
    facesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    desc));

  }

  /**
   *
   * @return
   */
  public String getClientIP() {
    return ((HttpServletRequest) externalContext().getRequest()).
            getRemoteAddr();
  }

  public void addCallbackParam(String val, boolean bval) {
    RequestContext.getCurrentInstance().addCallbackParam(val, bval);
  }

  public void update(String... ids) {
    for (String id : ids) {
      RequestContext.getCurrentInstance().update(id);

    }
  }
}
