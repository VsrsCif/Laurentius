/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb;

import com.sun.javafx.css.CalculatedValue;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTaskProperty;
import si.laurentius.ebox.SEDBox;
import si.laurentius.ejb.db.MockUserTransaction;
import si.laurentius.interceptor.SEDInterceptorProperty;
import si.laurentius.interceptor.SEDInterceptorRule;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessorRule;
import si.laurentius.process.SEDProcessorSet;
import si.laurentius.user.SEDUser;

/**
 *
 * @author sluzba
 */
public class SEDLookupsTest extends TestUtils {

  static EntityManagerFactory memfMSHFactory = null;
  static SEDLookups mTestInstance = new SEDLookups();

  @BeforeClass
  public static void setUpClass() throws IOException {

    // ---------------------------------
    // set logger
    setLogger(SEDLookupsTest.class.getSimpleName());

    // create persistence unit
    memfMSHFactory = Persistence.createEntityManagerFactory(
            PERSISTENCE_UNIT_NAME);
    mTestInstance.memEManager = memfMSHFactory.createEntityManager();
    mTestInstance.mutUTransaction
            = new MockUserTransaction(mTestInstance.memEManager.getTransaction());

  }

  @Test
  public void testAddSEDBox() throws Exception {
    System.out.println("addSEDBox");

    SEDBox sbInit = TestLookupUtils.createSEDBox();
    boolean result = mTestInstance.addSEDBox(sbInit);
    assertTrue(result);

    SEDBox sb2 = mTestInstance.getSEDBoxByLocalName(sbInit.getLocalBoxName());
    assertNotNull(sb2);
    assertEquals(sbInit.getLocalBoxName(), sb2.getLocalBoxName());
    assertEquals(sbInit.getActiveFromDate(), sb2.getActiveFromDate());
    assertEquals(sbInit.getActiveToDate(), sb2.getActiveToDate());

  }

  @Test
  public void testAddSEDCronJob() throws Exception {
    System.out.println("addSEDCronJob");
    SEDCronJob init = TestLookupUtils.createSEDCronJob();
    boolean result = mTestInstance.addSEDCronJob(init);
    assertTrue(result);

    SEDCronJob res = mTestInstance.getSEDCronJobByName(init.getName());
    assertNotNull(res);
    assertNotNull(res.getId());
    assertNotNull(res.getSEDTask());
    assertEquals(init.getActive(), res.getActive());
    assertEquals(init.getDayOfMonth(), res.getDayOfMonth());
    assertEquals(init.getDayOfWeek(), res.getDayOfWeek());
    assertEquals(init.getHour(), res.getHour());
    assertEquals(init.getMinute(), res.getMinute());
    assertEquals(init.getMonth(), res.getMonth());
    assertEquals(init.getName(), res.getName());

    assertEquals(init.getSEDTask().getPlugin(), res.getSEDTask().getPlugin());
    assertEquals(init.getSEDTask().getPluginVersion(), res.getSEDTask().
            getPluginVersion());
    assertEquals(init.getSEDTask().getType(), res.getSEDTask().getType());

    assertEquals(init.getSEDTask().getSEDTaskProperties().size(), res.
            getSEDTask().getSEDTaskProperties().size());
    for (int i = 0; i < init.getSEDTask().
            getSEDTaskProperties().size(); i++) {
      assertNotNull(res.getSEDTask().
              getSEDTaskProperties().get(i).getId());
      assertEquals(init.getSEDTask().
              getSEDTaskProperties().get(i).getKey(), res.
              getSEDTask().getSEDTaskProperties().get(i).
              getKey());
      assertEquals(init.getSEDTask().
              getSEDTaskProperties().get(i).getValue(), res.
              getSEDTask().getSEDTaskProperties().get(i).
              getValue());
    }

  }

