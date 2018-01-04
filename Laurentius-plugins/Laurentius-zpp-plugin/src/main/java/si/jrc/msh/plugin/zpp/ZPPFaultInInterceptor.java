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
package si.jrc.msh.plugin.zpp;

import java.io.File;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.apache.cxf.binding.soap.SoapMessage;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.jrc.msh.plugin.zpp.doc.DocumentSodBuilder;
import si.jrc.msh.plugin.zpp.enums.FopTransformation;
import si.jrc.msh.plugin.zpp.enums.ZPPPartType;
import si.jrc.msh.plugin.zpp.exception.ZPPErrorCode;
import si.jrc.msh.plugin.zpp.exception.ZPPException;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.jrc.msh.plugin.zpp.utils.ZPPUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;

import si.laurentius.lce.KeystoreUtils;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ZPPFaultInInterceptor implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(ZPPFaultInInterceptor.class);
  SEDCrypto mSedCrypto = new SEDCrypto();
  DocumentSodBuilder dsbSodBuilder = new DocumentSodBuilder();
  KeystoreUtils mkeyUtils = new KeystoreUtils();
  StorageUtils msuStorage = new StorageUtils();
  ZPPUtils mzppZPPUtils = new ZPPUtils();

  FOPUtils mfpFop = null;
  StringFormater msfFormat = new StringFormater();

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface msedLookup;

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertBean;


  @Override
  public MailInterceptorDef getDefinition() {
    MailInterceptorDef mid = new MailInterceptorDef();
    mid.setDescription("Sets Reciept unknown advice");
    mid.setName("ZPP in fault intercepror");
    mid.setType("ZPPFaultInInterceptor");
    return mid;
  }

  /**
   *
   * @param msg
   */
  @Override
  public boolean handleMessage(SoapMessage msg, Properties contextProperties) {
    long l = LOG.logStart();
    
    
    Object sigErrors = SoapUtils.getInErrors(msg);
    if (sigErrors == null) {
      LOG.logWarn("No signal errors  - ignore soap fault", null);
      return true;
    }

    EBMSMessageContext eOutCtx = SoapUtils.getEBMSMessageOutContext(msg);
    MSHOutMail moutMail = SoapUtils.getMSHOutMail(msg);

    
    
    System.out.println("SIGN ANIES: " + sigErrors);

    if (moutMail != null) {
      if ((ZPPConstants.S_ZPP_SERVICE.equalsIgnoreCase(moutMail.getService())
              || ZPPConstants.S_ZPPB_SERVICE.equalsIgnoreCase(moutMail.
                      getService()))
              && ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION.
                      equalsIgnoreCase(
                              moutMail.getAction())
             ) {
        processInZPPFaultForOutMessage((List<org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Error>) sigErrors, moutMail,
                eOutCtx, msg);
      } else {
        LOG.formatedWarning(
                "Ignore soapfault for message: %d service %s, action %s",
                 moutMail.getId(), moutMail.getService(), moutMail.getAction());
      }
    } else {
      LOG.logWarn("Ignore soapfault for null out message", null);
    }

    return true;
  }

  /**
   *
   * @param signalErrors
   * @param outMail
   * @param eInctx
   * @param msg
   * @throws ZPPException
   */
  public void processInZPPFaultForOutMessage(List<org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Error> signalErrors,
          MSHOutMail outMail,
          EBMSMessageContext eInctx, SoapMessage msg)  {
    long l = LOG.logStart();

    for (org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Error err : signalErrors) {
      

        if (err.getErrorCode() != null && err.getErrorCode().equalsIgnoreCase(
                ZPPErrorCode.ReceiverNotExists.getCode())) {
          processReciepientNotExists(outMail, eInctx, msg);
          break;
        }
      
    }

    LOG.logEnd(l);
  }

  /**
   *
   * @param outMail
   * @param eInctx
   * @param msg
   */
  public void processReciepientNotExists(MSHOutMail outMail,
          EBMSMessageContext eoutCtx, SoapMessage msg) {
    // create in mail

    String signAlias = eoutCtx.getSenderPartyIdentitySet().
            getExchangePartySecurity().
            getSignatureCertAlias();

    MSHInMail min = createReciepientNotExistsAdvice(outMail,
            signAlias);

    try {
      mDB.serializeInMail(min, ZPPConstants.S_ZPP_PLUGIN_TYPE);
    } catch (StorageException ex) {
      String strMessage = String.format(
              "Error occured while serialize MailNotExists message Error for out mail %d ",
              outMail.
                      getId());
      LOG.logError(strMessage, ex);
      return;
    }

   

  }

  public MSHInMail createReciepientNotExistsAdvice(MSHOutMail outMail,
          String signAlias) {
    long l = LOG.logStart();
    // create delivery advice

    MSHInMail min = new MSHInMail();
    min.setMessageId(Utils.getUUIDWithLocalDomain());
    min.setService(ZPPConstants.S_ZPPB_SERVICE);
    min.setAction(ZPPConstants.S_ZPP_ACTION_ADDRESS_NOT_EXISTS);
    min.setConversationId(outMail.getConversationId());
    min.setSenderEBox(outMail.getReceiverEBox());
    min.setSenderName(outMail.getReceiverName());
    min.setRefToMessageId(outMail.getMessageId());
    min.setReceiverEBox(outMail.getSenderEBox());
    min.setReceiverName(outMail.getSenderName());
    min.setSubject(ZPPConstants.S_ZPP_ACTION_ADDRESS_NOT_EXISTS);
    // prepare mail to persist
    Date dt = new Date();
    // set current status
    min.setStatus(SEDInboxMailStatus.RECEIVED.getValue());
    min.setSubmittedDate(dt);
    min.setStatusDate(dt);
    PrivateKey pk = null;
    X509Certificate xcert = null;
    try {
      // sign with receiver certificate 
      pk = mCertBean.getPrivateKeyForAlias(signAlias);
      xcert = mCertBean.getX509CertForAlias(signAlias);
    } catch (SEDSecurityException ex) {
      String msg = String.format(
              "Server error occured while preparing sign cert %s for S_ZPP_ACTION_ADDRESS_NOT_EXISTS for mail: %d, Error: %s.",
              signAlias, outMail.getId(), ex.getMessage());
      LOG.logError(l, msg, ex);
      return null;

    }

    MSHInPart mp;
    try {
      mp = mzppZPPUtils.createMSHInPart(outMail,
              ZPPPartType.RecieptAddressNotExists,
              FopTransformation.ReceiverAddressNotExists, pk, xcert);
    } catch (SEDSecurityException | StorageException | HashException | FOPException ex) {
      String msg = String.format(
              "Server error occured while preparing sign cert %s for S_ZPP_ACTION_ADDRESS_NOT_EXISTS for mail: %d, Error: %s.",
              signAlias, outMail.getId(), ex.getMessage());
      LOG.logError(l, msg, ex);
      return null;
    }

    min.setMSHInPayload(new MSHInPayload());
    min.getMSHInPayload().getMSHInParts().add(mp);

    LOG.logEnd(l);
    return min;
  }

  

  /**
   *
   * @return
   */
  public FOPUtils getFOP() {
    if (mfpFop == null) {
      File fconf
              = new File(System.getProperty(
                      SEDSystemProperties.SYS_PROP_HOME_DIR) + File.separator
                      + ZPPConstants.SVEV_FOLDER + File.separator + ZPPConstants.FOP_CONFIG_FILENAME);

      mfpFop
              = new FOPUtils(fconf, System.getProperty(
                      SEDSystemProperties.SYS_PROP_HOME_DIR)
                      + File.separator + ZPPConstants.SVEV_FOLDER + File.separator
                      + ZPPConstants.XSLT_FOLDER);
    }
    return mfpFop;
  }

 

  /**
   *
   * @param t
   */
  @Override
  public void handleFault(SoapMessage t, Properties contextProperties) {
    // ignore
  }

}
