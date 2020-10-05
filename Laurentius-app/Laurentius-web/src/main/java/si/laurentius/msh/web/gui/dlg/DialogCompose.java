package si.laurentius.msh.web.gui.dlg;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.context.RequestContext;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.outbox.property.MSHOutProperties;
import si.laurentius.msh.outbox.property.MSHOutProperty;
import si.laurentius.msh.pmode.Action;
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.web.gui.OutMailDataView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("dialogCompose")
public class DialogCompose implements Serializable {

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPMode;

  @Inject
  private OutMailDataView mOutMailDataView;

  String newMailBody;
  MSHOutMail newOutMail;

  MSHOutPart mBodyAttachment;
  boolean addBodyAttachment;

  private static final SEDLogger LOG = new SEDLogger(DialogCompose.class);

  public void composeNewMail() {
    long l = LOG.logStart();
    newOutMail = new MSHOutMail();
    newOutMail.setMSHOutPayload(new MSHOutPayload());
    List<String> lstUB = mOutMailDataView.getUserSessionData().getUserEBoxes();

    if (!lstUB.isEmpty()) {
      newOutMail.setSenderEBox(lstUB.get(0) + "@" + SEDSystemProperties.
              getLocalDomain());
      newOutMail.setReceiverEBox(
              lstUB.get(lstUB.size() - 1) + "@" + SEDSystemProperties.
              getLocalDomain());
    } else {
      newOutMail.setSenderEBox("");
      newOutMail.setReceiverEBox("");
    }

    List<Service> srv = mPMode.getServices();
    if (!srv.isEmpty()) {
      setNewMailService(srv.get(0).getId());
    }
    newOutMail.setSenderMessageId(Utils.getInstance().getGuidString());

    MSHOutPart mp = new MSHOutPart();
    mp.setMimeType(MimeValue.MIME_TXT.getMimeType());
    mp.setEncoding("UTF-8");
    mp.setDescription("");
    newMailBody = "Test body";
    setBodyAttachment(mp);
    addBodyAttachment = true;

    LOG.logEnd(l);
  }

  public List<MSHOutProperty> getActionMessageProperties() {
    return getNewOutMail() != null && getNewOutMail().getMSHOutProperties() != null
            ? getNewOutMail().getMSHOutProperties().getMSHOutProperties() : Collections.
            emptyList();

  }

  public boolean isAddBodyAttachment() {
    return addBodyAttachment;
  }

  public void setAddBodyAttachment(boolean addBodyAttachment) {
    this.addBodyAttachment = addBodyAttachment;
  }

  public MSHOutPart getBodyAttachment() {
    return mBodyAttachment;
  }

