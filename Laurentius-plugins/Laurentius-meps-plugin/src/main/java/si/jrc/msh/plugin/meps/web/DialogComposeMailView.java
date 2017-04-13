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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.xml.bind.JAXBException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.meps.envelope.EnvelopeData;
import si.laurentius.meps.envelope.PhysicalAddressType;
import si.laurentius.meps.envelope.PostalData;
import si.laurentius.meps.envelope.SenderMailData;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.plugin.meps.PartyType;
import si.laurentius.plugin.meps.ServiceType;

/**
 *
 * @author sluzba
 */
@SessionScoped
@ManagedBean(name = "dialogComposeMailView")
public class DialogComposeMailView extends AbstractJSFView {

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

  String serviceProvider;
  String serviceRequestor;
  
  si.laurentius.plugin.meps.PhysicalAddressType selectedAddress;

  public si.laurentius.plugin.meps.PhysicalAddressType getSelectedAddress() {
    return selectedAddress;
  }

  public void setSelectedAddress(
          si.laurentius.plugin.meps.PhysicalAddressType selectedAddress) {
    this.selectedAddress = selectedAddress;
  }

  
  
  public String getServiceProvider() {
    return serviceProvider;
  }

  public void setServiceProvider(String serviceProvider) {
    this.serviceProvider = serviceProvider;
  }

  public String getServiceRequestor() {
    return serviceRequestor;
  }

  public void setServiceRequestor(String serviceRequestor) {
    this.serviceRequestor = serviceRequestor;
  }

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

  public String getCurrentService() {
    return getNewOutMail().getService();

  }

  public void setCurrentService(String srv) {
    List<ServiceType> lst = pluginLookups.getServices();
    for (ServiceType st : lst) {
      if (Objects.equals(st.getName(), srv)) {
        getPostalData().setMepsService(st.getName());
        getPostalData().setEnvelopeType(st.getEnvelopeName());
        getPostalData().getUPNCode().setPrefix(st.getUPNPrefix());
        return;
      }
    }

  }

  public boolean hasUPNCode() {
    List<ServiceType> lst = pluginLookups.getServices();
    for (ServiceType st : lst) {
      if (Objects.equals(st.getName(), getCurrentService())) {
        return st.isUseUPN();

      }
    }
    return true;

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
        newOutMail.setId(null);
        newOutMail.
                setService(getEnvelopeData().getPostalData().getMepsService());
        newOutMail.setAction("AddMail");
        newOutMail.setSenderEBox(getServiceRequestor());
        newOutMail.setReceiverEBox(getServiceProvider());
        newOutMail.setReceiverName(newOutMail.getReceiverEBox());
        newOutMail.setSenderName(newOutMail.getSenderEBox());
        newOutMail.setSubject("Print and envelope: " + getEnvelopeData().
                getSenderMailData().getCaseCode());

        String pmodeId = Utils.getPModeIdFromOutMail(newOutMail);

        StorageUtils su = new StorageUtils();

        MSHOutPart p = new MSHOutPart();
        p.setEncoding("UTF-8");
        p.setDescription("EnvelopeData");
        p.setName("EnvelopeData");

        p.setMimeType(MimeValue.MIME_XML.getMimeType());

        byte[] buff = XMLUtils.serialize(getEnvelopeData());

        File fout = su.storeFile("envdata_", "xml", buff);

        String relPath = StorageUtils.getRelativePath(fout);
        p.setFilepath(relPath);

        if (Utils.isEmptyString(p.getFilename())) {
          p.setFilename(fout.getName());
        }

        if (newOutMail.getMSHOutPayload() == null) {
          newOutMail.setMSHOutPayload(new MSHOutPayload());
        }
        newOutMail.getMSHOutPayload().getMSHOutParts().add(0, p);
        newOutMail.setSubmittedDate(Calendar.getInstance().getTime());
        mDB.serializeOutMail(newOutMail, pluginData.getUser().getUserId(),
                "MEPS",
                pmodeId);
        addCallbackParam(AbstractJSFView.CB_PARA_SAVED, true);
      } catch (JAXBException | StorageException ex) {
        addCallbackParam(AbstractJSFView.CB_PARA_SAVED, true);
        addError(ex.getMessage());
        LOG.logError(0, ex);
      }
    }
  }

  
  public void setSelectedAddressToMail(){
    if (selectedAddress!=null &&  getReceiverAddress()!=null){
      getReceiverAddress().setName(getSelectedAddress().getName());
      getReceiverAddress().setName2(getSelectedAddress().getName2());
      getReceiverAddress().setAddress(getSelectedAddress().getAddress());
      getReceiverAddress().setTown(getSelectedAddress().getTown());
      getReceiverAddress().setPostalName(getSelectedAddress().getPostalName());
      getReceiverAddress().setPostalCode(getSelectedAddress().getPostalCode());
      getReceiverAddress().setCountry(getSelectedAddress().getCountry());
      getReceiverAddress().setCountryCode(getSelectedAddress().getCountryCode());
      
    }
    addCallbackParam(CB_PARA_SAVED, true);
  }
          
  public void createNewMail() {
    PartyType.PostContract pc = pluginLookups.getPostContract();
    newOutMail = new MSHOutMail();
    envelopeData = new EnvelopeData();
    PartyType.ServiceProviderContract spc = new PartyType.ServiceProviderContract();
    envelopeData.setExecutorContractId(spc.getCode());
    envelopeData.setExecutorId(spc.getName());
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
    if (pc != null) {
      envelopeData.getPostalData().setPostalContractName(pc.getName());
      envelopeData.getPostalData().setPostalContractId(pc.getCode());
      envelopeData.getPostalData().setSubmitPostalCode(pc.getSubmitPostalCode());
      envelopeData.getPostalData().setSubmitPostalName(pc.getSubmitPostalName());
    }

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
