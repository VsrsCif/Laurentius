/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.jsf;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author sluzba
 */
public class ViewExpiredExceptionHandler extends ExceptionHandlerWrapper {
  
  private static final String LOGIN_PAGE = "/public/login.xhtml";

  private ExceptionHandler wrapped;

  private static final SEDLogger LOG = new SEDLogger(
          ViewExpiredExceptionHandler.class);

  ViewExpiredExceptionHandler(ExceptionHandler exception) {
    this.wrapped = exception;
  }

  @Override
  public ExceptionHandler getWrapped() {
    return wrapped;
  }

  @Override
  public void handle() throws FacesException {
   

    //logData();
    handleViewExpiredException(FacesContext.
            getCurrentInstance());
    
    getWrapped().handle();
  }

  private void logData() {
    LOG.formatedWarning("_____________________\ngetHandledExceptionQueuedEvent");
    ExceptionQueuedEvent qe = getHandledExceptionQueuedEvent();
    if (qe != null) {
      LOG.formatedWarning("getHandledExceptionQueuedEvent: class %s, source %s",
              qe.getClass().getName(), qe.getSource());
    } else {
      LOG.formatedWarning("getHandledExceptionQueuedEven IS NULL");
    }

    LOG.formatedWarning(
            "_____________________\ngetHandledExceptionQueuedEvents");
    for (Iterator<ExceptionQueuedEvent> iter = getHandledExceptionQueuedEvents()
            .iterator(); iter.hasNext();) {
  ExceptionQueuedEvent que = iter.next();
      LOG.formatedWarning("getHandledExceptionQueuedEvent: class %s, source %s",
              que.getClass().getName(), que.getSource());
    }

    LOG.formatedWarning(
            "_____________________\ngetUnhandledExceptionQueuedEvents");
    for (Iterator<ExceptionQueuedEvent> iter = getUnhandledExceptionQueuedEvents().
            iterator(); iter.hasNext();) {
      ExceptionQueuedEvent que = iter.next();
      LOG.formatedWarning(
              "getUnhandledExceptionQueuedEvents: class %s, source %s",
              que.getClass().getName(), que.getSource());
    }

    FacesContext context = FacesContext.
            getCurrentInstance();

    ExternalContext externalContext = context.getExternalContext();

    LOG.formatedWarning("getUserPrincipal:  %s",
            externalContext.getUserPrincipal() == null ? "null" : externalContext.
            getUserPrincipal());
    LOG.formatedWarning("getSessionMap:  %s",
            externalContext.getSessionMap() == null ? "null" : externalContext.
            getSessionMap());
    LOG.formatedWarning("getRemoteUser:  %s",
            externalContext.getRemoteUser() == null ? "null" : externalContext.
            getRemoteUser());
    LOG.formatedWarning("getRequest:  %s",
            externalContext.getRequest() == null ? "null" : externalContext.
            getRemoteUser());
    LOG.formatedWarning("getRequestMap:  %s",
            externalContext.getRequestMap() == null ? "null" : externalContext.
            getRequestMap());

  }

  private boolean handleViewExpiredException(FacesContext context) {

    ExternalContext externalContext = context.getExternalContext();
    Map<String, Object> rmp=  externalContext.getRequestMap();
    if (
            externalContext.getRemoteUser() == null 
            && rmp!=null && rmp.containsKey("javax.servlet.forward.servlet_path")
            && Objects.equals(rmp.get("javax.servlet.forward.servlet_path"),"/laurentius/sed.xhtml")) {
  

      externalContext.invalidateSession();
      final Map<String, Object> requestMap = externalContext.
              getRequestMap();
      requestMap.put("javax.servlet.error.message",
              "Session expired, try again!");

      context.setViewRoot(context.getApplication().getViewHandler().createView(context,
              LOGIN_PAGE));
      context.getPartialViewContext().setRenderAll(true);
      context.renderResponse();
      return true;

    }
    return false;
  }
}
