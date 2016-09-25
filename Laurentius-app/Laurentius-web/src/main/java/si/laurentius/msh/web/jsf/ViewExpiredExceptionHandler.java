/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.jsf;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import si.laurentius.commons.SEDGUIConstants;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.gui.LoginManager;
import si.laurentius.user.SEDUser;

/**
 *
 * @author sluzba
 */
public class ViewExpiredExceptionHandler extends ExceptionHandlerWrapper {

  private static final String HOME_PAGE = "/";
  private static final String PAGE_AFTER_LOGOUT = HOME_PAGE; // Another good option is the login

  private static final SEDLogger LOG = new SEDLogger(ViewExpiredExceptionHandler.class);
  private final ExceptionHandler wrapped;

  public ViewExpiredExceptionHandler(ExceptionHandler wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public void handle()
      throws FacesException {
    long l = LOG.logStart();

/*    FacesContext context = facesContext();
    
    ExternalContext externalContext = context.getExternalContext();
    if (externalContext.getSession(false)!=null &&   !externalContext().getSessionMap()
        .containsKey(SEDGUIConstants.SESSION_USER_VARIABLE_NAME)) {
      
      
      externalContext.invalidateSession();
      try {
        externalContext.redirect(externalContext.getRequestContextPath() + PAGE_AFTER_LOGOUT);
      } catch (IOException ex) {
        Logger.getLogger(ViewExpiredExceptionHandler.class.getName()).log(Level.SEVERE, null, ex);
      }

    }*/


    getWrapped().handle();
    LOG.logEnd(l);
  }

  @Override
  public ExceptionHandler getWrapped() {
    return wrapped;
  }

  private ExternalContext externalContext() {
    return facesContext().getExternalContext();
  }

  private FacesContext facesContext() {
    return FacesContext.getCurrentInstance();
  }
}
