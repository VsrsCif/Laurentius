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
package si.laurentius.msh.web.gui.dlg;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import si.laurentius.msh.web.pmode.*;
import java.util.List;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.msh.outbox.payload.MSHOutPart;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("dialogComposePayloadView")
public class DialogComposePayloadView extends AbstractPModeJSFView<MSHOutPart> {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(DialogComposePayloadView.class);

  @Inject
  DialogCompose dialogCompose;

  public DialogCompose getDialogCompose() {
    return dialogCompose;
  }

  public void setDialogCompose(DialogCompose pmpw) {
    this.dialogCompose = pmpw;
  }

  @Override
  public boolean validateData() {
    return true;
  }

  @Override
  public void createEditable() {
    MSHOutPart p = new MSHOutPart();
  

    setNew(p);

  }

  @Override
  public List<MSHOutPart> getList() {
    
    return dialogCompose.getOutMailPayloads();
  }

  @Override
  public boolean persistEditable() {
    boolean bsuc = false;

    MSHOutPart ecj = getEditable();
    
    if (ecj != null ) {
      dialogCompose.addPayload(ecj);
      bsuc = true;
    } else {
      addError("No editable payload!");
    }
    return bsuc;
  }

  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    MSHOutPart ecj = getSelected();
    
    if (ecj != null  ){
      bSuc = dialogCompose.removePayload(ecj);
    } else {
      addError("No editable payload");
    }

    return bSuc;
  }

  @Override
  public boolean updateEditable() {
    boolean bSuc = false;
   
    MSHOutPart ecj = getEditable();
    if (ecj != null ) {
      getSelected().setType(ecj.getType());
      getSelected().setEbmsId(ecj.getEbmsId());
      getSelected().setFilename(ecj.getFilename());
      getSelected().setName(ecj.getName());
      getSelected().setMimeType(ecj.getMimeType());
      getSelected().setEncoding(ecj.getEncoding());
      getSelected().setDescription(ecj.getDescription());
      
      bSuc = true;
    } else {
      addError("No editable payload!");
    }
    return bSuc;
  }

  @Override
  public String getSelectedDesc() {
    return getSelected() != null ? getSelected().toString() : "";
  }
  
  
  
  public void handleNewOutMailAttachmentUpload(FileUploadEvent event) {
    long l = LOG.logStart();
    MSHOutPart mo =  getEditable();
    
    UploadedFile uf = event.getFile();
    StorageUtils su = new StorageUtils();
    String fileName = uf.getFileName();
    String name = fileName.substring(0, fileName.lastIndexOf('.'));
   

    try {
      File f = su.storeFile("doc_", fileName.substring(fileName.lastIndexOf(
              '.') + 1),
              uf.getInputstream());

      if (Utils.isEmptyString(mo.getName())){
        mo.setName(name);
      }
      if (Utils.isEmptyString(mo.getDescription())){
        mo.setDescription(name);
      }
      mo.setFilename(fileName);
      
      
      mo.setFilepath(StorageUtils.getRelativePath(f));
      mo.setMimeType(MimeValue.getMimeTypeByFileName(fileName));

      String hashValue = DigestUtils.getBase64Sha256Digest(f);
      mo.setSha256Value(hashValue);
      mo.setSize(BigInteger.valueOf(f.length()));

      
    } catch (StorageException | IOException ex) {
      String msg = "Error occured while uploading file! Err.: " + ex.
              getMessage();
      LOG.logError(l, msg, ex);
      addError(msg);
    }
  }
  
  
  public void selectedPayloadToTop() {
    
    
    
    MSHOutPart spi = getSelected();
    if (dialogCompose.getNewOutMail() != null && spi != null && dialogCompose.getNewOutMail().getMSHOutPayload()!=null) {
      int idx = dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().indexOf(spi);
      if (idx > 0) {
        dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().remove(spi);
        dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().add(0, spi);
      }

    } else {
      addError("Select payload!");
    }
  }

  public void selectedPayloadToUp() {
    MSHOutPart spi = getSelected();
    if (dialogCompose.getNewOutMail() != null && spi != null && dialogCompose.getNewOutMail().getMSHOutPayload()!=null) {
      int idx = dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().indexOf(spi);
      if (idx > 0) {
        dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().remove(spi);
        dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().add(--idx, spi);
      }

    } else {
      addError("Select payload!");
    }
  }

  public void selectedPayloadToDown() {
    MSHOutPart spi = getSelected();
    if (dialogCompose.getNewOutMail() != null && spi != null && dialogCompose.getNewOutMail().getMSHOutPayload()!=null) {
      int idx = dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().indexOf(spi);
      if (idx < dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().size() - 1) {
        dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().remove(spi);
        dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().add(++idx, spi);
      }

    } else {
      addError("Select payload!");
    }
  }

  public void selectedPayloadToBottom() {
    MSHOutPart spi = getSelected();
    if (dialogCompose.getNewOutMail() != null && spi != null && dialogCompose.getNewOutMail().getMSHOutPayload()!=null) {
      int idx = dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().indexOf(spi);
      if (idx < dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().size() - 1) {
        dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().remove(spi);
        dialogCompose.getNewOutMail().getMSHOutPayload().getMSHOutParts().add(spi);
      }

    } else {
      addError("Select payload!");
    }
  }
  
  public boolean showLaurentiusProperties(){
    return dialogCompose.showLaurentiusProperties();
  }
  
  
  

}
