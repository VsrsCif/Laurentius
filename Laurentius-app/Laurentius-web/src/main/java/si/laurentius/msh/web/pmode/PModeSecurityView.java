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
package si.laurentius.msh.web.pmode;

import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.context.RequestContext;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.pmode.enums.SecEncryptionAlgorithm;
import si.laurentius.commons.pmode.enums.SecHashFunction;
import si.laurentius.commons.pmode.enums.SecSignatureAlgorithm;
import si.laurentius.commons.pmode.enums.SecX509KeyIdentifier;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.pmode.References;
import si.laurentius.msh.pmode.Security;
import si.laurentius.msh.pmode.X509;
import si.laurentius.msh.pmode.XPath;
import si.laurentius.msh.web.gui.dlg.DialogXPath;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "pModeSecurityView")
public class PModeSecurityView extends AbstractPModeJSFView<Security> {

  public static final X509.Signature NULL_SIGNATURE = new X509.Signature();
  public static final X509.Encryption NULL_ENCRYPTION = new X509.Encryption();
  /**
   *
   *
   */
  public static SEDLogger LOG = new SEDLogger(PModeSecurityView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPModeInteface;

  XPath selectedSignXPath;
  XPath selectedEncryptXPath;

  /**
   *
   */
  @PostConstruct
  public void init() {

  }

  /**
   *
   */
  @Override
  public void createEditable() {

    String sbname = "SECURITY_%03d";
    int i = 1;

    while (mPModeInteface.getSecurityById(String.format(sbname, i)) != null) {
      i++;
    }

    Security ed = new Security();
    ed.setId(String.format(sbname, i));
    ed.setWSSVersion("1.1");
    ed.setX509(new X509());

    ed.getX509().setSignature(new X509.Signature());
    ed.getX509().getSignature().setAlgorithm(
            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512");
    ed.getX509().getSignature().setHashFunction(
            "http://www.w3.org/2001/04/xmlenc#sha512");
    ed.getX509().getSignature().setHashFunction("IssuerSerial");

    ed.getX509().getSignature().setReference(new X509.Signature.Reference());
    ed.getX509().getSignature().getReference().setAllAttachments(true);

    ed.getX509().setEncryption(new X509.Encryption());
    ed.getX509().getEncryption().setAlgorithm(
            "http://www.w3.org/2009/xmlenc11#aes128-gcm");

    ed.getX509().getEncryption().setMinimumStrength("128");
    ed.getX509().getEncryption().setReference(new X509.Encryption.Reference());
    ed.getX509().getEncryption().getReference().setAllAttachments(true);

    setNew(ed);

  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    Security srv = getSelected();
    if (srv != null) {
      mPModeInteface.removeSecurity(srv);;
      bSuc = true;
    }
    return bSuc;
  }

  @Override
  public boolean validateData() {
    Security cj = getEditable();
    if (isEditableNew() && (mPModeInteface.getSecurityById(cj.getId()) != null)) {
      addError("Name: '" + cj.getId() + "' already exists!");
      return false;
    }

    return true;
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    long l = LOG.logStart();
    boolean bsuc = false;
    Security sv = getEditable();
    if (sv != null) {
      mPModeInteface.addSecurity(sv);
      setEditable(null);
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    long l = LOG.logStart();
    boolean bsuc = false;
    Security sv = getEditable();
    if (sv != null) {
      mPModeInteface.updateSecurity(sv);
      setEditable(null);
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<Security> getList() {
    long l = LOG.logStart();
    List<Security> lst = mPModeInteface.getSecurities();
    LOG.logEnd(l);
    return lst;

  }

  @Override
  public String getUpdateTargetTable() {
    return ":forms:SettingsPModesSecurities:pmodeSecuritPanel:TblPModeSecurity";
  }

  @Override
  public String getSelectedDesc() {
    if (getSelected() != null) {
      return getSelected().getId();
    }
    return null;
  }

  public List<XPath> getEditableSignatureXPaths() {
    X509.Signature sg = getEditableSignature();
    if (sg != null && sg.getReference() != null && sg.getReference().
            getElements() != null) {
      return sg.getReference().getElements().getXPaths();
    }
    return Collections.emptyList();
  }

  public List<XPath> getEditableEncryptionXPaths() {
    X509.Encryption sg = getEditableEncryption();
    if (sg != null && sg.getReference() != null && sg.getReference().
            getElements() != null) {
      return sg.getReference().getElements().getXPaths();
    }
    return Collections.emptyList();
  }

  public X509.Signature getEditableSignature() {
    Security ed = getEditable();
    if (ed != null && ed.getX509() != null && ed.getX509().getSignature() != null) {
      return ed.getX509().getSignature();
    }
    return NULL_SIGNATURE;

  }

  public X509.Encryption getEditableEncryption() {
    Security ed = getEditable();
    if (ed != null && ed.getX509() != null && ed.getX509().getEncryption() != null) {
      return ed.getX509().getEncryption();
    }
    return NULL_ENCRYPTION;

  }

  public void startEditSelectedSignXPath() {

    if (selectedSignXPath != null) {
      editXPath(selectedSignXPath,
              ":dlgPModeSecurity:pModeSecurityDialog:pModeSecurityDialogForm:TblSignElements",
              getEditableSignatureXPaths());
    }
  }

  public void createSignXPath() {
    X509.Signature sg = getEditableSignature();
    if (sg != null) {
      if (sg.getReference() == null) {
        sg.setReference(new X509.Signature.Reference());
      }
      if (sg.getReference().getElements() == null) {
        sg.getReference().setElements(new References.Elements());
      }
      createXPath(":dlgPModeSecurity:pModeSecurityDialog:pModeSecurityDialogForm:TblSignElements",
              sg.getReference().getElements().getXPaths());
    }

  }

  public void setEncryptionToEditable(boolean bSuc) {
    Security ed = getEditable();
    if (ed != null) {
      if (bSuc) {
        if (ed.getX509() == null) {
          ed.setX509(new X509());
        }

        if (ed.getX509().getEncryption() == null) {
          ed.getX509().setEncryption(new X509.Encryption());
          ed.getX509().getEncryption().setAlgorithm(
                  "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512");
          ed.getX509().getEncryption().setAlgorithm(
                  "http://www.w3.org/2009/xmlenc11#aes128-gcm");

          ed.getX509().getEncryption().setMinimumStrength("128");

        }
        if (ed.getX509().getEncryption().getReference() == null) {
          ed.getX509().getEncryption().setReference(
                  new X509.Encryption.Reference());
          ed.getX509().getEncryption().getReference().setAllAttachments(true);
        }
      } else {
        if (ed.getX509() != null && ed.getX509().getEncryption() != null) {
          ed.getX509().setEncryption(null);
        }
      }

    }
  }

  public void setSignatureToEditable(boolean bSuc) {
    Security ed = getEditable();
    if (ed != null) {
      if (bSuc) {
        if (ed.getX509() == null) {
          ed.setX509(new X509());
        }

        if (ed.getX509().getSignature() == null) {
          ed.getX509().setSignature(new X509.Signature());
          ed.getX509().getSignature().setAlgorithm(
                  "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512");
          ed.getX509().getSignature().setHashFunction(
                  "http://www.w3.org/2001/04/xmlenc#sha512");
          ed.getX509().getSignature().setKeyIdentifierType("IssuerSerial");

        }
        if (ed.getX509().getSignature().getReference() == null) {
          ed.getX509().getSignature().setReference(
                  new X509.Signature.Reference());
          ed.getX509().getSignature().getReference().setAllAttachments(true);
        }
      } else {
        if (ed.getX509() != null && ed.getX509().getSignature() != null) {
          ed.getX509().setSignature(null);
        }
      }

    }
  }

  public boolean getEncryptionToEditable() {
    return getEditable() != null && getEditable().getX509() != null && getEditable().
            getX509().getEncryption() != null;
  }

  public boolean getSignatureToEditable() {
    return getEditable() != null && getEditable().getX509() != null && getEditable().
            getX509().getSignature() != null;
  }

  public void removeSelectedSignXPath() {
    if (getSelectedSignXPath() != null) {
      getEditableSignatureXPaths().remove(getSelectedSignXPath());
      setSelectedSignXPath(null);
    }
  }

  public void startEditSelectedEncXPath() {

    if (selectedEncryptXPath != null) {
      editXPath(selectedEncryptXPath, ":dlgPModeSecurity:pModeSecurityDialog:pModeSecurityDialogForm:TblEncElements",
              getEditableEncryptionXPaths());
    }
  }

  public void createEncXPath() {
    X509.Encryption sg = getEditableEncryption();
    if (sg != null) {
      if (sg.getReference() == null) {
        sg.setReference(new X509.Encryption.Reference());
      }
      if (sg.getReference().getElements() == null) {
        sg.getReference().setElements(new References.Elements());
      }
      createXPath(":dlgPModeSecurity:pModeSecurityDialog:pModeSecurityDialogForm:TblEncElements"
              , sg.getReference().getElements().getXPaths());
    }

  }

  public void removeSelectedEncXPath() {
    if (getSelectedEncryptXPath() != null) {
      getEditableEncryptionXPaths().remove(getSelectedEncryptXPath());
      setSelectedEncryptXPath(null);
    }
  }

  public void createXPath(String updateTableId, List<XPath> lstRef) {

    DialogXPath dp = (DialogXPath) getBean("dialogXPath");
    dp.createNewXPath(lstRef);
    dp.setUpdateTableId(updateTableId);
    RequestContext context = RequestContext.getCurrentInstance();
    context.execute("PF('pModeXPathDialog').show();");
    context.update(":dlgPModeXPath:pModeXPathDialog");

  }

  public void editXPath(XPath xp, String updateTableId, List<XPath> lstRef) {
    DialogXPath dp = (DialogXPath) getBean("dialogXPath");
    LOG.formatedlog("EDIT XPATH %s", xp);
    dp.setEditable(xp, lstRef);
    dp.setUpdateTableId(updateTableId);
    RequestContext context = RequestContext.getCurrentInstance();
    context.execute("PF('pModeXPathDialog').show();");
    context.update(":dlgPModeXPath:pModeXPathDialog");

  }

  public XPath getSelectedSignXPath() {
    return selectedSignXPath;
  }

  public void setSelectedSignXPath(XPath selectedSignXPath) {
    this.selectedSignXPath = selectedSignXPath;
  }

  public XPath getSelectedEncryptXPath() {
    return selectedEncryptXPath;
  }

  public void setSelectedEncryptXPath(XPath selectedEncryptXPath) {
    this.selectedEncryptXPath = selectedEncryptXPath;
  }

  public SecSignatureAlgorithm[] getSignatureAlgorithms() {
    return SecSignatureAlgorithm.values();
  }

  public SecHashFunction[] getHashFunction() {
    return SecHashFunction.values();
  }

  public SecX509KeyIdentifier[] getX509KeyIdentifier() {
    return SecX509KeyIdentifier.values();
  }

  public SecEncryptionAlgorithm[] getEncryptionAlgorithm() {
    return SecEncryptionAlgorithm.values();
  }



}