  @Test
  public void testAddSEDInterceptorRule() throws Exception {
    System.out.println("addSEDInterceptorRule");
    SEDInterceptorRule init = TestLookupUtils.createSEDInterceptorRule();
    boolean result = mTestInstance.addSEDInterceptorRule(init);
    assertTrue(result);

    SEDInterceptorRule res = mTestInstance.getSEDInterceptorRuleByName(init.
            getName());
    assertNotNull(res);
    assertNotNull(res.getId());
    assertNotNull(res.getSEDInterceptorInstance());
    assertEquals(init.getActive(), res.getActive());
    assertEquals(init.getAction(), res.getAction());
    assertEquals(init.getName(), res.getName());
    assertEquals(init.getInterceptEvent(), res.getInterceptEvent());

    assertEquals(init.getReceiverEBox(), res.getReceiverEBox());
    assertEquals(init.getSenderEBox(), res.getSenderEBox());
    assertEquals(init.getService(), res.getService());
    assertEquals(init.getSEDInterceptorInstance().getPlugin(), res.
            getSEDInterceptorInstance().getPlugin());
    assertEquals(init.getSEDInterceptorInstance().getPluginVersion(), res.
            getSEDInterceptorInstance().getPluginVersion());
    assertEquals(init.getSEDInterceptorInstance().getType(), res.
            getSEDInterceptorInstance().getType());
    assertEquals(init.getSEDInterceptorInstance().getSEDInterceptorProperties().
            size(), res.getSEDInterceptorInstance().
                    getSEDInterceptorProperties().
                    size());

    for (int i = 0; i < init.getSEDInterceptorInstance().
            getSEDInterceptorProperties().size(); i++) {
      assertNotNull(res.getSEDInterceptorInstance().
              getSEDInterceptorProperties().get(i).getId());
      assertEquals(init.getSEDInterceptorInstance().
              getSEDInterceptorProperties().get(i).getKey(), res.
              getSEDInterceptorInstance().getSEDInterceptorProperties().get(i).
              getKey());
      assertEquals(init.getSEDInterceptorInstance().
              getSEDInterceptorProperties().get(i).getValue(), res.
              getSEDInterceptorInstance().getSEDInterceptorProperties().get(i).
              getValue());
    }

  }

  @Test
  public void testAddSEDProcessorRule() throws Exception {
    System.out.println("addSEDProcessorRule");

    int iPSize = mTestInstance.getSEDProcessorRules().size();
    SEDProcessorRule init = TestLookupUtils.createSEDProcessorRule();
    boolean result = mTestInstance.addSEDProcessorRule(init);
    assertTrue(result);
    assertEquals(iPSize + 1, mTestInstance.getSEDProcessorRules().size());

    SEDProcessorRule res = null;
    for (SEDProcessorRule pr : mTestInstance.getSEDProcessorRules()) {
      // test code is unique
      if (Objects.equals(pr.getProcSetCode(), init.getProcSetCode())) {
        res = pr;
        break;
      }
    }
    assertNotNull(res);
    assertNotNull(res.getId());
    assertEquals(init.getAction(), res.getAction());
    assertEquals(init.getReceiverEBox(), res.getReceiverEBox());
    assertEquals(init.getSenderEBox(), res.getSenderEBox());
    assertEquals(init.getService(), res.getService());

  }

  @Test
  public void testAddSEDProcessorSet() throws Exception {
    System.out.println("addSEDProcessorSet");
    SEDProcessorSet init = TestLookupUtils.createSEDProcessorSet();
    boolean result = mTestInstance.addSEDProcessorSet(init);
    assertTrue(result);

    SEDProcessorSet res = mTestInstance.getSEDProcessorSet(init.
            getCode());
    assertNotNull(res);
    assertEquals(init.getName(), res.getName());
    assertEquals(init.getCode(), res.getCode());
    assertEquals(init.getDescription(), res.getDescription());
    assertEquals(init.getSEDProcessorInstances().size(),
            res.getSEDProcessorInstances().size());

    for (int i = 0; i < init.getSEDProcessorInstances().size(); i++) {

      assertNotNull(res.getSEDProcessorInstances().get(i).getId());

      assertEquals(init.getSEDProcessorInstances().get(i).getInstance(),
              res.getSEDProcessorInstances().get(i).getInstance());

      assertEquals(init.getSEDProcessorInstances().get(i).getPlugin(),
              res.getSEDProcessorInstances().get(i).getPlugin());
      assertEquals(init.getSEDProcessorInstances().get(i).getPluginVersion(),
              res.getSEDProcessorInstances().get(i).getPluginVersion());
      assertEquals(init.getSEDProcessorInstances().get(i).getType(),
              res.getSEDProcessorInstances().get(i).getType());
    }
  }

