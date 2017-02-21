/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.tc.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import si.jrc.msh.plugin.tc.utils.DisableService;
import si.jrc.msh.plugin.tc.utils.DisableServiceUtils;
import si.jrc.msh.plugin.tc.utils.TestUtils;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PluginType;

@SessionScoped
@ManagedBean(name = "TCStressTest")
public class TCStressTest implements Serializable {

  public static String ZPP_SERVICE = "LegalDelivery_ZPP";
  public static String ZPP_ACTION = "DeliveryNotification";

  public static String DWR_SERVICE = "DeliveryWithReceipt";
  public static String DWR_ACTION = "Delivery";

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPMode;

  private static final SEDLogger LOG = new SEDLogger(TCStressTest.class);
  TestUtils mTestUtils = new TestUtils();

  String testReceiverEBox;
  String testSenderEBox;

  String stressTestCnt = "5";
  String stressTestService = DWR_SERVICE;

  String payloadSizeTest = "100";
  String payloadSizeTestService = DWR_SERVICE;

  private int stressTestProgress = 0;
  private String stressTestMessage = "";
  private boolean stopStressTest = true;
  ExecutorService mSTExecutor = null;
  DisableService mSelectedService = null;

  private int payloadSizeTestProgress = 0;
  private String payloadSizeTestMessage = "";
  private boolean stopPayloadSizeTest = true;
  ExecutorService mPSExecutor = null;

  public String disableService;

  public BigInteger fictionMailId;

  public Integer getStressTestProgress() {

    return stressTestProgress;
  }

  public void setStressTestProgress(int progress) {
    this.stressTestProgress = progress;
  }

  public void executeStressTest() {
    stressTestMessage = "";
    stressTestProgress = 0;
    LOG.formatedWarning("executeStressTest");
    if (Utils.isEmptyString(getTestSenderEBox())) {
      stressTestMessage = "Izberite naslov pošiljatelja!";
      stressTestProgress = 100;
      LOG.formatedWarning("RETURN NAslovnik ");
      return;
    }
    if (Utils.isEmptyString(getTestReceiverEBox())) {
      stressTestMessage = "Vnesite naslov prejemnika!";
      stressTestProgress = 100;
      LOG.formatedWarning("RETURN prejemnik ");
      return;
    }
    String sndBox = getTestSenderEBox() + "@" + getLocalDomain();
    String rcvBox = getTestReceiverEBox();
    LOG.formatedWarning("executeStressTest 1");
    int icnt = Integer.parseInt(getStressTestCnt());
    if (!rcvBox.contains("@")) {
      stressTestMessage = String.format("Neveljaven naslov prejemnika %s!",
              rcvBox);
      stressTestProgress = 100;
      LOG.formatedWarning("NEveljaven naslov");

      return;
    }
    String rcName = rcvBox.substring(0, rcvBox.indexOf("@"));
    LOG.formatedWarning("executeStressTest 3");
    if (mSTExecutor != null) {
      mSTExecutor.shutdown();
      mSTExecutor = null;
    }
    LOG.formatedWarning("executeStressTest 3");
    String service;
    String action;
    if (Objects.equals(stressTestService, ZPP_SERVICE)) {
      service = ZPP_SERVICE;
      action = ZPP_ACTION;
    } else {
      service = DWR_SERVICE;
      action = DWR_ACTION;
    }
    stopStressTest = false;
    LOG.formatedWarning("executeStressTest 4");
    mSTExecutor = Executors.newSingleThreadExecutor();
    mSTExecutor.submit(() -> {
      try {
        LOG.formatedWarning("executeStressTest 5 cnt %d", icnt);
        for (int i = 0; i < icnt; i++) {
          LOG.formatedWarning("executeStressTest 6 indx %d  cnt %d", i, icnt);
          if (stopStressTest) {
            LOG.formatedWarning("executeStressTest break");
            break;

          }
          LOG.formatedWarning("add mail %d" , i);
          MSHOutMail mout = mTestUtils.createOutMail(i, sndBox, rcName, rcvBox,
                  service,
                  action);
          mDB.serializeOutMail(mout, "test", "test-plugin", "");
          stressTestProgress = i * 98 / icnt + 2;

        }
        stressTestProgress = 100;
        stressTestMessage = String.format(
                "V pošiljanje  po storitvi %s poslanih %s pošiljk.",
                stressTestService, stressTestCnt);
      } catch (StorageException ex) {
        LOG.logError(ex.getMessage(), ex);
      }
    });

  }

