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
package si.laurentius.export.jms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.NoResultException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import si.laurentius.commons.MimeValues;
import si.laurentius.commons.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.interfaces.SEDLookupsInterface;

import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.xml.XMLUtils;

import si.laurentius.ebox.SEDBox;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;

import si.laurentius.plugin.interfaces.exception.InMailProcessException;



/**
 *
 * @author Jože Rihtaršič
 */
@MessageDriven(
    activationConfig = {
      @ActivationConfigProperty(propertyName = "acknowledgeMode",
          propertyValue = "Auto-acknowledge"),
      @ActivationConfigProperty(propertyName = "destinationType",
          propertyValue = "javax.jms.Queue"),
      @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/MSHExportQueue"),
      @ActivationConfigProperty(propertyName = "maxSession",
          propertyValue = "5")})
@TransactionManagement(TransactionManagementType.BEAN)
public class MSHExportBean implements MessageListener {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(MSHExportBean.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface msedLookup;

  StorageUtils msStorageUtils = new StorageUtils();
  StringFormater msfFormat = new StringFormater();

//  XPathUtils mxsltUtils = new XPathUtils();

  @Override
  public void onMessage(Message msg) {
    long t = LOG.logStart();
    // parse JMS Message data 
    long jmsMessageId; // 

    // Read property Mail ID
    try {
      jmsMessageId = msg.getLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID);
    } catch (JMSException ex) {
      LOG.logError(t, String.format("Bad JMS message for queue: 'MSHQueue' with no property: '%s'",
          SEDValues.EBMS_QUEUE_PARAM_MAIL_ID), ex);
      return;
    }

    MSHInMail mail;
    try {
      mail = mDB.getMailById(MSHInMail.class, BigInteger.valueOf(jmsMessageId));
    } catch (NoResultException ex) {
      LOG.logError(t, "Message with id: '" + jmsMessageId + "' not exists in DB!", ex);
      return;
    }

    SEDBox sb = msedLookup.getSEDBoxByAddressName(mail.getReceiverEBox());
    if (sb == null) {
      String errMsg = String.format("Export failed! Receiver box '%s' not exists",
          mail.getReceiverEBox());
      setStatusToInMail(mail, SEDInboxMailStatus.ERROR, errMsg);
      LOG.logError(t, "Message with id: '" + jmsMessageId + "' export failed!" + errMsg, null);
      return;
    }

    List lstExportFiles = null;
/*    if (sb.getXSLT() != null) {
      try {
        tranformPayloads(sb.getXSLT(), mail);
      } catch (InMailProcessException ex) {
        setStatusToInMail(mail, SEDInboxMailStatus.ERROR, ex.getMessage());
        LOG.logError(t, "Message with id: '" + jmsMessageId + "' export failed!" + ex.getMessage(),
            ex);
        return;
      }
    }

    if (sb.getExport() != null && sb.getExport().getActive()) {
      try {
        lstExportFiles = exportMail(sb.getExport(), mail);
      } catch (InMailProcessException ex) {
        setStatusToInMail(mail, SEDInboxMailStatus.ERROR, ex.getMessage());
        LOG.logError(t, "Message with id: '" + jmsMessageId + "' export failed!" + ex.getMessage(),
            ex);
        return;

      }
    }

    // execute file 
    if (sb.getExecute() != null && sb.getExecute().getActive() != null &&
        sb.getExecute().getActive()) {
      try {
        executeProcessForMail(sb.getExecute(), mail, lstExportFiles);
        
        // mark as delivered
        setStatusToInMail(mail, SEDInboxMailStatus.DELIVERED, "Mail successfully exported!");
      } catch (InMailProcessException ex) {
        setStatusToInMail(mail, SEDInboxMailStatus.ERROR, ex.getMessage());
        LOG.logError(t, "Message with id: '" + jmsMessageId + "' Execution failed!" +
            ex.getMessage(),
            ex);
        return;
      }

    }
    */

    LOG.logEnd(t, jmsMessageId);
  }
/*
  public long executeProcessForMail(Execute e, MSHInMail mail, List<String> lstExportFiles)
      throws InMailProcessException {
    if (!Utils.isEmptyString(e.getCommand())) {
      String errMsg = "Execution external procces failed! No command defimed!";
      throw new InMailProcessException(InMailProcessException.ProcessExceptionCode.InitException,
          errMsg);
    }

    String command = StringFormater.replaceProperties(e.getCommand());
    String params =
        (lstExportFiles != null ? String.join(File.pathSeparator, lstExportFiles) + " " : "") +
        msfFormat.format(
            e.getParameters(), mail);
    return executeCommand(mail, command, params);

  }

  public List<String> exportMail(Export e, MSHInMail mail)
      throws InMailProcessException {

    String exportFolderName;
    File exportFolder;
    // export metadata
    List<String> listFiles = new ArrayList<>();

    exportFolderName = msfFormat.format(e.getFileMask(), mail);
    String folder = StringFormater.replaceProperties(e.getFolder());
    exportFolder = new File(folder + File.separator + exportFolderName);
    if (!exportFolder.exists() && !exportFolder.mkdirs()) {
      String errMsg = String.format("Export failed! Could not create export folder '%s'!",
          folder);
      throw new InMailProcessException(InMailProcessException.ProcessExceptionCode.InitException,
          errMsg);
    }

    boolean overwriteFiles = e.getOverwrite() != null && e.getOverwrite();
    if (e.getExportMetaData() != null && e.getExportMetaData()) {
      File fn = new File(exportFolder.getAbsolutePath() + File.separator + String.format(
          "metadata_%06d.", mail.getId()) +
          MimeValues.MIME_XML.getSuffix());
      if (fn.exists() && overwriteFiles) {
        fn.delete();
      }

      try {

        XMLUtils.serialize(mail, fn);
        listFiles.add(fn.getAbsolutePath());

      } catch (JAXBException | FileNotFoundException ex) {
        String errMsg = String.format("Failed to serialize metadata: %s!",
            ex.getMessage());
        throw new InMailProcessException(
            InMailProcessException.ProcessExceptionCode.ProcessException, errMsg);

      }

      for (MSHInPart mip : mail.getMSHInPayload().getMSHInParts()) {
        File f;
        try {
          f = msStorageUtils.copyFileToFolder(mip.getFilepath(), exportFolder,
              e.getOverwrite() != null && e.getOverwrite());
          listFiles.add(f.getAbsolutePath());
        } catch (StorageException ex) {
          String errMsg = String.format("Failed to export file %s! Error %s",
              mip.getFilepath(), ex.getMessage());
          throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.ProcessException, errMsg);
        }
      }
    }
    return listFiles;
  }

  public long executeCommand(MSHInMail mail, String cmd, String param)
      throws InMailProcessException {
    long t = LOG.logStart();
    long procRes = -1;
    try {
      String command = StringFormater.replaceProperties(cmd);
      ProcessBuilder builder = new ProcessBuilder(command, param);
      LOG.formatedlog("Start execution of command '%s', params '%s' for mail %d", command, param,
          mail.getId());
      long lSt = LOG.getTime();
      Process process = builder.start();
      procRes = process.waitFor();
      LOG.formatedlog(
          "END execution of command '%s', params '%s' for mail %d in %d ms. Return value %d",
          command, param, mail.getId(), (LOG.getTime() - lSt), procRes);

      if (procRes != 0) {
        String errMsg = String.format(
            "Execution process %s return value '%d'. Normal termination is '0'!",
            command.length() > 20 ? "..." + command.substring(command.length() - 20) : command,
            procRes);

        throw new InMailProcessException(
            InMailProcessException.ProcessExceptionCode.ProcessException,
            errMsg);

      } else {
        setStatusToInMail(mail, SEDInboxMailStatus.DELIVERED,
            "Mail successfully exported with proccess execution!");
      }

    } catch (InterruptedException | IOException ex) {
      String errMsg = String.format(
          "Execution process failed %s!",
          ex.getMessage());
      throw new InMailProcessException(
          InMailProcessException.ProcessExceptionCode.ProcessException,
          errMsg, ex, false, true);

    }
    LOG.logEnd(t);
    return procRes;
  }*/

