/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.tc.web.tc;

import java.io.File;
import java.util.List;
import si.jrc.msh.plugin.tc.web.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import si.jrc.msh.plugin.tc.web.dlg.ProcessAbstract;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;

public class ProcessLAOM extends ProcessAbstract {

  private static final SEDLogger LOG = new SEDLogger(ProcessLAOM.class);

  
  ExecutorService mSTExecutor = null;
  Integer mailCount = 5;

  private final TestCaseAbstract tcMailData;
  
  
  

  public ProcessLAOM(TestCaseAbstract data) {
    this.tcMailData = data;
  }

  

  public void executeStressTest() {
    String senderBox = tcMailData.getTestSenderEBox();
    String receiverBox = tcMailData.getTestReceiverEBox();
    String service = tcMailData.getTestService();
    String action = tcMailData.getTestAction();
    String subject = tcMailData.getTestSubject();
    String userName = tcMailData.getUserName();
    
    int icnt = getMailCount()!=null? getMailCount():0;
    setStop(false);
    setProcessMessage("");
    setProcessTitle(String.format("Submit %d mail by %s service", icnt, service));
    setProgress(0);
    
    LOG.formatedlog("ExecuteStressTest (sender: %s, receiver %s)",
            senderBox, receiverBox);

    
   

    mSTExecutor = Executors.newSingleThreadExecutor();
    mSTExecutor.submit(() -> {
      try {
        for (int i = 0; i < icnt; i++) {
          // check if stop stress
          if (getStop()) {
            setProgress(100);
            setProcessMessage("Process stopped!");
            break;
          }
          List<File> lstfiles = tcMailData.getTestFiles(); 
          tcMailData.createOutMail(userName, senderBox, receiverBox, 
                  String.format(subject, i), service, action,
                  lstfiles);
                   
          setProgress( i * 98 / icnt + 2);

        }
        
        setProgress(100);
        setProcessMessage(String.format(
                "Created %s mail by service %s.",
                icnt, service));

      } catch (StorageException ex) {
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

}