  public void setBodyAttachment(MSHOutPart bodyAttachment) {
    this.mBodyAttachment = bodyAttachment;
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
   * @return
   */
  public MSHOutMail getNewOutMail() {
    return newOutMail;
  }

  /**
   *
   * @return
   */
  public List<MSHOutPart> getOutMailPayloads() {
    return newOutMail != null
            ? newOutMail.getMSHOutPayload().getMSHOutParts() : Collections.
            emptyList();
  }

  /**
   *
   * @param part
   */
  public boolean removePayload(MSHOutPart part) {

    if (part != null && newOutMail != null
            && newOutMail.getMSHOutPayload() != null) {
      return newOutMail.getMSHOutPayload().getMSHOutParts().remove(
              part);
    }
    return false;

  }

  /**
   *
   */
  public void sendComposedMail() {
    if (newOutMail != null) {
      try {

        newOutMail.setReceiverName(newOutMail.getReceiverEBox());
        newOutMail.setSenderName(newOutMail.getSenderEBox());

        // validate
        if (!validateMail()) {
          return;
        }

        newOutMail.setSubmittedDate(Calendar.getInstance().getTime());

        // add new out mail
        mOutMailDataView.addNewMail(newOutMail);
        // send signal to close dialog
        RequestContext.getCurrentInstance().addCallbackParam("saved", true);

      } catch (StorageException | PModeException ex) {
        String msg = ex.getMessage();
        facesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                        msg));
        LOG.logError(0, ex);

      }
    }
  }

  boolean validateMail() {
    boolean suc = true;
    if (Utils.isEmptyString(newOutMail.getSenderEBox())) {
      suc = false;
      addError("Missing sender box");
    }
    if (Utils.isEmptyString(newOutMail.getReceiverEBox())) {
      suc = false;
      addError("Missing receiver box");
    }
    if (Utils.isEmptyString(newOutMail.getService())) {
      suc = false;
      addError("Missing service");
    }
    
    if (newOutMail.getMSHOutPayload().getMSHOutParts().isEmpty()) {
      suc = false;
      addError("Missing payloads!");
    }
    
    if (Utils.isEmptyString(newOutMail.getAction())) {
      suc = false;
      addError("Missing action");
    } else {
      suc = suc && validateMailProperties();
    }


    return suc;

  }

  boolean validateMailProperties() {
    boolean suc = true;
    Action act = getCurrentServiceAction(newOutMail.getAction());
    if (act.getProperties() == null
            || act.getProperties().getProperties().isEmpty()) {

      if (newOutMail.getMSHOutProperties() != null && !newOutMail.
              getMSHOutProperties().getMSHOutProperties().isEmpty()) {
        LOG.formatedlog("Clear mail (service %s, actin %s) properties, "
                + "action type do not contains any properies",
                newOutMail.getService(), newOutMail.getAction());
        newOutMail.getMSHOutProperties().getMSHOutProperties().clear();
      }

    }

    List<Action.Properties.Property> plst = act.getProperties().getProperties();
    List<MSHOutProperty> mopNewLst = new ArrayList<>();

    Map<String, String> mprp = new HashMap<>();
    if (newOutMail.getMSHOutProperties() != null) {
      for (MSHOutProperty mo : newOutMail.getMSHOutProperties().
              getMSHOutProperties()) {
        mprp.put(mo.getName(), mo.getValue());
      }
    }

    for (Action.Properties.Property p : plst) {
      if (mprp.containsKey(p.getName())) {
        if (!Utils.isEmptyString(mprp.get(p.getName()))) {
          MSHOutProperty mop = new MSHOutProperty();
          mop.setName(p.getName());
          mop.setType(p.getType());
          mop.setValue(mprp.get(p.getName()));
          mopNewLst.add(mop);
        } else if (p.getRequired() != null && p.getRequired()) {
          suc = false;
          addError("Missing property: " + p.getName());
        }
      }
    }
    
    // if ok  - then set new list
    if (suc && newOutMail.getMSHOutProperties() != null){
      newOutMail.getMSHOutProperties().getMSHOutProperties().clear();
      newOutMail.getMSHOutProperties().getMSHOutProperties().addAll(mopNewLst);    
    }

    

    return suc;
  }

  protected void addError(String desc) {
    facesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    desc));

  }

  protected FacesContext facesContext() {
    return FacesContext.getCurrentInstance();
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

  public Action getCurrentServiceAction(String actName) {
    List<Action> lstAct = getCurrentServiceActionList();
    for (Action act : lstAct) {
      if (Objects.equals(act.getName(), actName)) {
        return act;
      }
    }
    return null;
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
          setNewMailAction(lst.get(0).getName());
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

  public String getNewMailAction() {
    MSHOutMail nm = getNewOutMail();
    return nm != null ? nm.getAction() : null;
  }

  public void setNewMailAction(String strAct) {
    MSHOutMail nm = getNewOutMail();
    if (nm != null) {
      if (strAct == null) {
        nm.setAction(null);
        if (nm.getMSHOutProperties() != null) {
          nm.getMSHOutProperties().getMSHOutProperties().clear();
        }

      } else if (!Objects.equals(nm.getAction(), strAct)) {
        nm.setAction(strAct);
        Action act = getCurrentServiceAction(strAct);
        if (act.getProperties() != null) {
          setMessagePopertyList(nm, act.getProperties().getProperties());
        } else if (nm.getMSHOutProperties() != null) {
          nm.getMSHOutProperties().getMSHOutProperties().clear();

        }
      }

    }

  }

  private void setMessagePopertyList(MSHOutMail nm,
          List<Action.Properties.Property> prpLst) {
    if (nm.getMSHOutProperties() == null) {
      nm.setMSHOutProperties(new MSHOutProperties());
    } else {
      nm.getMSHOutProperties().getMSHOutProperties().clear();
    }

    for (Action.Properties.Property p : prpLst) {
      MSHOutProperty prp = new MSHOutProperty();
      prp.setName(p.getName());
      nm.getMSHOutProperties().getMSHOutProperties().add(prp);
    }
  }

  public void clearTextPayload() {

    newMailBody = "";
    mBodyAttachment.setEbmsId("");
    mBodyAttachment.setMimeType(MimeValue.MIME_TXT.getMimeType());
    mBodyAttachment.setEncoding("UTF-8");
    mBodyAttachment.setDescription("");

  }

  public void addTextPayload() {

    MSHOutPart p = XMLUtils.deepCopyJAXB(mBodyAttachment);

    String suffix = MimeValue.getSuffixBYMimeType(p.getMimeType());

    // mp.setValue();
    StorageUtils su = new StorageUtils();
    File fout;
    try {
      fout = su.storeFile("document", suffix, getComposedMailBody().getBytes(
              p.getEncoding()));
      String relPath = StorageUtils.getRelativePath(fout);
      p.setFilepath(relPath);

      String hashValue = DigestUtils.getBase64Sha256Digest(fout);
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

      newOutMail.getMSHOutPayload().getMSHOutParts().add(p);
    } catch (UnsupportedEncodingException | StorageException ex) {
      String errMsg = "Error occured while adding text payload: " + ex.
              getMessage();
      addError(errMsg);
      LOG.logError(errMsg, ex);
    }

  }

  public void addPayload(MSHOutPart p) {
    newOutMail.getMSHOutPayload().getMSHOutParts().add(p);
  }

  public boolean showLaurentiusProperties() {

    Service srv = mPMode.getServiceById(newOutMail.getService());
    return srv == null || srv.getUseSEDProperties() == null || srv.
            getUseSEDProperties();
  }

}
