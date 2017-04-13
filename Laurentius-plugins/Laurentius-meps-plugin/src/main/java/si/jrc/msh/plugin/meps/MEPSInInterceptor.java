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
package si.jrc.msh.plugin.meps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import si.jrc.msh.plugin.meps.enums.MEPSActions;
import si.jrc.msh.plugin.meps.exception.MEPSFault;
import si.jrc.msh.plugin.meps.exception.MEPSFaultCode;
import si.jrc.msh.plugin.meps.pdf.PDFContentData;
import si.jrc.msh.plugin.meps.pdf.PDFException;
import si.jrc.msh.plugin.meps.pdf.PDFUtil;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class MEPSInInterceptor implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(MEPSInInterceptor.class);

 
  PDFUtil pdfUtils = new PDFUtil();
  StorageUtils storageUtils = new StorageUtils();
  /**
   *
   */
  @Resource
  public UserTransaction mutUTransaction;

  @Override
  public MailInterceptorDef getDefinition() {
    MailInterceptorDef def = new MailInterceptorDef();
    def.setType("MEPSProviderInInterceptor");
    def.setName("MEPSProviderInInterceptor");
    def.setDescription("MEPS In mail interceptor ");
    return def;
  }

  /**
   *
   * @param msg
   */
  @Override
  public boolean handleMessage(SoapMessage msg) {
    long l = LOG.logStart();
    boolean isBackChannel = SoapUtils.isRequestMessage(msg);
    MSHInMail mInMail = SoapUtils.getMSHInMail(msg);

    if (!isBackChannel) {
      if (mInMail != null && MEPSActions.ADD_MAIL.getValue().equals(mInMail.
              getAction())) {
        processAddMail(mInMail);

      }
    }

    return true;
  }

  public void processAddMail(MSHInMail mInMail) {
    // test data

    if (mInMail.getMSHInPayload() == null || mInMail.getMSHInPayload().
            getMSHInParts().size() < 2) {
      throw new MEPSFault(MEPSFaultCode.Other, null,
              "Mail must have at least two attachmetns (Envelope data and content pdf)!",
              SoapFault.FAULT_CODE_CLIENT);

    }

    File envData = null;
    List<File> contentList = new ArrayList<>();

    for (MSHInPart mp : mInMail.getMSHInPayload().getMSHInParts()) {
      if (Objects.equals(MimeValue.MIME_XML.getMimeType(), mp.getMimeType())
              || Objects.equals(MimeValue.MIME_XML1.getMimeType(), mp.
                      getMimeType())) {
        if (envData != null) {
          throw new MEPSFault(MEPSFaultCode.Other, null,
                  "Mail must have only one XML  attachmetns (Envelope data)!",
                  SoapFault.FAULT_CODE_CLIENT);
        } else {
          envData = StorageUtils.getFile(mp.getFilepath());
        }

      } else if (Objects.equals(MimeValue.MIME_PDF.getMimeType(), mp.
              getMimeType())) {
        contentList.add(StorageUtils.getFile(mp.getFilepath()));
      } else {
        throw new MEPSFault(MEPSFaultCode.Other, null,
                "Mail must have only one XML  attachmetns (Envelope data)!",
                SoapFault.FAULT_CODE_CLIENT);
      }
    }
    try {
      PDFContentData pd = pdfUtils.concatenatePdfFiles(contentList);
      File f = storageUtils.storeInFile(MimeValue.MIME_PDF.getMimeType(), new File(pd.getTempFileName()));
      
      MSHInPart mp = new MSHInPart();
      mp.setDescription("Concenated PDF");
      mp.setFilename("concenated.pdf");
      mp.setName("concenated");
      mp.setFilepath(StorageUtils.getRelativePath(f));
      mp.setSource("MEPS");
      mp.setMimeType(MimeValue.MIME_PDF.getMimeType());
      mInMail.getMSHInPayload().getMSHInParts().add(mp);
    } catch (PDFException ex) {
      throw new MEPSFault(MEPSFaultCode.Other,null,
                "Process exception: " + ex.getMessage(), ex,
                SoapFault.FAULT_CODE_CLIENT);
    } catch (StorageException ex) {
      throw new MEPSFault(MEPSFaultCode.Other,null,
                "Process exception: " + ex.getMessage(), ex,
                SoapFault.FAULT_CODE_CLIENT);
    }

  }

  /**
   *
   * @param t
   */
  @Override
  public void handleFault(SoapMessage t) {
    // ignore
  }

}