  public void cancelStressTest() {
    stressTestProgress = 0;
    stressTestMessage = "";
    stopStressTest = true;
    if (mSTExecutor != null) {
      mSTExecutor.shutdown();
      mSTExecutor = null;
    }
  }

  public void onStressTestComplete() {
    facesContext().addMessage(null, new FacesMessage("Obremenitveni test: ",
            stressTestMessage));
    stressTestProgress = 0;
  }

  public String getStressTestCnt() {
    return stressTestCnt;
  }

  public void setStressTestCnt(String stressTestCnt) {
    this.stressTestCnt = stressTestCnt;
  }

  public Integer getPayloadSizeTestProgress() {

    return payloadSizeTestProgress;
  }

  public void setPayloadSizeTestProgress(int progress) {
    this.payloadSizeTestProgress = progress;
  }

  public void executePayloadSizeTest() {
    LOG.formatedWarning("executePayloadSizeTest");
    payloadSizeTestProgress = 0;
    if (Utils.isEmptyString(getTestSenderEBox())) {
      payloadSizeTestMessage = "Izberite naslov pošiljatelja!";
      payloadSizeTestProgress = 100;
      return;
    }
    if (Utils.isEmptyString(getTestReceiverEBox())) {
      payloadSizeTestMessage = "Vnesite naslov prejemnika!";
      payloadSizeTestProgress = 100;
      return;
    }
    String sndBox = getTestSenderEBox() + "@" + getLocalDomain();
    String rcvBox = getTestReceiverEBox();

    if (!rcvBox.contains("@")) {
      payloadSizeTestMessage = String.format("Neveljaven naslov prejemnika %s!",
              rcvBox);
      payloadSizeTestProgress = 100;
      return;
    }

    if (mPSExecutor != null) {
      mPSExecutor.shutdown();
      mSTExecutor = null;
    }

    String rcName = rcvBox.substring(0, rcvBox.indexOf("@"));

    String service;
    String action;
    if (Objects.equals(stressTestService, ZPP_SERVICE)) {
      service = ZPP_SERVICE;
      action = ZPP_ACTION;
    } else {
      service = DWR_SERVICE;
      action = DWR_ACTION;
    }

    int lpsz = Integer.parseInt(payloadSizeTest) * 1000;

    stopPayloadSizeTest = false;

    mSTExecutor = Executors.newSingleThreadExecutor();
    mSTExecutor.submit(() -> {
      File f;
      try {

        f = File.createTempFile("payloadSize", ".txt");
        byte[] testString = "TEST_".getBytes();
        try (FileOutputStream fos = new FileOutputStream(f)) {
          int isz = 0;
          while (isz < lpsz) {
            if (stopPayloadSizeTest) {
              break;
            }

            fos.write(testString);
            isz += testString.length;

            payloadSizeTestProgress = 48 * isz / lpsz + 2;

          }

        }

      } catch (IOException ex) {
        LOG.logError(service, ex);
        return;
      }
      if (stopPayloadSizeTest) {
        return;
      }

      try {
        payloadSizeTestProgress = 50;

        MSHOutMail mout = mTestUtils.createOutMail(sndBox, rcName, rcvBox,
                service,
                action, f);

        mDB.serializeOutMail(mout, "test", "test-plugin", "");
        payloadSizeTestProgress = 100;
        payloadSizeTestMessage = String.format(
                "V pošiljanje po storitvi %s poslana pošiljka velikosti %s KB.",
                payloadSizeTestService,
                payloadSizeTest);

      } catch (StorageException ex) {
        LOG.logError(ex.getMessage(), ex);
      }
    });
  }

  public void onPayloadSizeTestComplete() {
    facesContext().addMessage(null, new FacesMessage("Test končan",
            payloadSizeTestMessage));
    payloadSizeTestProgress = 0;

  }

  public void cancelPayloadSizeTest() {
    payloadSizeTestProgress = 0;
    stopPayloadSizeTest = true;
    if (mPSExecutor != null) {
      mPSExecutor.shutdown();
      mPSExecutor = null;
    }

  }

  public String getPayloadSize() {
    return payloadSizeTest;
  }

  public void setPayloadSize(String payloadSz) {
    this.payloadSizeTest = payloadSz;
  }

  public String getTestReceiverEBox() {
    return testReceiverEBox;
  }

  public void setTestReceiverEBox(String testReceiverEBox) {
    this.testReceiverEBox = testReceiverEBox;
  }

  public String getTestSenderEBox() {
    return testSenderEBox;
  }

  public void setTestSenderEBox(String testSenderEBox) {
    this.testSenderEBox = testSenderEBox;
  }