  @Test
  public void testAddSEDUser() throws Exception {
    System.out.println("addSEDUser");
    SEDUser init = TestLookupUtils.createSEDUser(true);
    // persist setboxes
    for (SEDBox sb : init.getSEDBoxes()) {
      mTestInstance.addSEDBox(sb);
    }

    boolean result = mTestInstance.addSEDUser(init);
    assertTrue(result);
    SEDUser res = mTestInstance.getSEDUserByUserId(init.getUserId());
    assertNotNull(res);
    assertEquals(init.getName(), res.getName());
    assertEquals(init.getUserId(), res.getUserId());
    assertEquals(init.getActiveFromDate(), res.getActiveFromDate());
    assertEquals(init.getActiveToDate(), res.getActiveToDate());

    assertEquals(init.getDesc(), res.getDesc());
    assertEquals(init.getEmail(), res.getEmail());
    assertEquals(init.getSEDBoxes().size(), res.getSEDBoxes().size());
  }

  @Test
  public void testGetSEDBoxByAddressName() throws Exception {
    System.out.println("getSEDBoxByAddressName");

    SEDBox init = TestLookupUtils.createSEDBox();
    String strname = init.getLocalBoxName() + "@" + LAU_TEST_DOMAIN;
    boolean result = mTestInstance.addSEDBox(init);
    assertTrue(result);

    SEDBox res = mTestInstance.getSEDBoxByAddressName(strname);
    assertNotNull(res);
    assertEquals(init.getLocalBoxName(), res.getLocalBoxName());

  }

  @Test
  public void testGetSEDBoxByLocalName() throws Exception {
    System.out.println("getSEDBoxByLocalName");
    SEDBox init = TestLookupUtils.createSEDBox();
    String strname = init.getLocalBoxName();
    boolean result = mTestInstance.addSEDBox(init);
    assertTrue(result);

    SEDBox res = mTestInstance.getSEDBoxByLocalName(strname);
    assertNotNull(res);
    assertEquals(init.getLocalBoxName(), res.getLocalBoxName());

  }

  @Test
  public void testGetSEDBoxes() throws Exception {
    System.out.println("getSEDBoxes");
    int iSize = mTestInstance.getSEDBoxes().size();
    int iCnt = 4;
    for (int i = 0; i < iCnt; i++) {
      mTestInstance.addSEDBox(TestLookupUtils.createSEDBox());
    }
    assertEquals(iSize + iCnt, mTestInstance.getSEDBoxes().size());
  }

  @Test
  public void testGetSEDCronJobById() throws Exception {
    System.out.println("getSEDCronJobById");

    SEDCronJob init = TestLookupUtils.createSEDCronJob();
    mTestInstance.addSEDCronJob(init);
    SEDCronJob rs = mTestInstance.getSEDCronJobByName(init.getName());
    SEDCronJob rs2 = mTestInstance.getSEDCronJobById(rs.getId());
    assertEquals(rs, rs2);

  }

  @Test
  public void testGetSEDInterceptorRuleById() throws Exception {
    System.out.println("getSEDInterceptorRuleById");

    SEDInterceptorRule init = TestLookupUtils.createSEDInterceptorRule();
    boolean result = mTestInstance.addSEDInterceptorRule(init);
    assertTrue(result);

    SEDInterceptorRule rs = mTestInstance.getSEDInterceptorRuleByName(init.
            getName());
    SEDInterceptorRule rs2 = mTestInstance.getSEDInterceptorRuleById(
            rs.getId());
    assertEquals(rs, rs2);
  }

  @Test
  public void testGetSEDCronJobs() throws Exception {
    System.out.println("getSEDCronJobs");
    int iSize = mTestInstance.getSEDCronJobs().size();
    int iCnt = 4;
    for (int i = 0; i < iCnt; i++) {
      mTestInstance.addSEDCronJob(TestLookupUtils.createSEDCronJob());
    }
    assertEquals(iSize + iCnt, mTestInstance.getSEDCronJobs().size());
  }

