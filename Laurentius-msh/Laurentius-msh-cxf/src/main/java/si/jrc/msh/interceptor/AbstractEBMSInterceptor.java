/*
 * Copyright 2015, Supreme Court Republic of Slovenia
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
package si.jrc.msh.interceptor;

import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import si.laurentius.msh.pmode.PartyIdentitySetType;
import si.laurentius.msh.pmode.Security;
import si.laurentius.msh.pmode.X509;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.jrc.msh.client.sec.SecurityUtils;
import si.jrc.msh.exception.EBMSError;
import si.jrc.msh.exception.EBMSErrorCode;
import static si.jrc.msh.interceptor.EBMSOutInterceptor.LOG;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;

/**
 * Abstract class extends from AbstractSoapInterceptor with access to apliation EJB resources as: -
 * ejb reference to signleton application settings - ejb reference to signleton application lookups
 * - ejb reference to DAO services.
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public abstract class AbstractEBMSInterceptor extends AbstractSoapInterceptor {

  String LOADED_CLASSES = "hibernate.ejb.loaded.classes";
  // ejb reference to signleton application settings 
  protected DBSettingsInterface mDBSettings;
  // ejb reference to signleton application lookups 
  protected SEDLookupsInterface mSedLookups;
  // ejb reference to DAO services
  protected SEDDaoInterface mSedDao;
  
  
  SEDCertStoreInterface mCertBean;

  // ejb reference to PModeManager services
  protected PModeInterface mPMode;

  protected static SEDLogger A_LOG = new SEDLogger(AbstractEBMSInterceptor.class);

  /**
   * Constructor.
   *
   * @param p - CXF bus Phase. Values are defined in org.apache.cxf.phase.Phase
   */
  public AbstractEBMSInterceptor(String p) {
    super(p);
  }

  /**
   * constructor
   *
   * @param i - Instantiates the interceptor with a specified id.
   * @param p - CXF bus Phase. Values are defined in org.apache.cxf.phase.Phase
   */
  public AbstractEBMSInterceptor(String i, String p) {
    super(i, p);

  }

  /**
   * Methods lookups SEDDaoInterface.
   *
   * @return SEDDaoInterface or null if bad application configuration.
   */
  public SEDDaoInterface getDAO() {
    long l = A_LOG.logStart();
    if (mSedDao == null) {
      try {
        mSedDao = InitialContext.doLookup(SEDJNDI.JNDI_SEDDAO);
        A_LOG.logEnd(l);
      } catch (NamingException ex) {
        A_LOG.logError(l, ex);
      }
    }

    return mSedDao;
  }

  /**
   * Methods lookups SEDLookupsInterface.
   *
   * @return SEDLookupsInterface or null if bad application configuration.
   */
  public SEDLookupsInterface getLookups() {
    long l = A_LOG.logStart();
    if (mSedLookups == null) {
      try {
        mSedLookups = InitialContext.doLookup(SEDJNDI.JNDI_SEDLOOKUPS);
        A_LOG.logEnd(l);
      } catch (NamingException ex) {
        A_LOG.logError(l, ex);
      }
    }

    return mSedLookups;
  }
  


  public DBSettingsInterface getSettings() {
    long l = A_LOG.logStart();
    if (mDBSettings == null) {
      try {
        mDBSettings = InitialContext.doLookup(SEDJNDI.JNDI_DBSETTINGS);
        A_LOG.logEnd(l);
      } catch (NamingException ex) {
        A_LOG.logError(l, ex);
      }
    }

    return mDBSettings;
  }

  /**
   * Methods lookups DBSettingsInterface.
   *
   * @return DBSettingsInterface or null if bad application configuration.
   */
  public SEDCertStoreInterface getCertStore() {
    long l = A_LOG.logStart();
    if (mCertBean == null) {
      try {
        mCertBean = InitialContext.doLookup(SEDJNDI.JNDI_DBCERTSTORE);
        A_LOG.logEnd(l);
      } catch (NamingException ex) {
        A_LOG.logError(l, ex);
      }
    }
    return mCertBean;
  }

  /**
   * Methods lookups PModeInterface.
   *
   * @return PModeInterface or null if bad application configuration.
   */
  public PModeInterface getPModeManager() {
    long l = A_LOG.logStart();
    if (mPMode == null) {
      try {
        mPMode = InitialContext.doLookup(SEDJNDI.JNDI_PMODE);
        A_LOG.logEnd(l);
      } catch (NamingException ex) {
        A_LOG.logError(l, ex);
      }
    }
    return mPMode;
  }

  /**
   * Abstract method for handling SoapMessage.
   *
   * @param t - soap messsage
   */
  @Override
  public abstract void handleMessage(SoapMessage t)
      throws Fault;

  public WSS4JOutInterceptor configureOutSecurityInterceptors(Security sc,
      PartyIdentitySetType.LocalPartySecurity lps, PartyIdentitySetType.ExchangePartySecurity epx,
      String msgId, QName sv)
      throws EBMSError {
    long l = LOG.logStart();
    WSS4JOutInterceptor sec = null;
    Map<String, Object> outProps = null;

    if (sc.getX509() == null) {
      LOG.logWarn(l,
          "Sending not message with not security policy. No security configuration (pmode) for message:" +
          msgId, null);
      return null;
    }

    if (sc.getX509().getSignature() != null && sc.getX509().getSignature().getReference() != null) {
      X509.Signature sig = sc.getX509().getSignature();

      SEDCertStore cs;
      try {
        cs = getCertStore().getCertificateStore();
      } catch (SEDSecurityException ex) {
        String msg = "Error opening keystore - check configuration!";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }
      
      if (cs == null) {
        String msg = "Keystore do not exists - check configuration!";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }

      SEDCertificate aliasCrt =
          getLookups().getSEDCertificatForAlias(lps.getSignatureKeyAlias(), cs, true);
      if (aliasCrt == null) {
        String msg = "Key for alias '" + lps.getSignatureKeyAlias() + "' do not exists!";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }

      if (!KeystoreUtils.isCertValid(aliasCrt)) {
        String msg = "Key for alias '" + lps.getSignatureKeyAlias() + " is not valid!";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }

      outProps =
          SecurityUtils.createSignatureConfiguration(sig, cs, aliasCrt);
      if (outProps == null) {
        LOG.logWarn(l,
            "Sending not signed message. Incomplete configuration: X509/Signature for message:  " +
            msgId, null);
      }
    } else {
      LOG.logWarn(l,
          "Sending not signed message. No configuration: X509/Signature/Sign for message:  " + msgId,
          null);
    }

    if (sc.getX509().getEncryption() != null && sc.getX509().getEncryption().getReference() != null) {
      X509.Encryption enc = sc.getX509().getEncryption();

      
      SEDCertStore cs;
      try {
        cs = getCertStore().getCertificateStore();
      } catch (SEDSecurityException ex) {
        String msg = "Error opening keystore - check configuration!";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }
      if (cs == null) {
        String msg = "Trustore  do not exists - check configuration!";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }
      SEDCertificate aliasCrt = getLookups().getSEDCertificatForAlias(epx.getEncryptionCertAlias(),
          cs, false);
      if (aliasCrt == null) {
        String msg = "Ecryptiong cert for alias '" + epx.getEncryptionCertAlias() +
            "' do not exists!";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }

      Map<String, Object> penc = SecurityUtils.createEncryptionConfiguration(enc, cs, aliasCrt);
      if (enc == null) {
        LOG.logWarn(l,
            "Sending not encrypted message. Incomplete configuration: X509/Encryption/Encryp for message:  " +
            msgId, null);
      } else if (outProps == null) {
        outProps = penc;
      } else {
        String action = (String) outProps.get(WSHandlerConstants.ACTION);
        action += " " + (String) penc.get(WSHandlerConstants.ACTION);
        outProps.putAll(penc);
        outProps.put(WSHandlerConstants.ACTION, action);
      }
    } else {
      LOG.logWarn(l,
          "Sending not encrypted message. No configuration: X509/Encryption/Encrypt for message:  " +
          msgId, null);
    }

    if (outProps != null) {
      sec = new WSS4JOutInterceptor(outProps);
    } else {
      LOG.logWarn(l,
          "Sending not message with not security policy. Bad/incomplete security configuration (pmode) for message:" +
          msgId, null);
    }
    LOG.logEnd(l);
    return sec;
  }

  public WSS4JInInterceptor configureInSecurityInterceptors(Security sc,
      PartyIdentitySetType.LocalPartySecurity lps, PartyIdentitySetType.ExchangePartySecurity eps,
      String msgId, QName sv)
      throws EBMSError {

    long l = LOG.logStart();
    WSS4JInInterceptor sec = null;
    Map<String, Object> outProps = null;

    if (sc.getX509() == null) {
      LOG.logWarn(l,
          "Sending not message with not security policy. No security configuration (pmode) for message:" +
          msgId, null);
      return null;
    }
    if (sc.getX509().getSignature() != null && sc.getX509().getSignature().getReference() != null) {
      X509.Signature sig = sc.getX509().getSignature();

      // create signature priperties
      SEDCertStore cs;
      try {
        cs = getCertStore().getCertificateStore();
      } catch (SEDSecurityException ex) {
        String msg = "Error opening keystore - check configuration!";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }
      if (cs == null) {
        String msg = "Truststore for name  do not exists - check configuration!";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }
      SEDCertificate aliasCrt = getLookups().getSEDCertificatForAlias(eps.getSignatureCertAlias(),
          cs, false);
      if (aliasCrt == null) {
        String msg = "Certificate for alias '" + eps.getSignatureCertAlias() +
            "' do not exists in keystore.";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }
      outProps = SecurityUtils.createSignatureValidationConfiguration(sig, cs, aliasCrt);
      if (outProps == null) {
        LOG.logWarn(l,
            "Sending not signed message. Incomplete configuration: X509/Signature for message:  " +
            msgId, null);
      }
    } else {
      LOG.logWarn(l,
          "Sending not signed message. No configuration: X509/Signature/Sign for message:  " + msgId,
          null);
    }

    if (sc.getX509().getEncryption() != null && sc.getX509().getEncryption().getReference() != null) {
      X509.Encryption enc = sc.getX509().getEncryption();
      SEDCertStore cs;
      try {
        cs = getCertStore().getCertificateStore();
      } catch (SEDSecurityException ex) {
        String msg = "Error opening keystore - check configuration!";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }
      if (cs == null) {
        String msg = "Keystore for name  do not exists - check configuration!";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }
      SEDCertificate aliasCrt = getLookups().getSEDCertificatForAlias(lps.getDecryptionKeyAlias(),
          cs, true);
      if (aliasCrt == null) {
        String msg = "Decryptiong key for alias '" + lps.getDecryptionKeyAlias() +
            "' do not exist in keystore.";
        LOG.logError(l, msg, null);
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg, sv);
      }

      Map<String, Object> penc =
          SecurityUtils.createDecryptionConfiguration(enc, cs, aliasCrt);
      if (enc == null) {
        LOG.logWarn(l,
            "Sending not encrypted message. Incomplete configuration: X509/Encryption/Encryp for message:  " +
            msgId, null);
      } else if (outProps == null) {
        outProps = penc;
      } else {
        String action = (String) outProps.get(WSHandlerConstants.ACTION);
        action += " " + (String) penc.get(WSHandlerConstants.ACTION);
        outProps.putAll(penc);
        outProps.put(WSHandlerConstants.ACTION, action);
      }
    } else {
      LOG.logWarn(l,
          "Sending not encypted message. No configuration: X509/Encryption/Encrypt for message:  " +
          msgId, null);
    }

    if (outProps != null) {
      sec = new WSS4JInInterceptor(outProps);
    } else {
      LOG.logWarn(l,
          "Sending not message with not security policy. Bad/incomplete security configuration (pmode) for message:" +
          msgId, null);
    }
    LOG.logEnd(l);
    return sec;
  }
}
