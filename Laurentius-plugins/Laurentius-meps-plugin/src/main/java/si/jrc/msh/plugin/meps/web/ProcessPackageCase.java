/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.meps.web;

import java.io.File;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import si.jrc.msh.plugin.meps.web.dlg.ProcessAbstract;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.plugin.meps.ServiceType;

public class ProcessPackageCase extends ProcessAbstract {

  private static final SEDLogger LOG = new SEDLogger(ProcessPackageCase.class);

  ExecutorService mSTExecutor = null;
  Integer mailCount = 5;
  String testReceiverEBox;
  String testSenderEBox;
  String testService = "PrintAndEnvelope-LegalZPP";

  private final MEPSTestAbstract tcMailData;

  public String getTestSenderEBox() {
    return testSenderEBox;
  }

  public void setTestSenderEBox(String testSenderEBox) {
    this.testSenderEBox = testSenderEBox;
  }

  public ProcessPackageCase(MEPSTestAbstract data) {
    this.tcMailData = data;
  }

  public String getTestReceiverEBox() {
    return testReceiverEBox;
  }

  public void setTestReceiverEBox(String testReceiverEBox) {
    this.testReceiverEBox = testReceiverEBox;
  }

  public String getTestService() {
    return testService;
  }

  public void setTestService(String testService) {
    this.testService = testService;
  }

  public void prepareToStart() {
    setProcessMessage("");
    setProcessTitle(String.
            format("Submit %d mail by %s service", getMailCount(),
                    getTestService()));
    setProgress(0);
  }

  public void executeStressTest() {

    String senderBox = getTestSenderEBox();
    String receiverBox = getTestReceiverEBox();
    String service = getTestService();
    String action = "AddMail";
    ServiceType st = tcMailData.getServiceType(service);
   // String subject = tcMailData.getTestSubject();
   // String userName = tcMailData.getUserName();

     String subject = "test ";
    String userName = " test ";
   
   
    int icnt = getMailCount() != null ? getMailCount() : 0;
    setStop(false);

    LOG.formatedWarning("ExecuteStressTest (sender: %s, receiver %s, cnt: %d)",
            senderBox, receiverBox, icnt);

    mSTExecutor = Executors.newSingleThreadExecutor();
    mSTExecutor.submit(() -> {
      try {
        LOG.formatedWarning(
                "ExecuteStressTest Start (sender: %s, receiver %s, cnt: %d)",
                senderBox, receiverBox, icnt);
        for (int i = 0; i < icnt; i++) {
          LOG.formatedWarning(
                  "Stat to Submit %d (sender: %s, receiver %s, cnt: %d)",
                  i, senderBox, receiverBox, icnt);
          // check if stop stress
          if (getStop()) {
            setProgress(100);
            setProcessMessage("Process stopped!");
            break;
          }
          LOG.formatedWarning(
                  "Create mail list  %d (sender: %s, receiver %s, cnt: %d)",
                  i, senderBox, receiverBox, icnt);
          
          tcMailData.createOutMail(i, senderBox, receiverBox,
                   service, action, st,
                  userName);

          setProgress(i * 98 / icnt + 2);

        }
        LOG.formatedDebug("Stop list  (sender: %s, receiver %s, cnt: %d)",
                senderBox, receiverBox, icnt);
        setProgress(100);
        setProcessMessage(String.format(
                "Created %s mail by service %s.",
                icnt, service));

      } catch (JAXBException | StorageException ex) {
        LOG.logError(ex.getMessage(), ex);
      } 
    });

  }

  public void cancelStressTest() {
    setStop(true);

    if (mSTExecutor != null) {
      mSTExecutor.shutdown();
      mSTExecutor = null;
    }
  }

  public Integer getMailCount() {
    return mailCount;
  }

  public void setMailCount(Integer mailCount) {
    this.mailCount = mailCount;
  }

  public boolean validateData() {
    if (Utils.isEmptyString(getTestSenderEBox())) {
      tcMailData.addError("Missing data", "Missing sender box address!");
      return false;
    }

    if (!getTestSenderEBox().contains("@")) {
      tcMailData.addError("Invalid data", "Invalid sender box address!");
      return false;
    }

    if (Utils.isEmptyString(getTestReceiverEBox())) {
      tcMailData.addError("Missing data", "Missing receiver box address!");
      return false;
    }
    if (!getTestReceiverEBox().contains("@")) {
      tcMailData.addError("Invalid data", "Invalid receiver box address!");
      return false;
    }
    if (Utils.isEmptyString(getTestService())) {
      tcMailData.addError("Missing data", "Missing service!");
      return false;
    }

    return true;

  }
}
