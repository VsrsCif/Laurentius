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
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.msh.outbox.event.MSHOutEvent;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.pmode.Action;
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.web.abst.AbstractMailView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("OutMailDataView")
public class OutMailDataView extends AbstractMailView<MSHOutMail, MSHOutEvent>
        implements
        Serializable {

  private static final SEDLogger LOG = new SEDLogger(OutMailDataView.class);

  private static final long serialVersionUID = 1L;

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface mLookup;
  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPMode;

  StorageUtils msuStorageUtils = new StorageUtils();
  String newMailBody;
  MSHOutMail newOutMail;
  MSHOutPart selectedNewOutMailAttachment;

  @Inject
  private UserSessionData userSessionData;

  /**
   *
   */
  public void composeNewMail() {
    long l = LOG.logStart();
    MSHOutMail m = new MSHOutMail();
    List<String> lstUB = getUserSessionData().getUserEBoxes();

    if (!lstUB.isEmpty()) {
      m.setSenderEBox(lstUB.get(0) + "@" + SEDSystemProperties.
              getLocalDomain());
      m.setReceiverEBox(lstUB.get(lstUB.size() - 1) + "@" + SEDSystemProperties.
              getLocalDomain());
    } else {
      m.setSenderEBox("");
      m.setReceiverEBox("");
    }

    List<Service> srv = mPMode.getServices();
    if (!srv.isEmpty()) {
      m.setService(srv.get(0).getId());
      if (!srv.get(0).getActions().isEmpty()) {
        m.setAction(srv.get(0).getActions().get(0).getName());
      }
    }
    m.setSenderMessageId(Utils.getInstance().getGuidString());
    m.setSubject("VL 1/2016 Predložitveno poročilo, spis I 291/2014");

    newMailBody = "Pozdravljeni!<br />to je testno besedilo<br /> Lep pozdrav";
    setNewOutMail(m);
    LOG.logEnd(l);
  }

  /**
   *
   */
  public void deleteSelectedMail() {
    long l = LOG.logStart();
    if (getSelected() != null && !getSelected().isEmpty()) {
      List<MSHOutMail> molst = getSelected();
      for (MSHOutMail mo : molst) {
        try {
          mDB.setStatusToOutMail(mo, SEDOutboxMailStatus.DELETED,
                  "Manually deleted by "
                  + getUserSessionData().getUser().getUserId());
        } catch (StorageException ex) {
          String mail = String.format(
                  "id: %d, sender: %s, receiver %s, service %s, action %s",
                  mo.getId(), mo.getSenderEBox(), mo.getReceiverEBox(), mo.
                  getService(), mo.getAction());
          facesContext().addMessage(null, new FacesMessage(
                  "'Napaka pri brisanju pošiljke",
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

  /**
   *
   * @return
   */
  public String getComposedMailBody() {
    return newMailBody;
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
        return new DefaultStreamedContent(new FileInputStream(f), MimeValue.
                getMimeTypeByFileName(
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
   * @param bi
   * @return
   */
  @Override
  public StreamedContent getFile(BigInteger bi) {
    long l = LOG.logStart();
    MSHOutPart part = null;
    MSHOutMail mom = getCurrentMail();
    if (mom == null || mom.getMSHOutPayload() == null
            || mom.getMSHOutPayload().getMSHOutParts().isEmpty()) {
      return null;
    }

    for (MSHOutPart ip : mom.getMSHOutPayload().getMSHOutParts()) {
      if (ip.getId().equals(bi)) {
        part = ip;
        break;
      }
    }
    if (part != null) {
      try {
        File f = StorageUtils.getFile(part.getFilepath());
        return new DefaultStreamedContent(new FileInputStream(f), part.
                getMimeType(),
                part.getFilename());
      } catch (FileNotFoundException ex) {
        LOG.logError(l, ex);
      }
    }
    return null;
  }

  /**
   *
   * @return
   */
  public MSHOutMail getNewOutMail() {
    return newOutMail;
  }

  /**
   *
   * @return
   */
  public List<MSHOutPart> getNewOutMailAttachmentList() {
    List<MSHOutPart> lst = new ArrayList<>();
    if (getNewOutMail() != null && getNewOutMail().getMSHOutPayload() != null
            && !getNewOutMail().getMSHOutPayload().getMSHOutParts().isEmpty()) {
      lst = getNewOutMail().getMSHOutPayload().getMSHOutParts();
    }
    return lst;
  }

  /**
   *
   * @return
   */
  public OutMailDataModel getOutMailModel() {
    if (mMailModel == null) {
      mMailModel = new OutMailDataModel(MSHOutMail.class, getUserSessionData(),
              mDB);
    }
    return (OutMailDataModel) mMailModel;
  }

  /**
   *
   * @return
   */
  public List<SEDOutboxMailStatus> getOutStatuses() {
    return Arrays.asList(SEDOutboxMailStatus.values());
  }

  /**
   *
   * @return
   */
  public MSHOutPart getSelectedNewOutMailAttachment() {
    return selectedNewOutMailAttachment;
  }

  /**
   *
   * @param status
   * @return
   */
  @Override
  public String getStatusColor(String status) {
    return SEDOutboxMailStatus.getColor(status);
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
   * @param event
   */
  public void handleNewOutMailAttachmentUpload(FileUploadEvent event) {
    long l = LOG.logStart();
    UploadedFile uf = event.getFile();
    StorageUtils su = new StorageUtils();
    String fileName = uf.getFileName();

    if (getNewOutMail() == null) {
      LOG.logError(l, "Setting file to null composed mail!", null);
      return;
    }
    if (getNewOutMail().getMSHOutPayload() == null) {
      getNewOutMail().setMSHOutPayload(new MSHOutPayload());
    }
    try {
      File f
              = su.storeFile("tst_att", fileName.substring(fileName.lastIndexOf(
                      '.') + 1),
                      uf.getInputstream());

      String name = fileName.substring(0, fileName.lastIndexOf('.'));

      MSHOutPart mp = new MSHOutPart();
      mp.setDescription(name);
      mp.setFilename(fileName);
      mp.setName(name);
      mp.setFilepath(StorageUtils.getRelativePath(f));
      mp.setMimeType(MimeValue.getMimeTypeByFileName(fileName));

      String hashValue = DigestUtils.getHexSha256Digest(f);
      mp.setSha256Value(hashValue);
      mp.setSize(BigInteger.valueOf(f.length()));
      getNewOutMail().getMSHOutPayload().getMSHOutParts().add(mp);

      // FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() +
      // " is uploaded.");
      // FacesContext.getCurrentInstance().addMessage(null, message);
    } catch (StorageException | IOException ex) {
      Logger.getLogger(OutMailDataView.class.getName()).log(Level.SEVERE, null,
              ex);
    }
  }

  @PostConstruct
  private void init() {
    mMailModel = new OutMailDataModel(MSHOutMail.class, getUserSessionData(),
            mDB);
  }

  /**
   *
   */
  public void removeselectedNewOutMailAttachment() {

    if (selectedNewOutMailAttachment != null && getNewOutMail() != null
            && getNewOutMail().getMSHOutPayload() != null) {
      boolean bVal
              = getNewOutMail().getMSHOutPayload().getMSHOutParts().remove(
                      selectedNewOutMailAttachment);
      LOG.log("MSHOutPart removed staus: " + bVal);

    }

  }

  /**
   *
   * @throws IOException
   */
  public void resendSelectedMail()
          throws IOException {
    long l = LOG.logStart();
    if (getSelected() != null && !getSelected().isEmpty()) {
      List<MSHOutMail> molst = getSelected();
      LOG.formatedWarning("get mail count %d to submit", molst.size());
      int iResend = 0, iNotResend = 0, iError = 0;
      StringWriter err = new StringWriter();
      for (MSHOutMail mo : molst) {
        if (SEDOutboxMailStatus.DELIVERED.getValue().equals(mo.getStatus())) {
          iNotResend++;
        } else {
          try {
            mDB.sendOutMessage(mo, 0, 0, AppConstant.S_APPLICATION_CODE,
                    userSessionData.getUser().getUserId());
            iResend++;
          } catch (StorageException ex) {
            String mail = String.format(
                    "id: %d, sender: %s, receiver %s, service %s, action %s\n",
                    mo.getId(), mo.getSenderEBox(), mo.getReceiverEBox(), mo.
                    getService(),
                    mo.getAction());
            err.append(mail);
            LOG.logError(l, ex);
            iError++;
          }
        }
      }
      String strVal = err.toString();
      if (strVal.length() > 500) {
        strVal = strVal.substring(0, 500);
      }

      String msg = String.format(
              "Resend %d, not resend %s (wrong status), error %s\n", iResend,
              iNotResend, iError);
      facesContext().addMessage(null, new FacesMessage(
              iError > 0 ? FacesMessage.SEVERITY_ERROR
                      : FacesMessage.SEVERITY_INFO,
              "'Resending mail action",
              msg + strVal));

    } else {
      facesContext().addMessage(null, new FacesMessage(
              FacesMessage.SEVERITY_WARN,
              "'Resending mail action",
              "No mail selected"));

    }
    RequestContext context = RequestContext.getCurrentInstance();
    context.execute("PF('blockMainPanel').hide();");
    LOG.logEnd(l);
  }

  /**
   *
   */
  public void sendComposedMail() {
    if (newOutMail != null) {
      try {

        String pmodeId = Utils.getPModeIdFromOutMail(newOutMail);
        newOutMail.setReceiverName(newOutMail.getReceiverEBox());
        newOutMail.setSenderName(newOutMail.getSenderEBox());

        MSHOutPart p = new MSHOutPart();
        p.setEncoding("UTF-8");
        p.setDescription("Mail body");
        p.setIsSent(Boolean.TRUE);
        p.setIsReceived(Boolean.FALSE);
        
        p.setMimeType(MimeValue.MIME_TXT.getMimeType());

        // mp.setValue();
        StorageUtils su = new StorageUtils();
        File fout = su.storeFile("tst_", "txt", getComposedMailBody().getBytes(
                "UTF-8"));

        String relPath = StorageUtils.getRelativePath(fout);
        p.setFilepath(relPath);
        String hashValue = DigestUtils.getHexSha256Digest(fout);
        p.setSha256Value(hashValue);
        p.setSize(BigInteger.valueOf(fout.length()));

        if (Utils.isEmptyString(p.getFilename())) {
          p.setFilename(fout.getName());
        }
        if (Utils.isEmptyString(p.getName())) {
          p.setName(p.getFilename().substring(0, p.getFilename().
                  lastIndexOf(".")));
        }

        if (newOutMail.getMSHOutPayload() == null) {
          newOutMail.setMSHOutPayload(new MSHOutPayload());
        }

        newOutMail.getMSHOutPayload().getMSHOutParts().add(0, p);

        newOutMail.setSubmittedDate(Calendar.getInstance().getTime());

        mDB.serializeOutMail(newOutMail, userSessionData.getUser().getUserId(),
                "Laurentius-web",
                pmodeId);
      } catch (UnsupportedEncodingException | StorageException ex) {
        LOG.logError(0, ex);
      }
    }
  }

  /**
   *
   * @param body
   */
  public void setComposedMailBody(String body) {
    newMailBody = body;
  }

  /**
   *
   * @param newOutMail
   */
  public void setNewOutMail(MSHOutMail newOutMail) {
    this.newOutMail = newOutMail;
  }

  /**
   *
   * @param selectedNewOutMailAttachment
   */
  public void setSelectedNewOutMailAttachment(
          MSHOutPart selectedNewOutMailAttachment) {
    this.selectedNewOutMailAttachment = selectedNewOutMailAttachment;
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
   */
  @Override
  public void updateEventList() {
    MSHOutMail mpo = getCurrentMail();
    if (mpo != null) {
      mlstMailEvents = mDB.getMailEventList(MSHOutEvent.class, mpo.getId());
    } else {
      this.mlstMailEvents = null;
    }
  }

  public List<Action> getCurrentServiceActionList() {
    if (getNewOutMail() != null
            && !Utils.isEmptyString(getNewOutMail().getService())) {
      String srvId = getNewOutMail().getService();
      Service srv = mPMode.getServiceById(srvId);
      if (srv != null) {
        return srv.getActions();
      }
    }
    return Collections.emptyList();
  }

  public void setNewMailService(String ts) {
    MSHOutMail nm = getNewOutMail();
    if (nm != null) {
      if (Objects.equals(nm.getService(), ts)) {
        return;
      }
      nm.setService(ts);
      if (!Utils.isEmptyString(ts)) {
        List<Action> lst = getCurrentServiceActionList();
        if (!lst.isEmpty()) {
          nm.setAction(lst.get(0).getName());
        } else {
          nm.setAction(null);
        }
      } else {
        nm.setAction(null);
      }
    }
  }

  public String getNewMailService() {
    MSHOutMail nm = getNewOutMail();
    return nm != null ? nm.getService() : null;
  }

  public List<Action> getCurrentFilterServiceActionList() {
    if (getOutMailModel().getFilter() != null
            && !Utils.isEmptyString(getOutMailModel().getFilter().getService())) {
      String srvId = getOutMailModel().getFilter().getService();
      Service srv = mPMode.getServiceById(srvId);
      if (srv != null) {
        return srv.getActions();
      }
    }
    return Collections.emptyList();
  }

}
