/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.meps.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.meps.envelope.EnvelopeData;
import si.laurentius.meps.envelope.PhysicalAddressType;
import si.laurentius.meps.envelope.PostalData;
import si.laurentius.meps.envelope.SenderMailData;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.plugin.meps.ServiceType;

/**
 *
 * @author sluzba
 */
@SessionScoped
@ManagedBean(name = "dialogComposeMailView")
public class DialogComposeMailView {

  private static final SEDLogger LOG = new SEDLogger(DialogComposeMailView.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @ManagedProperty(value = "#{MEPSLookups}")
  private MEPSLookups pluginLookups;

  @ManagedProperty(value = "#{MEPSPluginData}")
  private MEPSPluginData pluginData;

  MSHOutMail newOutMail;
  EnvelopeData envelopeData;

  MSHOutPart selectedNewOutMailAttachment;

  public MEPSLookups getPluginLookups() {
    return pluginLookups;
  }

  public void setPluginLookups(MEPSLookups pluginLookups) {
    this.pluginLookups = pluginLookups;
  }

  public MEPSPluginData getPluginData() {
    return pluginData;
  }

  public void setPluginData(MEPSPluginData pluginData) {
    this.pluginData = pluginData;
  }

  public MSHOutMail getNewOutMail() {
    return newOutMail;
  }

  public void setNewOutMail(MSHOutMail newOutMail) {
    this.newOutMail = newOutMail;
  }

  public EnvelopeData getEnvelopeData() {
    return envelopeData;
  }

  public void setEnvelopeData(EnvelopeData envelopeData) {
    this.envelopeData = envelopeData;
  }

  public MSHOutPart getSelectedNewOutMailAttachment() {
    return selectedNewOutMailAttachment;
  }

  public void setSelectedNewOutMailAttachment(
          MSHOutPart selectedNewOutMailAttachment) {
    this.selectedNewOutMailAttachment = selectedNewOutMailAttachment;
  }

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

      getNewOutMail().getMSHOutPayload().getMSHOutParts().add(mp);

      // FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() +
      // " is uploaded.");
      // FacesContext.getCurrentInstance().addMessage(null, message);
    } catch (StorageException | IOException ex) {
      Logger.getLogger(DialogComposeMailView.class.getName()).log(Level.SEVERE,
              null,
              ex);
    }
  }

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

  public void sendComposedMail() {
    if (newOutMail != null) {
      try {

        String pmodeId = Utils.getPModeIdFromOutMail(newOutMail);
        newOutMail.setReceiverName(newOutMail.getReceiverEBox());
        newOutMail.setSenderName(newOutMail.getSenderEBox());

        MSHOutPart p = new MSHOutPart();
        p.setEncoding("UTF-8");
        p.setDescription("Mail body");
        p.setMimeType(MimeValue.MIME_TXT.getMimeType());

        // mp.setValue();
        StorageUtils su = new StorageUtils();
        File fout = su.storeFile("tst_", "txt", "<test xml>".getBytes());

        String relPath = StorageUtils.getRelativePath(fout);
        p.setFilepath(relPath);

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

        mDB.serializeOutMail(newOutMail, pluginData.getUser().getUserId(),
                "meps-plugin",
                pmodeId);
      } catch (StorageException ex) {
        LOG.logError(0, ex);
      }
    }
  }

  public void createNewMail() {
    newOutMail = new MSHOutMail();
    envelopeData = new EnvelopeData();
    envelopeData.setExecutorContractId("Contrat id");
    envelopeData.setExecutorId("meps1");
    envelopeData.setPostalData(new PostalData());
    envelopeData.getPostalData().setUPNCode(new PostalData.UPNCode());
    if (!pluginLookups.getServices().isEmpty()) {
      ServiceType st = pluginLookups.getServices().get(0);
      envelopeData.getPostalData().setMepsService(st.getName());
      envelopeData.getPostalData().setEnvelopeType(st.getEnvelopeName());
      if (st.isUseUPN()) {
        envelopeData.getPostalData().getUPNCode().setPrefix(st.getUPNPrefix());
        envelopeData.getPostalData().getUPNCode().setSuffix("SI");
      } else {
        envelopeData.getPostalData().getUPNCode().setPrefix("");
        envelopeData.getPostalData().getUPNCode().setCode(null);
        envelopeData.getPostalData().getUPNCode().setControl(null);
        envelopeData.getPostalData().getUPNCode().setSuffix("");

      }
    }
    envelopeData.getPostalData().setPostalContractId("1235-5-12-1102");
    envelopeData.getPostalData().setSubmitPostalCode("1102");
    envelopeData.getPostalData().setSubmitPostalName("Ljubljana");

    envelopeData.setSenderAddress(new PhysicalAddressType());
    envelopeData.getSenderAddress().setAddress(pluginLookups.getSenderAddress().
            getAddress());
    envelopeData.getSenderAddress().setCountry(pluginLookups.getSenderAddress().
            getCountry());
    envelopeData.getSenderAddress().setCountryCode(pluginLookups.
            getSenderAddress().getCountryCode());
    envelopeData.getSenderAddress().setName(pluginLookups.getSenderAddress().
            getName());
    envelopeData.getSenderAddress().setName2(pluginLookups.getSenderAddress().
            getName2());
    envelopeData.getSenderAddress().setPostalCode(pluginLookups.
            getSenderAddress().getPostalCode());
    envelopeData.getSenderAddress().setPostalName(pluginLookups.
            getSenderAddress().getPostalName());
    envelopeData.getSenderAddress().setTown(pluginLookups.getSenderAddress().
            getTown());

  }

  public PhysicalAddressType getSenderAddress() {
    if (envelopeData == null) {
      envelopeData = new EnvelopeData();
    }
    if (envelopeData.getSenderAddress() == null) {
      envelopeData.setSenderAddress(new PhysicalAddressType());;
    }
    return envelopeData.getSenderAddress();
  }

  public PhysicalAddressType getReceiverAddress() {
    if (envelopeData == null) {
      envelopeData = new EnvelopeData();
    }
    if (envelopeData.getReceiverAddress() == null) {
      envelopeData.setReceiverAddress(new PhysicalAddressType());;
    }
    return envelopeData.getReceiverAddress();
  }

  public SenderMailData getSenderData() {
    if (envelopeData == null) {
      envelopeData = new EnvelopeData();
    }
    if (envelopeData.getSenderMailData() == null) {
      envelopeData.setSenderMailData(new SenderMailData());;
    }
    return envelopeData.getSenderMailData();
  }

  public PostalData getPostalData() {
    if (envelopeData == null) {
      envelopeData = new EnvelopeData();
    }
    if (envelopeData.getPostalData() == null) {
      envelopeData.setPostalData(new PostalData());
    }
    if (envelopeData.getPostalData().getUPNCode() == null) {
      envelopeData.getPostalData().setUPNCode(new PostalData.UPNCode());
    }
    return envelopeData.getPostalData();
  }

}
