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
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import si.jrc.msh.plugin.tc.utils.TestUtils;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.outbox.mail.MSHOutMail;

@SessionScoped
@ManagedBean(name = "TCStressTest")
public class TCStressTest implements Serializable {

  public static String ZPP_SERVICE = "LegalDelivery_ZPP";
  public static String ZPP_ACTION = "DeliveryNotification";

  public static String DWR_SERVICE = "DeliveryWithReceipt";
  public static String DWR_ACTION = "Delivery";

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

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
  private int payloadSizeTestProgress = 0;
  private String payloadSizeTestMessage = "";

  public Integer getStressTestProgress() {

    return stressTestProgress;
  }

  public void setStressTestProgress(int progress) {
    this.stressTestProgress = progress;
  }

  public void executeStressTest() {
    stressTestMessage = "";
    payloadSizeTestProgress = 2;
    LOG.formatedWarning("executeStressTest");
    if (Utils.isEmptyString(getTestSenderEBox())) {
      stressTestMessage = "Izberite naslov pošiljatelja!";
      stressTestProgress = 100;
      return;
    }
    if (Utils.isEmptyString(getTestReceiverEBox())) {
      stressTestMessage = "Vnesite naslov prejemnika!";
      stressTestProgress = 100;
      return;
    }
    String sndBox = getTestSenderEBox() + "@" + getLocalDomain();
    String rcvBox = getTestReceiverEBox();

    int icnt = Integer.parseInt(getStressTestCnt());
    if (!rcvBox.contains("@")) {
      stressTestMessage = String.format("Neveljaven naslov prejemnika %s!", rcvBox);
      stressTestProgress = 100;
      return;
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

    try {

      for (int i = 0; i < icnt; i++) {

        MSHOutMail mout = mTestUtils.createOutMail(i, sndBox, rcName, rcvBox, service,
            action);
        mDB.serializeOutMail(mout, "test", "test-plugin", "");
        stressTestProgress = i * 98 / icnt + 2;

      }
      stressTestProgress = 100;
      stressTestMessage = String.format("V pošiljanje  po storitvi %s poslanih %s pošiljk.",
          stressTestService, stressTestCnt);
    } catch (StorageException ex) {
      LOG.logError(ex.getMessage(), ex);
    }
  }

  public void onStressTestComplete() {
    facesContext().addMessage(null, new FacesMessage("Obremenitveni test: ", stressTestMessage));
    stressTestProgress = 0;
  }

  public void cancelStressTest() {
    stressTestProgress = 0;
    stressTestMessage = "";
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
    payloadSizeTestProgress = 2;
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
      payloadSizeTestMessage = String.format("Neveljaven naslov prejemnika %s!", rcvBox);
      payloadSizeTestProgress = 100;
      return;
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
    File f;
    try {

      f = File.createTempFile("payloadSize", ".txt");
      byte[] testString = "TEST_".getBytes();
      try (FileOutputStream fos = new FileOutputStream(f)) {
        int isz = 0;
        while (isz < lpsz) {
          fos.write(testString);
          isz += testString.length;

          payloadSizeTestProgress = 48 * isz / lpsz + 2;

        }

      }

    } catch (IOException ex) {
      LOG.logError(service, ex);
      return;
    }

    try {
      payloadSizeTestProgress = 50;

      MSHOutMail mout = mTestUtils.createOutMail(sndBox, rcName, rcvBox, service,
          action, f);

      mDB.serializeOutMail(mout, "test", "test-plugin", "");
      payloadSizeTestProgress = 100;
      payloadSizeTestMessage = String.format(
          "V pošiljanje  po storitvi %s poslana pošiljka velikosti %s KB.", payloadSizeTestService,
          payloadSizeTest);

    } catch (StorageException ex) {
      LOG.logError(ex.getMessage(), ex);
    }
  }

  public void onPayloadSizeTestComplete() {
    facesContext().addMessage(null, new FacesMessage("Test končan", payloadSizeTestMessage));
    payloadSizeTestProgress = 0;

  }

  public void cancelPayloadSizeTest() {
    payloadSizeTestProgress = 0;
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
    return System.getProperty(SEDSystemProperties.S_PROP_LAU_DOMAIN);
  }

}
