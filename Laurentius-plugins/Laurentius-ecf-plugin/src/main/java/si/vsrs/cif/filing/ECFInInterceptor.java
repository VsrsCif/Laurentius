/*
 * Copyright 2018, Supreme Court Republic of Slovenia
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
package si.vsrs.cif.filing;

import java.io.File;
import java.util.Objects;
import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.msh.inbox.payload.IMPartProperty;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;
import si.vsrs.cif.filing.enums.ECFAction;
import si.vsrs.cif.filing.enums.ECFService;
import si.vsrs.cif.filing.exception.ECFFault;
import si.vsrs.cif.filing.exception.ECFFaultCode;
import si.vsrs.cif.filing.utils.PDFUtils;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ECFInInterceptor implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(ECFInInterceptor.class);

  StorageUtils storageUtils = new StorageUtils();
  PDFUtils mPDFUtils = new PDFUtils();

  /**
   *
   */
  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mdao;

  @Override
  public MailInterceptorDef getDefinition() {
    MailInterceptorDef def = new MailInterceptorDef();
    def.setType(ECFInInterceptor.class.getSimpleName());
    def.setName(ECFInInterceptor.class.getSimpleName());
    def.setDescription(
            "e-court filing inteceptor");
    return def;
  }

  /**
   *
   * @param msg
   * @param cp
   */
  @Override
  public boolean handleMessage(SoapMessage msg, Properties cp) {
    long l = LOG.logStart();
    boolean isBackChannel = SoapUtils.isRequestMessage(msg);
    MSHInMail mInMail = SoapUtils.getMSHInMail(msg);

    if (isBackChannel || mInMail == null) {
      // ignore processsing
      return true;
    }
    if (!ECFService.CourtFiling.getService().equals(mInMail.getService())) {
      LOG.formatedWarning(
              "Message ConversationId: %s from %s has wrong service: '%s' (expected: %s) for this plugin!",
              mInMail.getConversationId(), mInMail.getSenderEBox(), mInMail.
              getService(), ECFService.CourtFiling.getService());

    }

    if (!ECFAction.ServeFiling.getValue().equals(mInMail.getAction())) {
      LOG.formatedWarning(
              "Message ConversationId: %s from %s has wrong action: '%s' (expected: %s) for this plugin!",
              mInMail.getConversationId(), mInMail.getSenderEBox(), mInMail.
              getAction(), ECFAction.ServeFiling.getValue());
    }

    if (mInMail.getMSHInPayload() == null || mInMail.getMSHInPayload().
            getMSHInParts().isEmpty()) {
      LOG.formatedWarning(
              "Message ConversationId: %s from %s does not have payload!",
              mInMail.getConversationId(), mInMail.getSenderEBox());
    }

    for (MSHInPart mip : mInMail.getMSHInPayload().getMSHInParts()) {
      if (SEDMailPartSource.MAIL.getValue().equals(mip.getSource())) {
        if (!MimeValue.MIME_PDF.getMimeType().
                equalsIgnoreCase(mip.getMimeType())) {
          String msgError = String.format(
                  "Message ConversationId: %s from %s has wrong payload mimetype %s!",
                  mInMail.getConversationId(), mInMail.getSenderEBox(), mip.
                  getMimeType());
          LOG.logWarn(msgError, null);
          throw new ECFFault(ECFFaultCode.Other, mInMail.getMessageId(),
                  msgError,
                  SoapFault.FAULT_CODE_SERVER);
        }

        //--------------------------------------------------------
        // valdiate for pdf/a
        File fPDF = StorageUtils.getFile(mip.getFilepath());
        String res = mPDFUtils.testPDFA(fPDF);
        if (res != null) {
          String msgError = String.format(
                  "Message ConversationId: %s from %s payload is not pdf/a compliant: %s!",
                  mInMail.getConversationId(), mInMail.getSenderEBox(), res);
          LOG.logWarn(msgError, null);
          throw new ECFFault(ECFFaultCode.Other, mInMail.getMessageId(),
                  msgError,
                  SoapFault.FAULT_CODE_SERVER);

        }
      }
    }

    
    // test if pdf is signed
    
  // test if payload is signed PDF
  // test if mail is PDF/a

return true;
  }

  

  public String getPartProperty(MSHInPart part, String propertytype) {
    if (part == null || part.getIMPartProperties().isEmpty()) {
      return null;
    }
    for (IMPartProperty p : part.getIMPartProperties()) {
      if (Objects.equals(p.getName(), propertytype)) {
        return p.getValue();
      }
    }
    return null;
  }

  /**
   *
   * @param t
   */
  @Override
        public void handleFault(SoapMessage t, Properties cp) {
    // ignore
  }

}