  @Test
  public void testGetSEDInterceptorRules() throws Exception {
    System.out.println("getSEDInterceptorRules");
    int iSize = mTestInstance.getSEDInterceptorRules().size();
    int iCnt = 4;
    for (int i = 0; i < iCnt; i++) {
      mTestInstance.addSEDInterceptorRule(TestLookupUtils.
              createSEDInterceptorRule());
    }
    assertEquals(iSize + iCnt, mTestInstance.getSEDInterceptorRules().size());

  }

  @Test
  public void testGetSEDProcessorRule() throws Exception {
    System.out.println("getSEDProcessorRule");
    mTestInstance.addSEDProcessorRule(TestLookupUtils.createSEDProcessorRule());
    SEDProcessorRule rs = mTestInstance.getSEDProcessorRules().get(0);
    SEDProcessorRule rs2 = mTestInstance.getSEDProcessorRule(rs.getId());

    assertEquals(rs, rs2);

  }

  @Test
  public void testGetSEDProcessorRules() throws Exception {
    int iSize = mTestInstance.getSEDProcessorRules().size();
    int iCnt = 4;
    for (int i = 0; i < iCnt; i++) {
      mTestInstance.
              addSEDProcessorRule(TestLookupUtils.createSEDProcessorRule());
    }
    assertEquals(iSize + iCnt, mTestInstance.getSEDProcessorRules().size());
  }

  @Test
  public void testGetSEDProcessorSet() throws Exception {
    System.out.println("getSEDProcessorSet");
    SEDProcessorSet init = TestLookupUtils.createSEDProcessorSet();
    boolean result = mTestInstance.addSEDProcessorSet(init);
    assertTrue(result);

    SEDProcessorSet res = mTestInstance.getSEDProcessorSet(init.
            getCode());
    assertNotNull(res);
    assertEquals(init.getName(), res.getName());
    assertEquals(init.getCode(), res.getCode());

  }

  @Test
  public void testGetSEDProcessorSets() throws Exception {
    System.out.println("getSEDProcessorSets");
    int iSize = mTestInstance.getSEDProcessorSets().size();
    int iCnt = 4;
    for (int i = 0; i < iCnt; i++) {
      mTestInstance.addSEDProcessorSet(TestLookupUtils.createSEDProcessorSet());
    }
    assertEquals(iSize + iCnt, mTestInstance.getSEDProcessorSets().size());

  }

  @Test
  public void testGetSEDUserByUserId() throws Exception {
    System.out.println("getSEDUserByUserId");
    SEDUser init = TestLookupUtils.createSEDUser(false);
    boolean result = mTestInstance.addSEDUser(init);
    assertTrue(result);

    SEDUser res = mTestInstance.getSEDUserByUserId(init.getUserId());

    assertNotNull(res);
    assertEquals(init.getName(), res.getName());
    assertEquals(init.getUserId(), res.getUserId());

  }

  @Test
  public void testGetSEDUsers() throws Exception {
    System.out.println("getSEDUsers");
    int iSize = mTestInstance.getSEDUsers().size();
    int iCnt = 4;
    for (int i = 0; i < iCnt; i++) {
      mTestInstance.addSEDUser(TestLookupUtils.createSEDUser(false));
    }
    assertEquals(iSize + iCnt, mTestInstance.getSEDUsers().size());
  }

  @Test
  public void testRemoveSEDBox() throws Exception {
    System.out.println("removeSEDBox");
    SEDBox sbInit = TestLookupUtils.createSEDBox();
    boolean result = mTestInstance.addSEDBox(sbInit);
    assertTrue(result);

    SEDBox sb2 = mTestInstance.getSEDBoxByLocalName(sbInit.getLocalBoxName());
    assertNotNull(sb2);

    result = mTestInstance.removeSEDBox(sb2);
    assertTrue(result);

    SEDBox sb3 = mTestInstance.getSEDBoxByLocalName(sbInit.getLocalBoxName());
    assertNull(sb3);

  }

