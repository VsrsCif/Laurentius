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
package si.laurentius.msh.web.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.jms.JMSException;
import javax.naming.NamingException;
import org.primefaces.context.RequestContext;
import si.laurentius.msh.inbox.event.MSHInEvent;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.web.abst.AbstractMailView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "InMailDataView")
public class InMailDataView extends AbstractMailView<MSHInMail, MSHInEvent> implements Serializable {

  private static final SEDLogger LOG = new SEDLogger(InMailDataView.class);
  private static final long serialVersionUID = 1L;

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  @ManagedProperty(value = "#{userSessionData}")
  private UserSessionData userSessionData;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPMode;

  @PostConstruct
  private void init() {
    mMailModel = new InMailDataModel(MSHInMail.class, userSessionData, mDB);
  }

  /**
   *
   * @param messageBean
   */
  public void setUserSessionData(UserSessionData messageBean) {
    this.userSessionData = messageBean;

  }

  /**
   *
   * @return
   */
  public UserSessionData getUserSessionData() {
    return this.userSessionData;
  }

  /**
   *
   * @return
   */
  public InMailDataModel getInMailModel() {
    return (InMailDataModel) mMailModel;
  }

  /**
   *
   * @param status
   * @return
   */
  @Override
  public String getStatusColor(String status) {
    return SEDInboxMailStatus.getColor(status);
  }

  /**
   *
   */
  @Override
  public void updateEventList() {
    MSHInMail mim = getCurrentMail();
    if (mim != null) {
      mlstMailEvents = mDB.getMailEventList(MSHInEvent.class, mim.getId());
    } else {
      mlstMailEvents = null;
    }
  }

  /**
   *
   * @param bi
   * @return
   */
  @Override
  public StreamedContent getFile(BigInteger bi) {
    MSHInPart inpart = null;
    MSHInMail mim = getCurrentMail();
    if (mim == null || mim.getMSHInPayload() == null ||
        mim.getMSHInPayload().getMSHInParts().isEmpty()) {
      return null;
    }

    for (MSHInPart ip : mim.getMSHInPayload().getMSHInParts()) {
      if (ip.getId().equals(bi)) {
        inpart = ip;
        break;
      }
    }
    if (inpart != null) {
      try {
        File f = StorageUtils.getFile(inpart.getFilepath());
        return new DefaultStreamedContent(new FileInputStream(f), inpart.getMimeType(),
            inpart.getFilename());
      } catch (FileNotFoundException ex) {
        Logger.getLogger(InMailDataView.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return null;
  }

  /**
   *
   * @param filePath
   * @return
   */
  @Override
  public StreamedContent getEventEvidenceFile(String filePath) {
    long l = LOG.logStart();
    File f = StorageUtils.getFile(filePath);
    if (f.exists()) {
      try {
        return new DefaultStreamedContent(new FileInputStream(f), MimeValue.getMimeTypeByFileName(
            f.getName()),
            f.getName());
      } catch (FileNotFoundException ex) {
        LOG.logError(l, ex);
      }
    }
    LOG.formatedWarning("Event file '%s' not found ", filePath);
    return null;
  }

  /**
   *
   * @return
   */
  public List<SEDInboxMailStatus> getInStatuses() {
    return Arrays.asList(SEDInboxMailStatus.values());
  }

  public void deleteSelectedMail() {
    setStatusSelectedMail(SEDInboxMailStatus.DELETED);
  }

  public void setPluginLockedSelectedMail() {
    setStatusSelectedMail(SEDInboxMailStatus.PLOCKED);
  }

  public void setReceivedSelectedMail() {
    setStatusSelectedMail(SEDInboxMailStatus.RECEIVED);
  }

  public void setStatusSelectedMail(SEDInboxMailStatus status) {
    long l = LOG.logStart();

    if (getSelected() != null && !getSelected().isEmpty()) {
      List<MSHInMail> milst = getSelected();
      for (MSHInMail mi : milst) {

        try {
          mDB.setStatusToInMail(mi, status, "Status changed to '" + status.getValue() +
              "' by " +
              getUserSessionData().getUser().getUserId(), getUserSessionData().getUser().getUserId(),
              "laurentius-web");
        } catch (StorageException ex) {
          String mail = String.format("id: %d, sender: %s, receiver %s, service %s, action %s",
              mi.getId(), mi.getSenderEBox(), mi.getReceiverEBox(), mi.getService(), mi.getAction());
          facesContext().addMessage(null, new FacesMessage(
              "'Napaka pri spreminjanju statusa pošiljke",
              mail));

          break;
        }

      }

    }
    RequestContext context = RequestContext.getCurrentInstance();
    context.execute("PF('blockMainPanel').hide();");
    LOG.logEnd(l);
  }

  public void exportSelectedMail() {
    long l = LOG.logStart();
    if (getSelected() != null && !getSelected().isEmpty()) {
      List<MSHInMail> milst = getSelected();
      for (MSHInMail mi : milst) {
        try {
          mDB.setStatusToInMail(mi, SEDInboxMailStatus.LOCKED, "Send mail to export queue",
              getUserSessionData().getUser().getUserId(), "laurentius-web");
          mJMS.exportInMail(mi.getId().longValue());
        } catch (StorageException | NamingException | JMSException ex) {
          String mail = String.format("Error submiting mail to export queue  %s",
              ex.getMessage());
          facesContext().addMessage(null, new FacesMessage(
              "'Napaka pri spreminjanju statusa pošiljke",
              mail));
          LOG.logError(l, ex);
          break;

        }
      }
    }
    RequestContext context = RequestContext.getCurrentInstance();
    context.execute("PF('blockMainPanel').hide();");
    LOG.logEnd(l);
  }

  public List<Service.Action> getCurrentFilterServiceActionList() {
    if (getInMailModel().getFilter() != null &&
        !Utils.isEmptyString(getInMailModel().getFilter().getService())) {
      String srvId = getInMailModel().getFilter().getService();
      Service srv = mPMode.getServiceById(srvId);
      if (srv != null) {
        return srv.getActions();
      }
    }
    return Collections.emptyList();
  }
  
  
}
