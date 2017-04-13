/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.tc.web.tc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import si.jrc.msh.plugin.tc.web.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import si.jrc.msh.plugin.tc.web.dlg.ProcessAbstract;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;

public class ProcessLM extends ProcessAbstract {

  private static final SEDLogger LOG = new SEDLogger(ProcessLM.class);

  ExecutorService mSTExecutor = null;
  Integer payloadSize = 5;

  private final TestCaseAbstract tcMailData;

  public ProcessLM(TestCaseAbstract data) {
    this.tcMailData = data;
  }

  public void executeStressTest() {
    String senderBox = tcMailData.getTestSenderEBox();
    String receiverBox = tcMailData.getTestReceiverEBox();
    String service = tcMailData.getTestService();
    String action = tcMailData.getTestAction();
    String subject = tcMailData.getTestSubject();
    String userName = tcMailData.getUserName();

    int ips = getPayloadSize() != null ? getPayloadSize() : 0;
    setStop(false);
    setProcessMessage("");
    setProcessTitle(String.format("Submit large mail (%d kB) by %s service.",
            ips, service));
    setProgress(0);

    LOG.formatedlog("ExecuteStressTest (sender: %s, receiver %s)",
            senderBox, receiverBox);

    mSTExecutor = Executors.newSingleThreadExecutor();
    mSTExecutor.submit(() -> {
      try {
        byte[] buff = "Brown fox jumps over smart dog.".getBytes();
        
        File f = File.createTempFile("largeFile", ".txt");
        try (FileOutputStream fos = new FileOutputStream(f)) {
          int iSize = ips*1000;
          int iCnt = 0;
          while (iCnt < iSize) {
            if (getStop()) {
            setProgress(100);
            setProcessMessage("Process stopped!");
            break;
          }
            fos.write(buff);
            iCnt += buff.length;
            setProgress(iCnt*80/iSize);
          }
        }

        // create large file
    
          List<File> lstfiles = Collections.singletonList(f);
          
          tcMailData.createOutMail(userName, senderBox, receiverBox,
                  String.format(subject, 0), service, action,
                  lstfiles);

        
        

        setProgress(100);
        setProcessMessage(String.format(
                "Created large mail by service %s.",
                service));

      } catch (StorageException ex) {
        LOG.logError(ex.getMessage(), ex);
      } catch (IOException ex) {
        Logger.getLogger(ProcessLM.class.getName()).log(Level.SEVERE, null, ex);
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

  public Integer getPayloadSize() {
    return payloadSize;
  }

  public void setPayloadSize(Integer payloadSize) {
    this.payloadSize = payloadSize;
  }

}