  @Test
  public void testRemoveSEDCronJob() throws Exception {
    System.out.println("removeSEDCronJob");
    SEDCronJob init = TestLookupUtils.createSEDCronJob();
    boolean result = mTestInstance.addSEDCronJob(init);
    assertTrue(result);

    SEDCronJob sb2 = mTestInstance.getSEDCronJobByName(init.getName());
    assertNotNull(sb2);

    result = mTestInstance.removeSEDCronJob(sb2);
    assertTrue(result);

    SEDCronJob sb3 = mTestInstance.getSEDCronJobByName(init.getName());
    assertNull(sb3);

  }

  @Test
  public void testRemoveSEDInterceptorRule() throws Exception {
    System.out.println("removeSEDInterceptorRule");
    SEDInterceptorRule init = TestLookupUtils.createSEDInterceptorRule();
    boolean result = mTestInstance.addSEDInterceptorRule(init);
    assertTrue(result);

    SEDInterceptorRule sb2 = mTestInstance.getSEDInterceptorRuleByName(init.
            getName());
    assertNotNull(sb2);

    result = mTestInstance.removeSEDInterceptorRule(sb2);
    assertTrue(result);

    SEDInterceptorRule sb3 = mTestInstance.getSEDInterceptorRuleByName(init.
            getName());
    assertNull(sb3);

  }

  @Test
  public void testRemoveSEDProcessorRule() throws Exception {
    System.out.println("removeSEDProcessorRule");
    SEDProcessorRule init = TestLookupUtils.createSEDProcessorRule();
    boolean result = mTestInstance.addSEDProcessorRule(init);
    assertTrue(result);

    SEDProcessorRule sb2 = mTestInstance.getSEDProcessorRules().get(0);
    assertNotNull(sb2);

    result = mTestInstance.removeSEDProcessorRule(sb2);
    assertTrue(result);

    SEDProcessorRule sb3 = mTestInstance.getSEDProcessorRule(sb2.getId());
    assertNull(sb3);

  }

  @Test
  public void testRemoveSEDProcessorSet() throws Exception {
    System.out.println("removeSEDProcessorSet");
    SEDProcessorSet init = TestLookupUtils.createSEDProcessorSet();
    boolean result = mTestInstance.addSEDProcessorSet(init);
    assertTrue(result);

    SEDProcessorSet sb2 = mTestInstance.getSEDProcessorSet(init.getCode());
    assertNotNull(sb2);

    result = mTestInstance.removeSEDProcessorSet(sb2);
    assertTrue(result);

    SEDProcessorSet sb3 = mTestInstance.getSEDProcessorSet(init.getCode());
    assertNull(sb3);
  }

  @Test
  public void testRemoveSEDUser() throws Exception {
    System.out.println("removeSEDUser");

    SEDUser init = TestLookupUtils.createSEDUser(false);
    boolean result = mTestInstance.addSEDUser(init);
    assertTrue(result);

    SEDUser sb2 = mTestInstance.getSEDUserByUserId(init.getUserId());
    assertNotNull(sb2);

    result = mTestInstance.removeSEDUser(sb2);
    assertTrue(result);

    SEDUser sb3 = mTestInstance.getSEDUserByUserId(init.getUserId());
    assertNull(sb3);

  }

  @Test
  public void testUpdateSEDBox() throws Exception {
    System.out.println("updateSEDBox");
    SEDBox init = TestLookupUtils.createSEDBox();
    boolean result = mTestInstance.addSEDBox(init);
    assertTrue(result);
    SEDBox res = mTestInstance.getSEDBoxByLocalName(init.getLocalBoxName());

    assertEquals(init.getLocalBoxName(), res.getLocalBoxName());
    assertEquals(init.getActiveFromDate(), res.getActiveFromDate());
    assertEquals(init.getActiveToDate(), res.getActiveToDate());

    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_YEAR, -12);
    Date dtFrom = c.getTime();
    res.setActiveFromDate(dtFrom);
    res.setActiveToDate(null);
    result = mTestInstance.update(res);
    assertTrue(result);

