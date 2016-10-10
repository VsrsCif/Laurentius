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
import si.laurentius.ebox.Execute;
import si.laurentius.ebox.Export;
import si.laurentius.ebox.SEDBox;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;

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

    String exportFolderName;
    File exportFolder;
    if (sb.getExport() != null && sb.getExport().getActive() &&
        sb.getExport().getFileMask() != null) {
      Export e = sb.getExport();
      exportFolderName = msfFormat.format(e.getFileMask(), mail);
      String folder = StringFormater.replaceProperties(e.getFolder());
      exportFolder = new File(folder + File.separator + exportFolderName);
      if (!exportFolder.exists() && !exportFolder.mkdirs()) {
        String errMsg = String.format("Export failed! Could not create export folder '%s'!",
            folder);
        setStatusToInMail(mail, SEDInboxMailStatus.ERROR, errMsg);
        LOG.logError(t, "Message with id: '" + jmsMessageId + "' export failed!" + errMsg, null);
        return;
      }

    } else {
      String errMsg = String.format(
          "Receiver box '%s' does not have configurationf for exporting mail! Export suppressed for mail %d!",
          mail.getReceiverEBox(), mail.getId());
      setStatusToInMail(mail, SEDInboxMailStatus.ERROR, errMsg);
      LOG.logError(t, errMsg, null);
      return;
    }
    // export metadata
    List<String> listFiles = new ArrayList<>();

    if (sb.getExport().getExportMetaData() != null && sb.getExport().getExportMetaData()) {
      File fn = new File(exportFolder.getAbsolutePath() + File.separator + "metadata." +
          MimeValues.MIME_XML.getSuffix());
      if (fn.exists() && sb.getExport().getOverwrite() != null && sb.getExport().getOverwrite()) {
        fn.delete();
      }

      try {

        XMLUtils.serialize(mail, fn);
        listFiles.add(fn.getAbsolutePath());

      } catch (JAXBException | FileNotFoundException ex) {
        String errMsg = String.format("Failed to serialize metadata: %s!",
            ex.getMessage());
        setStatusToInMail(mail, SEDInboxMailStatus.ERROR, errMsg);
        LOG.logError(t, "Message with id: '" + jmsMessageId + "' export failed!" + errMsg, ex);
        return;
      }
    }

    for (MSHInPart mip : mail.getMSHInPayload().getMSHInParts()) {
      File f;
      try {
        f = msStorageUtils.copyFileToFolder(mip.getFilepath(), exportFolder,
            sb.getExport().getOverwrite() != null && sb.getExport().getOverwrite());
        listFiles.add(f.getAbsolutePath());
      } catch (StorageException ex) {
        String errMsg = String.format("Failed to export file %s! Error %s",
            mip.getFilepath(), ex.getMessage());
        setStatusToInMail(mail, SEDInboxMailStatus.ERROR, errMsg);
        LOG.logError(t, "Message with id: '" + jmsMessageId + "' export failed!" + errMsg, ex);
        return;
      }
    }
    // execute file 
    if (sb.getExecute() != null && sb.getExecute().getActive() != null &&
        sb.getExecute().getActive() &&
        !Utils.isEmptyString(sb.getExecute().getCommand())) {
      Execute e = sb.getExecute();
      String command = StringFormater.replaceProperties(e.getCommand());
      String params = String.join(File.pathSeparator, listFiles) + " " + msfFormat.format(
          e.getParameters(), mail);
      executeCommand(mail, command, params);
    } else {
      setStatusToInMail(mail, SEDInboxMailStatus.DELIVERED, "Mail successfully exported!");
    }
    LOG.logEnd(t, jmsMessageId);
  }

  public void executeCommand(MSHInMail mail, String cmd, String param) {
    long t = LOG.logStart();
    try {
      String command = StringFormater.replaceProperties(cmd);
      ProcessBuilder builder = new ProcessBuilder(command, param);
      LOG.formatedlog("Start execution of command '%s', params '%s' for mail %d", command, param,
          mail.getId());
      long lSt = LOG.getTime();
      Process process = builder.start();
      long procRes = process.waitFor();
      LOG.formatedlog(
          "END execution of command '%s', params '%s' for mail %d in %d ms. Return value %d",
          command, param, mail.getId(), (LOG.getTime() - lSt), procRes);

      if (procRes != 0) {
        String errMsg = String.format(
            "Execution process %s return value '%d'. Normal termination is '0'!",
            command.length() > 20 ? "..." + command.substring(command.length() - 20) : command,
            procRes);
        setStatusToInMail(mail, SEDInboxMailStatus.ERROR, errMsg);
        LOG.logError(t, String.format("Message with id: %d failed to export: %s", mail.getId(),
            errMsg), null);
      } else {
        setStatusToInMail(mail, SEDInboxMailStatus.DELIVERED,
            "Mail successfully exported with proccess execution!");
      }

    } catch (InterruptedException | IOException ex) {
      String errMsg = String.format(
          "Execution process failed %s!",
          ex.getMessage());
      setStatusToInMail(mail, SEDInboxMailStatus.ERROR, errMsg);
      LOG.logError(t, String.format("Message with id: %d failed to export: %s", mail.getId(),
          errMsg), ex);
    }
    LOG.logEnd(t);
  }

  public void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status, String msg) {
    try {
      mDB.setStatusToInMail(mail, status, msg);
    } catch (StorageException ex) {
      LOG.logError("Failed to set status to message with id: '" + mail.getId() + "'!", ex);
    }
  }

}
