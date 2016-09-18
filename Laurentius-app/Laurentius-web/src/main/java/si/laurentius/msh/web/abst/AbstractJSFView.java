/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.abst;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Jože Rihtaršič
 */
public class AbstractJSFView {

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
  public String getClientIP() {
    return ((HttpServletRequest) externalContext().getRequest()).getRemoteAddr();
  }

}