    SEDBox res2 = mTestInstance.getSEDBoxByLocalName(init.getLocalBoxName());
    assertEquals(dtFrom, res2.getActiveFromDate());
    assertNull(res2.getActiveToDate());

  }

  @Test
  public void testUpdateSEDCronJob() throws Exception {
    System.out.println("updateSEDCronJob");
    for (int ind = 0; ind < 4; ind++) {

      SEDCronJob init = TestLookupUtils.createSEDCronJob();
      boolean result = mTestInstance.addSEDCronJob(init);
      assertTrue(result);

      SEDCronJob res = mTestInstance.getSEDCronJobByName(init.getName());
      // second time create copy of object - test hibernate cache
      if (ind % 2 == 0) {
        res = XMLUtils.deepCopyJAXB(res);
      }
      boolean active = !res.getActive();
      String dayOfWeek = init.getDayOfWeek() + "_test";
      String dayOfMonth = init.getDayOfMonth() + "_test";
      String minute = init.getMinute() + "_test";
      String hour = init.getHour() + "_test";
      String month = init.getMonth() + "_test";

      String taskPlugin = init.getSEDTask().getPlugin() + "_test";
      String taskPluginVersion = init.getSEDTask().getPluginVersion() + "_test";
      String taskType = init.getSEDTask().getType() + "_test";
      String propertyValue = "New propertyValue";
      int iPropSize = res.getSEDTask().getSEDTaskProperties().size();

      res.setActive(active);
      res.setMinute(minute);
      res.setHour(hour);
      res.setMonth(month);
      res.setDayOfMonth(dayOfMonth);
      res.setDayOfWeek(dayOfWeek);
      res.getSEDTask().setPlugin(taskPlugin);
      res.getSEDTask().setPluginVersion(taskPluginVersion);
      res.getSEDTask().setType(taskType);

      // remove or add  one element
      if (ind < 2) {
        res.getSEDTask().
                getSEDTaskProperties().remove(0);
        iPropSize--;
      } else {
        SEDTaskProperty stp = new SEDTaskProperty();
        stp.setKey("new.key");
        stp.setValue(propertyValue);
        res.getSEDTask().
                getSEDTaskProperties().add(stp);
        iPropSize++;
      }
      // change values
      for (int i = 0; i < res.getSEDTask().
              getSEDTaskProperties().size(); i++) {
        res.getSEDTask().getSEDTaskProperties().get(i).setValue(propertyValue);

      }
      result = mTestInstance.updateSEDCronJob(res);
      assertTrue(result);

      SEDCronJob res2 = mTestInstance.getSEDCronJobByName(init.getName());
      assertNotNull(res2);
      assertNotNull(res2.getId());
      assertNotNull(res2.getSEDTask());
      assertEquals(active, res2.getActive());
      assertEquals(dayOfMonth, res2.getDayOfMonth());
      assertEquals(dayOfWeek, res2.getDayOfWeek());
      assertEquals(hour, res2.getHour());
      assertEquals(minute, res2.getMinute());
      assertEquals(month, res2.getMonth());

      assertEquals(taskPlugin, res2.getSEDTask().getPlugin());
      assertEquals(taskPluginVersion, res2.getSEDTask().
              getPluginVersion());
      assertEquals(taskType, res2.getSEDTask().getType());

      assertEquals(iPropSize, res2.getSEDTask().
              getSEDTaskProperties().size());

      for (int i = 0; i < res2.getSEDTask().
              getSEDTaskProperties().size(); i++) {
        assertEquals(propertyValue, res2.getSEDTask().
                getSEDTaskProperties().get(i).getValue());
      }
    }

  }

  @Test
  public void testUpdateSEDInterceptorRule() throws Exception {
    System.out.println("updateSEDInterceptorRule");
    for (int ind = 0; ind < 4; ind++) {
      SEDInterceptorRule init = TestLookupUtils.createSEDInterceptorRule();
      boolean result = mTestInstance.addSEDInterceptorRule(init);
      assertTrue(result);

      SEDInterceptorRule res = mTestInstance.getSEDInterceptorRuleByName(init.
              getName());

      // second time create copy of object - test hibernate cache
      if (ind % 2 == 0) {
        res = XMLUtils.deepCopyJAXB(res);
      }

      boolean active = !init.getActive();
      String action = init.getAction() + "_test";
      String receiverEBox = init.getReceiverEBox() + "_test";
      String senderEBox = init.getSenderEBox() + "_test";
      String service = init.getService() + "_test";
      String interceptEvent = init.getInterceptEvent() + "_test";

      String taskPlugin = init.getSEDInterceptorInstance().getPlugin() + "_test";
      String taskPluginVersion = init.getSEDInterceptorInstance().
              getPluginVersion() + "_test";
      String taskType = init.getSEDInterceptorInstance().getType() + "_test";
      String propertyValue = "New propertyValue";
      int iPropSize = res.getSEDInterceptorInstance().
              getSEDInterceptorProperties().size();

      res.setActive(active);
      res.setAction(action);
      res.setInterceptEvent(interceptEvent);
      res.setService(service);
      res.setReceiverEBox(receiverEBox);
      res.setSenderEBox(senderEBox);

      res.getSEDInterceptorInstance().setPlugin(taskPlugin);
      res.getSEDInterceptorInstance().setPluginVersion(taskPluginVersion);
      res.getSEDInterceptorInstance().setType(taskType);

      // remove or add  one element
      if (ind < 2) {
        res.getSEDInterceptorInstance().
                getSEDInterceptorProperties().remove(0);
        iPropSize--;
      } else {
        SEDInterceptorProperty stp = new SEDInterceptorProperty();
        stp.setKey("new.key");
        stp.setValue(propertyValue);
        res.getSEDInterceptorInstance().
                getSEDInterceptorProperties().add(stp);
        iPropSize++;
      }
      // change values
      for (int i = 0; i < res.getSEDInterceptorInstance().
              getSEDInterceptorProperties().size(); i++) {
        res.getSEDInterceptorInstance().getSEDInterceptorProperties()
                .get(i).setValue(propertyValue);

      }
      result = mTestInstance.updateSEDInterceptorRule(res);
      assertTrue(result);

      SEDInterceptorRule res2 = mTestInstance.getSEDInterceptorRuleByName(init.
              getName());
      assertNotNull(res2);
      assertNotNull(res2.getId());
      assertNotNull(res2.getSEDInterceptorInstance());
      assertEquals(active, res2.getActive());
      assertEquals(interceptEvent, res2.getInterceptEvent());
      assertEquals(action, res2.getAction());
      assertEquals(service, res2.getService());
      assertEquals(senderEBox, res2.getSenderEBox());
      assertEquals(receiverEBox, res2.getReceiverEBox());

      assertEquals(taskPlugin, res2.getSEDInterceptorInstance().getPlugin());
      assertEquals(taskPluginVersion, res2.getSEDInterceptorInstance().
              getPluginVersion());
      assertEquals(taskType, res2.getSEDInterceptorInstance().getType());

      assertEquals(iPropSize, res2.getSEDInterceptorInstance().
              getSEDInterceptorProperties().size());

      for (int i = 0; i < res2.getSEDInterceptorInstance().
              getSEDInterceptorProperties().size(); i++) {
        assertEquals(propertyValue, res2.getSEDInterceptorInstance().
                getSEDInterceptorProperties().get(i).getValue());
      }
    }
  }

  @Test
  public void testUpdateSEDProcessorRule() throws Exception {
    System.out.println("updateSEDProcessorRule");
    SEDProcessorRule sb = null;

    int iPSize = mTestInstance.getSEDProcessorRules().size();
    SEDProcessorRule init = TestLookupUtils.createSEDProcessorRule();
    boolean result = mTestInstance.addSEDProcessorRule(init);
    assertTrue(result);
    assertEquals(iPSize + 1, mTestInstance.getSEDProcessorRules().size());

    SEDProcessorRule res = null;
    for (SEDProcessorRule pr : mTestInstance.getSEDProcessorRules()) {
      // test code is unique
      if (Objects.equals(pr.getProcSetCode(), init.getProcSetCode())) {
        res = pr;
        break;
      }
    }
    assertNotNull(res);
    assertNotNull(res.getId());
    res = XMLUtils.deepCopyJAXB(res);

    String action = init.getAction() + "_test";
    String receiverEBox = init.getReceiverEBox() + "_test";
    String senderEBox = init.getSenderEBox() + "_test";
    String service = init.getService() + "_test";

    res.setAction(action);
    res.setReceiverEBox(receiverEBox);
    res.setSenderEBox(senderEBox);
    res.setService(service);

    mTestInstance.updateSEDProcessorRule(res);

    SEDProcessorRule res2 = mTestInstance.getSEDProcessorRule(res.getId());
    assertNotNull(res2);
    assertEquals(action, res2.getAction());
    assertEquals(receiverEBox, res2.getReceiverEBox());
    assertEquals(senderEBox, res2.getSenderEBox());
    assertEquals(service, res2.getService());

  }

  @Test
  public void testUpdateSEDProcessorSet() throws Exception {
    System.out.println("updateSEDProcessorSet");
    for (int ind = 0; ind < 4; ind++) {
      SEDProcessorSet init = TestLookupUtils.createSEDProcessorSet();
      boolean result = mTestInstance.addSEDProcessorSet(init);
      assertTrue(result);

      SEDProcessorSet res = mTestInstance.getSEDProcessorSet(init.
              getCode());
      assertNotNull(res);
      // second time create copy of object - test hibernate cache
      if (ind % 2 == 0) {
        res = XMLUtils.deepCopyJAXB(res);
      }

      String name = init.getName() + "_test";
      String description = init.getDescription() + "_test";
      String instance = "NewInstance";
      int iPropSize = res.getSEDProcessorInstances().size();

      res.setName(name);
      res.setDescription(description);

      // remove or add  one element
      if (ind < 2) {
        res.getSEDProcessorInstances().remove(0);
        iPropSize--;
      } else {
        SEDProcessorInstance stp = new SEDProcessorInstance();
        stp.setPlugin("plugin");
        stp.setPluginVersion("pluginVersion");
        stp.setPluginVersion("version");
        stp.setInstance(instance);

        res.getSEDProcessorInstances().add(stp);
        iPropSize++;
      }

      // change values
      for (int i = 0; i < res.getSEDProcessorInstances().size(); i++) {
        res.getSEDProcessorInstances().get(i).setInstance(instance);

      }
      result = mTestInstance.updateSEDProcessorSet(res);
      assertTrue(result);

      SEDProcessorSet res2 = mTestInstance.getSEDProcessorSet(init.
              getCode());

      assertNotNull(res2);
      assertEquals(init.getCode(), res2.getCode());
      assertEquals(name, res2.getName());
      assertEquals(description, res2.getDescription());

      assertEquals(iPropSize,
              res2.getSEDProcessorInstances().size());

      for (int i = 0; i < iPropSize; i++) {
        assertEquals(instance,
                res2.getSEDProcessorInstances().get(i).getInstance());

      }

    }
  }

  @Test
  public void testUpdateSEDUser() throws Exception {
    System.out.println("updateSEDUser");
    SEDUser init = TestLookupUtils.createSEDUser(true);
    // persist setboxes
    for (SEDBox sb : init.getSEDBoxes()) {
      mTestInstance.addSEDBox(sb);
    }

    boolean result = mTestInstance.addSEDUser(init);
    assertTrue(result);

    SEDUser res = mTestInstance.getSEDUserByUserId(init.getUserId());
    res = XMLUtils.deepCopyJAXB(res);
    String name = res.getName() + "_test";
    String desc = res.getDesc() + "_test";
    String email = res.getEmail() + "_test";

    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_YEAR, -12);
    Date dtFrom = c.getTime();
    Date dtTo = null;

    res.setActiveFromDate(dtFrom);
    res.setActiveToDate(dtTo);

    res.setDesc(desc);
    res.setEmail(email);
    res.setName(name);

    mTestInstance.updateSEDUser(res);

    SEDUser res3 = mTestInstance.getSEDUserByUserId(init.
            getUserId());

    assertNotNull(res3);
    assertEquals(name, res3.getName());
    assertEquals(init.getUserId(), res3.getUserId());
    assertEquals(dtFrom, res3.getActiveFromDate());
    assertEquals(dtTo, res3.getActiveToDate());
    assertEquals(desc, res3.getDesc());
    assertEquals(email, res3.getEmail());
    assertEquals(init.getSEDBoxes().size(), res3.getSEDBoxes().size());

  }

}
