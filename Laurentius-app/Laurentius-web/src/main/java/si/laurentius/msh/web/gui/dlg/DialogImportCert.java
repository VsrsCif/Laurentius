package si.laurentius.msh.web.gui.dlg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.CertStatus;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.lce.KeystoreUtils;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("dialogImportCert")
public class DialogImportCert  implements Serializable{

  private static final SEDLogger LOG = new SEDLogger(DialogImportCert.class);

  public static final String DLG_TYPE_CERTSTORE = "DLG_CERTSTORE";
  public static final String DLG_TYPE_ROOT_CA_STORE = "DLG_ROOTCATORE";

  public static final String DLG_KS_TYPE_CERT = "CERT";
  public static final String DLG_KS_TYPE_JKS = "JKS";
  public static final String DLG_KS_TYPE_PKCS12 = "PKCS12";

  public static final String CB_PARA_SAVED = "saved";

  String importDialogType;

  KeystoreUtils mku = new KeystoreUtils();

  String keystoreType;
  String filename;
  String filepath;
  String password;
  List<SEDCertificate> certificates = new ArrayList<>();
  SEDCertificate selectedCertificate;
  X509Certificate selectedX509Cert;
  KeyStore importKeyStore = null;
  Map<String, String> mpPaswds = new HashMap<>();

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertStore;

  public String getKeystoreType() {
    return keystoreType;
  }

