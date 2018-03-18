/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.tc.web.tc;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import si.jrc.msh.plugin.tc.web.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import si.jrc.msh.plugin.tc.utils.TestUtils;
import si.jrc.msh.plugin.tc.web.dlg.ProcessAbstract;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.testcase.MailTestCases;

public class ProcessLAOM extends ProcessAbstract {

  private static final SEDLogger LOG = new SEDLogger(ProcessLAOM.class);

  ExecutorService mSTExecutor = null;
  Integer mailCount = 5;

  private final TestCaseAbstract tcMailData;

  protected List<String> mailTemplatesList = new ArrayList<>();

  public ProcessLAOM(TestCaseAbstract data) {
    this.tcMailData = data;
  }

  public List<String> getMailTemplates() {
    return mailTemplatesList;
  }

  public void setMailTemplates(List<String> mailTemplatesList) {
    this.mailTemplatesList = mailTemplatesList;
  }

  public void executeStressTest() {
    String senderBox = tcMailData.getTestSenderEBox();
    String receiverBox = tcMailData.getTestReceiverEBox();
    String service = tcMailData.getTestService();
    String action = tcMailData.getTestAction();
    String subject = tcMailData.getTestSubject();
    String userName = tcMailData.getUserName();

    int icnt = getMailCount() != null ? getMailCount() : 0;
    setStop(false);
    setProcessMessage("");
    setProcessTitle(String.format("Submit %d mail by %s service", icnt, service));
    setProgress(0);

    MailTestCases mtc = TestUtils.getGenericTestCases();
    Map<String, MailTestCases.MailTestCase> mapTC = new Hashtable<>();
    if (mtc != null) {
      for (MailTestCases.MailTestCase mt : mtc.getMailTestCases()) {
        mapTC.put(mt.getName(), mt);
      }
    }

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
          List<File> lstfiles = null;

          if (mailTemplatesList == null || mailTemplatesList.isEmpty()) {
            lstfiles = tcMailData.getTestFiles();
          } else {

            String mailTemplate = mailTemplatesList.get(i % mailTemplatesList.
                    size());
            if (mapTC.containsKey(mailTemplate)) {
              MailTestCases.MailTestCase mt = mapTC.get(mailTemplate);
              
              lstfiles = new ArrayList<>();
              String folder
                      = StringFormater.replaceProperties(
                              TestUtils.GENERIC_FOLDER);
              for (MailTestCases.MailTestCase.Payload p : mt.getPayloads()) {
                File f = new File(SEDSystemProperties.getPluginsFolder(),
                        folder + p.getFilepath());
                if (f.exists()) {
                  lstfiles.add(f);
                } else {
                  LOG.formatedError("Missing file %s", f.getName());
                  return;
                }
              }

            } else {
              lstfiles = tcMailData.getTestFiles();
            }

          }

          tcMailData.createOutMail(userName, senderBox, receiverBox,
                  String.format(subject, i), service, action,
                  lstfiles);

          setProgress(i * 98 / icnt + 2);

        }

        setProgress(100);
        setProcessMessage(String.format(
                "Created %s mail by service %s.",
                icnt, service));

      } catch (StorageException | PModeException ex) {
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