  public String getStressTestService() {
    return stressTestService;
  }

  public void setStressTestService(String stressTestService) {
    this.stressTestService = stressTestService;
  }

  public String getPayloadSizeTestService() {
    return payloadSizeTestService;
  }

  public void setPayloadSizeTestService(String service) {
    this.payloadSizeTestService = service;
  }

  /**
   *
   * @return
   */
  protected ExternalContext externalContext() {
    return facesContext().getExternalContext();
  }

  /**
   *
   * @return
   */
  protected FacesContext facesContext() {
    return FacesContext.getCurrentInstance();
  }

  /**
   *
   * @return
   */
  public String getLocalDomain() {
    return System.getProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN);
  }

  public List<String> getRegistredDisableServicePlugin() {
    List<String> mlst = new ArrayList<>();
    for (PMode pmd : mPMode.getPModes()) {
      if (pmd.getPlugins() != null
              && pmd.getPlugins().getInPlugins() != null
              && !pmd.getPlugins().getInPlugins().getPlugins().isEmpty()) {
        PMode.Plugins.InPlugins op = pmd.getPlugins().getInPlugins();
        for (PluginType pt : op.getPlugins()) {
          if (!Utils.isEmptyString(pt.getValue())
                  && pt.getValue().equals(
                          "java:global/plugin-testcase/TestCaseInInterceptor!si.laurentius.commons.interfaces.SoapInterceptorInterface")) {
            mlst.add(pmd.getServiceIdRef());
            break;
          }
        }
      }
    }
    return mlst;
  }

  public String getDisableService() {
    return disableService;
  }

  public void setDisableService(String disableService) {
    this.disableService = disableService;
  }

  public void addNewDisableService() {

    if (!Utils.isEmptyString(getDisableService())
            && !Utils.isEmptyString(getTestSenderEBox())
            && !Utils.isEmptyString(getTestReceiverEBox())) {
      String rb = getTestSenderEBox() + "@" + SEDSystemProperties.
              getLocalDomain();
      DisableServiceUtils.addNewDisableService(getDisableService(),
              getTestReceiverEBox(), rb);
    } else {
      facesContext().addMessage(null, new FacesMessage(
              FacesMessage.SEVERITY_WARN,
              "Manjkajoči podatki",
              "Vnesite posiljatelja, naslovnika in storitev!"));
    }
  }

  public List<DisableService> getDisableServiceList() {
    return DisableServiceUtils.STDisableList;
  }

  public void removeSelectedService(ActionEvent event) {

    DisableService sr = (DisableService) event.getComponent().getAttributes().
            get("disabledService");
    LOG.formatedlog("Remove selected list %s", sr);
    if (sr != null && DisableServiceUtils.STDisableList.contains(sr)) {
      DisableServiceUtils.STDisableList.remove(sr);
    }

  }

  public BigInteger getFictionMailId() {
    return fictionMailId;
  }

  public void setFictionMailId(BigInteger fictionMailId) {
    this.fictionMailId = fictionMailId;
  }

  public void changeSentDateAction() {
    if (fictionMailId == null) {
      facesContext().addMessage(null, new FacesMessage(
              FacesMessage.SEVERITY_WARN,
              "Manjkajoči podatki", "Vnesite Id pošiljke!"));
      return;
    }
    LOG.formatedlog("get mail by id %d", fictionMailId);
    MSHOutMail mo = mDB.getMailById(MSHOutMail.class, fictionMailId);
    if (mo == null) {
      facesContext().addMessage(null, new FacesMessage(
              FacesMessage.SEVERITY_WARN,
              "Napačni podatki", String.format(
                      "Izhodna pošiljka za id '%d' ne obstaja!", fictionMailId)));
      return;
    }
    if (!Objects.equals(mo.getStatus(), SEDOutboxMailStatus.SENT.getValue())) {
      facesContext().addMessage(null, new FacesMessage(
              FacesMessage.SEVERITY_WARN,
              "Neveljavna pošiljka", String.format(
                      "Izhodna pošiljka za id '%d' je v napačnem statusu %s!",
                      fictionMailId, mo.getStatus())));
      return;
    }
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_MONTH, -16);
    mo.setSentDate(c.getTime());
    mo.setReceivedDate(c.getTime());
    try {
      mDB.updateOutMail(mo, "Test Fikcija", "");
    } catch (StorageException ex) {
      facesContext().addMessage(null, new FacesMessage(
              FacesMessage.SEVERITY_WARN,
              "Programska napaka", ex.getMessage()));
    }

  }

}