  public void setKeystoreType(String keystoreType) {
    this.keystoreType = keystoreType;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getFilepath() {
    return filepath;
  }

  public void setFilepath(String filepath) {
    this.filepath = filepath;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<SEDCertificate> getCertificates() {
    return certificates;
  }

  public void handleKeystoreUpload(FileUploadEvent event) {
    long l = LOG.logStart();

    clearImportStore();

    UploadedFile uf = event.getFile();
    filename = uf.getFileName();

    try {

      File f = File.createTempFile("certstore", ".ks");
      Files.copy(uf.getInputstream(), f.toPath(),
              StandardCopyOption.REPLACE_EXISTING);
      filepath = f.getAbsolutePath();
      ;

    } catch (IOException ex) {
      String errMsg = "Error loading certificate" + filename;
      LOG.logError(errMsg, ex);

    }

  }

  public void setDialogType(String strVal) {
    clearImportStore();
    importDialogType = strVal;
    if (!Objects.equals(strVal, DLG_TYPE_CERTSTORE)
            && !Objects.equals(strVal, DLG_TYPE_ROOT_CA_STORE)) {

      LOG.formatedWarning("Unknown dialog type %s. % is setted!", strVal,
              DLG_TYPE_CERTSTORE);
      importDialogType = DLG_TYPE_CERTSTORE;
    }
  }

  public void clearImportStore() {
    if (!Utils.isEmptyString(filepath)) {
      File f = new File(filepath);
      if (f.exists()) {
        f.delete();
      }
    }
    filepath = null;
    filename = null;
    keystoreType = "JKS";
    certificates.clear();
    selectedCertificate = null;
    selectedX509Cert = null;

  }

  public SEDCertificate getSelectedCertificate() {
    return selectedCertificate;
  }

  public void setSelectedCertificate(SEDCertificate selectedCertificate) {
    this.selectedCertificate = selectedCertificate;
  }

  public X509Certificate getSelectedX509Cert() {
    return selectedX509Cert;
  }

  public void setSelectedX509Cert(X509Certificate selectedX509Cert) {
    this.selectedX509Cert = selectedX509Cert;
  }

  /**
   *
   */
  public void openImportKeystore() {

    long l = LOG.logStart();
    if (Utils.isEmptyString(getFilepath())) {
      String strmsg = "Select file to import!";
      addError(strmsg);
      LOG.log(strmsg);
      return;
    }

    getCertificates().clear();
    importKeyStore = null;

    if (Objects.equals("CERT", getKeystoreType())) {

      try (FileInputStream fis = new FileInputStream(getFilepath())) {
        try {
          selectedX509Cert = mku.getCertFromInputStream(fis);
          SEDCertificate ec = new SEDCertificate();
          String subjectDN = selectedX509Cert.getSubjectDN().getName();
          String alias = getFirstCNValueFromDN(subjectDN);

          ec.setValidFrom(selectedX509Cert.getNotBefore());
          ec.setValidTo(selectedX509Cert.getNotAfter());
          ec.setIssuerDN(selectedX509Cert.getIssuerDN().getName());
          ec.setSubjectDN(subjectDN);
          ec.setAlias(alias);

          ec.setSerialNumber(selectedX509Cert.getSerialNumber() + "");
          ec.setHexSHA1Digest(DigestUtils.getHexSha1Digest(selectedX509Cert.
                  getEncoded()));

         
          try {
            selectedX509Cert.checkValidity();
          } catch (CertificateExpiredException | CertificateNotYetValidException ex) {
            ec.setStatus(CertStatus.INVALID_BY_DATE.getCode());
          }
          
          getCertificates().add(ec);
          selectedCertificate = ec;

        } catch (CertificateEncodingException | SEDSecurityException ex) {
          String strmsg = "Error reading cert: " + ex.getMessage();
          addError(strmsg);
        }
      } catch (IOException ex) {
        String strmsg = "Error reading file: " + getFilename();
        addError(strmsg);
      }

    } else {
      try {
        if (Utils.isEmptyString(getPassword())) {
          addError("Missing keystore password");
        }

        importKeyStore
                = mku.openKeyStore(getFilepath(), getKeystoreType(),
                        getPassword().toCharArray());

        List<SEDCertificate> lstals = mku.getKeyStoreSEDCertificates(
                importKeyStore);
        getCertificates().addAll(lstals);

      } catch (SEDSecurityException ex) {
        String msg = String.format(
                "Error occured while opening keystore %s. Check password in keystore type!",
                getFilename());
        addError(msg);
        LOG.logWarn(l, msg + " Filepath: " + getFilepath(), ex);
      }
    }

  }

  public String getFirstCNValueFromDN(String dn) {
    String res = null;
    try {
      LdapName ldapDN = new LdapName(dn);

      for (Rdn rdn : ldapDN.getRdns()) {
        if (rdn.getType().equalsIgnoreCase("cn")) {
          res = rdn.getValue().toString();
          break;
        }
      }
    } catch (InvalidNameException ex) {
      LOG.formatedWarning("Invalid cn %s. error: %s", dn, ex.getMessage());
    }
    return res == null ? (dn.length() > 0 ? dn.substring(0, 10) : dn) : res;
  }

  public void importStoreRowSelectionChanged() {
    selectedX509Cert = null;
    if (this.selectedCertificate != null && importKeyStore != null) {
      try {
        selectedX509Cert = mku.getTrustedCertForAlias(importKeyStore,
                selectedCertificate.getAlias());

      } catch (SEDSecurityException ex) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR, "Import error", ex.getMessage()));

      }
    } else {
      selectedCertificate = null;
    }

  }

  public void importFile() {

    switch (getKeystoreType()) {
      case DLG_KS_TYPE_CERT:
        importCertificate();
        break;
      case DLG_KS_TYPE_PKCS12:
      case DLG_KS_TYPE_JKS:
        if (Objects.equals(importDialogType, DLG_TYPE_CERTSTORE)) {
          importKeysAndCertsToCertStore();
        } else if (Objects.equals(importDialogType, DLG_TYPE_ROOT_CA_STORE)) {
          importJKSCertsToRootCAStore();
        } else {
          String strMsg = String.format("Invalid dialog type %s",
                  importDialogType);
          LOG.logWarn(strMsg, null);
          setCBParamSaved(false);
        }
        break;
    }

  }

  public void importCertificate() {

    try {
      if (Objects.equals(importDialogType, DLG_TYPE_CERTSTORE)) {
        mCertStore.addCertToCertStore(selectedX509Cert, selectedCertificate.
                getAlias());
        setCBParamSaved(true);
      } else if (Objects.equals(importDialogType, DLG_TYPE_ROOT_CA_STORE)) {
        mCertStore.addCertToRootCA(selectedX509Cert, selectedCertificate.
                getAlias());
        setCBParamSaved(true);
      } else {
        String strMsg = String.format("Invalid dialog type %s",
                importDialogType);
        LOG.logWarn(strMsg, null);
        setCBParamSaved(false);
      }
    } catch (SEDSecurityException ex) {
      String strMsg = String.format(
              "Error occured while adding cert %s, err: %s.",
              selectedCertificate.getAlias(), ex.getMessage());

      LOG.logWarn(strMsg, ex);
      addError(strMsg);
      setCBParamSaved(false);
    }
  }

  public void importJKSCertsToRootCAStore() {
    //check if all passwords are given
    int iImpCout = 0;
    for (SEDCertificate sc : getCertificates()) {
      if (!sc.isImport()) {
        continue;
      }
      iImpCout++;
    }

    if (iImpCout == 0) {
      addError("No certificate selected to import!");
      setCBParamSaved(false);
      return;
    }

    // import certificates and keys
    for (SEDCertificate sc : getCertificates()) {
      if (!sc.isImport()) {
        continue;
      }
      try {
        X509Certificate c = mku.getTrustedCertForAlias(importKeyStore, sc.
                getAlias());
        mCertStore.addCertToRootCA(c, sc.getAlias());

      } catch (SEDSecurityException ex) {
        String msg = String.format("Error importing  %s. Err: %s", sc.
                getAlias(),
                ex.getMessage());
        addError(msg);
        LOG.logWarn(msg, ex);
        setCBParamSaved(false);
        return;
      }

    }
    setCBParamSaved(true);
  }

  public void importKeysAndCertsToCertStore() {
    LOG.formatedDebug("importKeysAndCertsToCertStore");
    //check if all passwords are given
  
    int iImpCout = 0;
    for (SEDCertificate sc : getCertificates()) {
      if (!sc.isImport()) {
        continue;
      }
      iImpCout++;
      if (sc.isKeyEntry()) {
        if (!Objects.equals(getKeystoreType(), DLG_KS_TYPE_PKCS12) && Utils.
                isEmptyString(sc.getPassword())) {
          addError("Enter password for key: '"
                  + sc.getAlias() + "'.");
          setCBParamSaved(false);
          return;
        } else if (Objects.equals(getKeystoreType(), DLG_KS_TYPE_PKCS12)) {
          try {
            LOG.formatedWarning("Test access to key %s password %s",  sc.getAlias(), sc.
                    getPassword());
            boolean bac = mku.testAccessToKey(importKeyStore, sc.getAlias(), sc.
                    getPassword());
            if (!bac) {
              addError("Invalid password for key: '"
                      + sc.getAlias() + "'.");
              setCBParamSaved(false);
              return;
            }
          } catch (SEDSecurityException ex) {
            addError("Invalid password for key: '"
                    + sc.getAlias() + "'.");
            setCBParamSaved(false);
            return;
          }
        } else {
          LOG.formatedDebug("Certificate  %s ready to import!", sc.getAlias());
        }

      }
    }
    LOG.formatedDebug("Add new %d certifikactes ", iImpCout);
    if (iImpCout == 0) {
      addError("No certificate selected to import!");
      setCBParamSaved(false);
      return;
    }

    // import certificates and keys
    for (SEDCertificate sc : getCertificates()) {
      if (!sc.isImport()) {
        continue;
      }
      try {
        if (sc.isKeyEntry()) {
          Key k = importKeyStore.getKey(sc.getAlias(), sc.getPassword().
                  toCharArray());
          Certificate[] crts = importKeyStore.getCertificateChain(sc.getAlias());
          mCertStore.addKeyToToCertStore(sc.getAlias(), k, crts, sc.
                  getPassword());

        } else {
          X509Certificate c = mku.getTrustedCertForAlias(importKeyStore, sc.
                  getAlias());
          mCertStore.addCertToCertStore(c, sc.getAlias());
        }
      } catch (SEDSecurityException | KeyStoreException | NoSuchAlgorithmException
              | UnrecoverableKeyException ex) {
        String msg = String.format("Error importing  %s. Err: %s", sc.
                getAlias(),
                ex.getMessage());
        addError(msg);
        LOG.logWarn(msg, ex);
        setCBParamSaved(false);
        return;
      }
    }
    LOG.formatedDebug("Successfully added new  %d certifikactes ", iImpCout);
    setCBParamSaved(true);

  }

  public boolean showKeyPasswords() {
    return Objects.equals(DLG_TYPE_CERTSTORE, importDialogType);
  }

  protected FacesContext facesContext() {
    return FacesContext.getCurrentInstance();
  }

  protected void addError(String desc) {
    facesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    desc));
  }

  public void setCBParamSaved(boolean bval) {
    RequestContext.getCurrentInstance().addCallbackParam(CB_PARA_SAVED, bval);
  }

  public String getUpdateTargetPanel() {

    if (Objects.equals(importDialogType, DLG_TYPE_CERTSTORE)) {
      return ":forms:PanelKeystore:certPanel:keylist";
    } else if (Objects.equals(importDialogType, DLG_TYPE_ROOT_CA_STORE)) {
      return ":forms:PanelRootCA:rootCAView:RootCAlist";
    } else {
      LOG.formatedWarning("Invalid dialog type %s",
              importDialogType);
      return ":forms:PanelKeystore:certPanel:keylist";
    }
  }
}