  public void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status, String msg) {
    try {
      mDB.setStatusToInMail(mail, status, msg);
    } catch (StorageException ex) {
      LOG.logError("Failed to set status to message with id: '" + mail.getId() + "'!", ex);
    }
  }
/*
  public void tranformPayloads(SEDXslt xslt, MSHInMail mim)
      throws InMailProcessException {
    LOG.formatedlog("XSLT for box");
    for (XPathRule xpr : xslt.getXPathRules()) {
      LOG.formatedlog("XSLT for box rule 1");
      if (Objects.equals(xpr.getService(), mim.getService()) &&
          Objects.equals(xpr.getAction(), mim.getAction()) &&
          mim.getMSHInPayload() != null &&
          !mim.getMSHInPayload().getMSHInParts().isEmpty()) {

        LOG.formatedlog("XSLT for box rule 2");
        List<MSHInPart> miplst = mim.getMSHInPayload().getMSHInParts();
        XPath xpath = mxsltUtils.createXPathFromNSContext(new XSLTNamespaceContext(
            xslt.getNamespaces()));
        List<MSHInPart> mipNewPayload = new ArrayList<>();
        for (MSHInPart mip : miplst) {
          LOG.formatedlog("XSLT for box rule 3");
          if (Objects.equals(mip.getMimeType(), MimeValues.MIME_XML.getMimeType())) {
            LOG.formatedlog("XSLT for box rule 4");
            try {
              Document doc = XMLUtils.deserializeToDom(StorageUtils.getFile(mip.getFilepath()));
              if (mxsltUtils.doesRuleApply(doc, xpath, xpr.getXPath(), xpr.getValue())) {
                LOG.formatedlog("XSLT for box rule 5");
                String trFile = StringFormater.replaceProperties(xpr.getTransformation());
                LOG.formatedlog("XSLT file %s ", trFile);

                try {
                  File fRes = StorageUtils.getNewStorageFile("xml", "xslt");
                  XSLTUtils.transform(doc, new File(trFile), fRes);

                  MSHInPart mipt = new MSHInPart();
                  mipt.setDescription("Transform");
                  mipt.setMimeType(MimeValues.MIME_XML.getMimeType());
                  mipt.setFilename(fRes.getName());
                  mipt.setSource("xslt");
                  mipt.setFilepath(StorageUtils.getRelativePath(fRes));
                  mipt.setName("transformation");
                  mipNewPayload.add(mipt);

                } catch (JAXBException | TransformerException | StorageException ex) {
                  String errMsg = String.format(
                      "XSLT transformation failed %s!",
                      ex.getMessage());
                  throw new InMailProcessException(
                      InMailProcessException.ProcessExceptionCode.ProcessException,
                      errMsg, ex, false, true);
                }

              }
            } catch (IOException | ParserConfigurationException | SAXException
                | XPathExpressionException ex) {

              String errMsg = String.format(
                  "XSLT transformation failed %s!",
                  ex.getMessage());
              throw new InMailProcessException(
                  InMailProcessException.ProcessExceptionCode.ProcessException,
                  errMsg, ex, false, true);
            }

          }
        }
        // add new payloads
        if (!mipNewPayload.isEmpty()) {
          mim.getMSHInPayload().getMSHInParts().addAll(mipNewPayload);
          try {
            mDB.updateInMail(mim, "transform", "");
          } catch (StorageException ex) {
            String errMsg = String.format(
                "XSLT transformation failed %s!",
                ex.getMessage());
            throw new InMailProcessException(
                InMailProcessException.ProcessExceptionCode.ProcessException,
                errMsg, ex, false, true);
          }
        }

      }
    }

  }*/

}
