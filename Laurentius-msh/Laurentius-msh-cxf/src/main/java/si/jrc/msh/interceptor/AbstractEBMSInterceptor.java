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

import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
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
import si.laurentius.commons.ebms.EBMSError;
import si.jrc.msh.exception.EBMSErrorCode;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.interfaces.SEDCertUtilsInterface;

/**
 * Abstract class extends from AbstractSoapInterceptor with access to apliation
 * EJB resources as: - ejb reference to signleton application settings - ejb
 * reference to signleton application lookups - ejb reference to DAO services.
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
    // ejb reference to cert store 
    SEDCertUtilsInterface mCertUtils;

    // ejb reference to PModeManager services
    protected PModeInterface mPMode;

    protected static SEDLogger A_LOG = new SEDLogger(AbstractEBMSInterceptor.class);

    /**
     * Constructor.
     *
     * @param p - CXF bus Phase. Values are defined in
     * org.apache.cxf.phase.Phase
     */
    public AbstractEBMSInterceptor(String p) {
        super(p);
    }

    /**
     * constructor
     *
     * @param i - Instantiates the interceptor with a specified id.
     * @param p - CXF bus Phase. Values are defined in
     * org.apache.cxf.phase.Phase
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
    public SEDCertUtilsInterface getCertUtilsStore() {
        long l = A_LOG.logStart();
        if (mCertUtils == null) {
            try {
                mCertUtils = InitialContext.doLookup(SEDJNDI.JNDI_DBCERTUTILS);
                A_LOG.logEnd(l);
            } catch (NamingException ex) {
                A_LOG.logError(l, ex);
            }
        }
        return mCertUtils;
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
            PartyIdentitySetType.LocalPartySecurity lps,
            PartyIdentitySetType.ExchangePartySecurity epx,
            String msgId, QName sv)
            throws EBMSError {
        long l = A_LOG.logStart();
        WSS4JOutInterceptor sec = null;
        Map<String, Object> outProps = null;

        if (sc.getX509() == null) {
            A_LOG.logWarn(l,
                    "Sending not message with not security policy. No security configuration (pmode) for message:"
                    + msgId, null);
            return null;
        }

        if (sc.getX509().getSignature() != null && sc.getX509().getSignature().
                getReference() != null) {
            X509.Signature sig = sc.getX509().getSignature();

            String sigAlias = lps.getSignatureKeyAlias();

            try {
                A_LOG.formatedlog("Validate message: '%s' signed width: %s", msgId, sigAlias);
                outProps = getCertUtilsStore().createCXFSignatureConfiguration(sig,
                        sigAlias);
            } catch (SEDSecurityException ex) {
                String msg = "Error occurred while creating signature configuration for message: " + msgId + " and cert alias '" + sigAlias + "'! Error: " + ex.
                        getMessage();
                A_LOG.logError(l, msg, ex);
                throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg,
                        sv);
            }
            if (outProps == null) {
                A_LOG.logWarn(l,
                        "Sending not signed message. Incomplete configuration: X509/Signature for message:  "
                        + msgId, null);
            }
        } else {
            A_LOG.logWarn(l,
                    "Sending not signed message. No configuration: X509/Signature/Sign for message:  " + msgId,
                    null);
        }

        if (sc.getX509().getEncryption() != null && sc.getX509().getEncryption().
                getReference() != null) {
            X509.Encryption enc = sc.getX509().getEncryption();

            String encAlias = epx.getEncryptionCertAlias();

            Map<String, Object> penc;
            try {
                penc = getCertUtilsStore().
                        createCXFEncryptionConfiguration(enc, encAlias);
            } catch (SEDSecurityException ex) {
                throw new EBMSError(EBMSErrorCode.PolicyNoncompliance, msgId, ex.getMessage(),
                        sv);
            }

            if (enc == null) {
                A_LOG.logWarn(l,
                        "Sending not encrypted message. Incomplete configuration: X509/Encryption/Encryp for message:  "
                        + msgId, null);
            } else if (outProps == null) {
                outProps = penc;
            } else {
                String action = (String) outProps.get(WSHandlerConstants.ACTION);
                action += " " + (String) penc.get(WSHandlerConstants.ACTION);
                outProps.putAll(penc);
                outProps.put(WSHandlerConstants.ACTION, action);
            }
        } else {
            A_LOG.logWarn(l,
                    "Sending not encrypted message. No configuration: X509/Encryption/Encrypt for message:  "
                    + msgId, null);
        }

        if (outProps != null) {

            A_LOG.formatedlog("Message %s security properties: %s", msgId, getLogSecurityProperitesStringForMail(outProps));
            sec = new WSS4JOutInterceptor(outProps);

        } else {
            A_LOG.logWarn(l,
                    "Sending not message with not security policy. Bad/incomplete security configuration (pmode) for message:"
                    + msgId, null);
        }
        A_LOG.logEnd(l);
        return sec;
    }

    public WSS4JInInterceptor configureInSecurityInterceptors(Security sc,
            PartyIdentitySetType.LocalPartySecurity lps,
            PartyIdentitySetType.ExchangePartySecurity eps,
            String msgId, QName sv)
            throws EBMSError {

        long l = A_LOG.logStart();
        WSS4JInInterceptor sec = null;
        Map<String, Object> inProps = null;

        if (sc.getX509() == null) {
            A_LOG.logWarn(l,
                    "Sending not message with not security policy. No security configuration (pmode) for message:"
                    + msgId, null);
            return null;
        }
        if (sc.getX509().getSignature() != null && sc.getX509().getSignature().
                getReference() != null) {
            X509.Signature sig = sc.getX509().getSignature();
            String sigAliasProp = eps.getSignatureCertAlias();

            try {
                inProps = getCertUtilsStore().createCXFSignatureValidationConfiguration(sig, sigAliasProp);
            } catch (SEDSecurityException ex) {
                throw new EBMSError(EBMSErrorCode.PolicyNoncompliance, msgId, ex.getMessage(),
                        sv);
            }
            if (inProps == null) {
                A_LOG.logWarn(l,
                        "Sending not signed message. Incomplete configuration: X509/Signature for message:  "
                        + msgId, null);
            }
        } else {
            A_LOG.logWarn(l,
                    "Sending not signed message. No configuration: X509/Signature/Sign for message:  " + msgId,
                    null);
        }

        if (sc.getX509().getEncryption() != null && sc.getX509().getEncryption().
                getReference() != null) {
            X509.Encryption enc = sc.getX509().getEncryption();
            String decAlias = lps.getDecryptionKeyAlias();

            Map<String, Object> penc = null;
            try {
                penc = getCertUtilsStore().createCXFDecryptionConfiguration(enc, decAlias);
            } catch (SEDSecurityException ex) {
                String msg = "Error occured while creating CXFDecryptionConfiguration alias '" + decAlias
                        + "' do not exist in keystore. Error:" + ex.getMessage();
                A_LOG.logError(l, msg, ex);
                throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, msg,
                        sv);
            }

            if (enc == null) {
                A_LOG.logWarn(l,
                        "Sending not encrypted message. Incomplete configuration: X509/Encryption/Encryp for message:  "
                        + msgId, null);
            } else if (inProps == null) {
                inProps = penc;
            } else {
                String action = (String) inProps.get(WSHandlerConstants.ACTION);
                action += " " + (String) penc.get(WSHandlerConstants.ACTION);
                inProps.putAll(penc);
                inProps.put(WSHandlerConstants.ACTION, action);
            }
        } else {
            A_LOG.logWarn(l,
                    "Sending not encypted message. No configuration: X509/Encryption/Encrypt for message:  "
                    + msgId, null);
        }

        if (inProps != null) {
            A_LOG.formatedlog("Message %s security properties: %s", msgId, getLogSecurityProperitesStringForMail(inProps));
            sec = new WSS4JInInterceptor(inProps);
        } else {
            A_LOG.logWarn(l,
                    "Sending not message with not security policy. Bad/incomplete security configuration (pmode) for message:"
                    + msgId, null);
        }

        A_LOG.logEnd(l);
        return sec;
    }

    private String getLogSecurityProperitesStringForMail(Map<String, Object> prop) {
        StringWriter sw = new StringWriter();
        sw.append('[');
        for (String key : prop.keySet()) {
            sw.append(key);
            sw.append('=');
            Object o = prop.getOrDefault(key, "null");
            if (o instanceof Properties) {
                Properties prp = (Properties) o;
                sw.append('[');
                for (String prpKey : prp.stringPropertyNames()) {
                    sw.append("\n\t");
                    sw.append(prpKey);
                    sw.append('=');
                    if (prpKey.contains("pass")) {
                        sw.append("****");
                    } else {
                        sw.append(prp.getProperty(prpKey, "null"));
                    }
                }
                sw.append("\n]\n");
            } else {
                if (!key.contains("pass")) {
                    sw.append(o.toString());
                } else {
                    sw.append("****");
                }
                sw.append(";\n");
            }
        }
        sw.append(']');
        return sw.toString();
    }
}
