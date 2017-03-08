/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb;

import si.laurentius.ejb.utils.TestUtils;
import si.laurentius.ejb.utils.TestLookupUtils;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.interceptor.SEDInterceptorRule;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessorProperty;
import si.laurentius.process.SEDProcessor;
import si.laurentius.process.SEDProcessorRule;
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

    SEDBox init = TestLookupUtils.createSEDBox();
    boolean result = mTestInstance.addSEDBox(init);
    assertTrue(result);

    // force to read from db
    mTestInstance.clearAllCache();
    mTestInstance.memEManager.clear();

    SEDBox res = mTestInstance.getSEDBoxByLocalName(init.getLocalBoxName());
    assertNotEquals(init, res);
    assertNotNull(res);
    assertEquals(init.getLocalBoxName(), res.getLocalBoxName());
    assertEquals(init.getActiveFromDate(), res.getActiveFromDate());
    assertEquals(init.getActiveToDate(), res.getActiveToDate());

  }

  @Test
  public void testAddSEDCronJob() throws Exception {
    System.out.println("addSEDCronJob");
    SEDCronJob init = TestLookupUtils.createSEDCronJob();

    boolean result = mTestInstance.addSEDCronJob(init);
    assertTrue(result);

    // force to read from db
    mTestInstance.clearAllCache();
    mTestInstance.memEManager.clear();

    SEDCronJob res = mTestInstance.getSEDCronJobByName(init.getName());
    assertNotEquals(init, res);

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
  public void testAddSEDInterceptor() throws Exception {
    System.out.println("addSEDInterceptor");
    SEDInterceptor init = TestLookupUtils.createSEDInterceptor();
    boolean result = mTestInstance.addSEDInterceptor(init);
    assertTrue(result);

    // force to read from db
    mTestInstance.clearAllCache();
    mTestInstance.memEManager.clear();

    SEDInterceptor res = mTestInstance.getSEDInterceptorByName(init.
            getName());
    assertNotEquals(init, res);

    assertNotNull(res);
    assertNotNull(res.getId());
    assertNotNull(res.getSEDInterceptorInstance());
    assertEquals(init.getActive(), res.getActive());
    assertEquals(init.getName(), res.getName());
    assertEquals(init.getInterceptEvent(), res.getInterceptEvent());
    assertEquals(init.getInterceptRole(), res.getInterceptRole());

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

    for (int i = 0; i < init.getSEDInterceptorRules().size(); i++) {
      assertEquals(init.getSEDInterceptorRules().get(i).getProperty(), res.
              getSEDInterceptorRules().get(i).getProperty());
      assertEquals(init.getSEDInterceptorRules().get(i).getPredicate(), res.
              getSEDInterceptorRules().get(i).getPredicate());
      assertEquals(init.getSEDInterceptorRules().get(i).getValue(), res.
              getSEDInterceptorRules().get(i).getValue());
    }

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
  public void testAddSEDProcessor() throws Exception {
    System.out.println("addSEDProcessor");

    int iPSize = mTestInstance.getSEDProcessors().size();
    SEDProcessor init = TestLookupUtils.createSEDProcessor();
    boolean result = mTestInstance.addSEDProcessor(init);
    assertTrue(result);
    assertEquals(iPSize + 1, mTestInstance.getSEDProcessors().size());

    // force to read from db
    mTestInstance.clearAllCache();
    mTestInstance.memEManager.clear();

    SEDProcessor res = mTestInstance.getSEDProcessorByName(init.
            getName());
    // check if cached object
    assertNotEquals(init, res);

    assertNotNull(res);
    assertNotNull(res.getId());
    assertEquals(init.getName(), res.getName());
    assertEquals(init.getActive(), res.getActive());
    assertEquals(init.getDeliveredOnSuccess(), res.getDeliveredOnSuccess());
    assertEquals(init.getSEDProcessorInstances().size(), res.
            getSEDProcessorInstances().size());

    for (int i = 0; i < init.getSEDProcessorRules().size(); i++) {
      assertEquals(init.getSEDProcessorRules().get(i).getProperty(), res.
              getSEDProcessorRules().get(i).getProperty());
      assertEquals(init.getSEDProcessorRules().get(i).getPredicate(), res.
              getSEDProcessorRules().get(i).getPredicate());
      assertEquals(init.getSEDProcessorRules().get(i).getValue(), res.
              getSEDProcessorRules().get(i).getValue());
    }

    for (int i = 0; i < init.getSEDProcessorInstances().size(); i++) {
      assertNotNull(res.getSEDProcessorInstances().get(i).getId());
      assertEquals(init.getSEDProcessorInstances().get(i).getPlugin(),
              res.getSEDProcessorInstances().get(i).getPlugin());

      assertEquals(init.getSEDProcessorInstances().get(i).getPluginVersion(),
              res.getSEDProcessorInstances().get(i).getPluginVersion());

      assertEquals(init.getSEDProcessorInstances().get(i).getType(),
              res.getSEDProcessorInstances().get(i).getType());

      assertEquals(init.getSEDProcessorInstances().get(i).
              getSEDProcessorProperties().size(), res.
                      getSEDProcessorInstances().get(i).
                      getSEDProcessorProperties().size());

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

    // force to read from db
    mTestInstance.clearAllCache();
    mTestInstance.memEManager.clear();

    SEDUser res = mTestInstance.getSEDUserByUserId(init.getUserId());
    assertNotEquals(init, res);
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
  public void testGetSEDInterceptorById() throws Exception {
    System.out.println("getSEDInterceptorById");

    SEDInterceptor init = TestLookupUtils.createSEDInterceptor();
    boolean result = mTestInstance.addSEDInterceptor(init);
    assertTrue(result);

    SEDInterceptor rs = mTestInstance.getSEDInterceptorByName(init.
            getName());
    SEDInterceptor rs2 = mTestInstance.getSEDInterceptorById(
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
  public void testGetSEDInterceptors() throws Exception {
    System.out.println("getSEDInterceptors");
    int iSize = mTestInstance.getSEDInterceptors().size();
    int iCnt = 4;
    for (int i = 0; i < iCnt; i++) {
      mTestInstance.addSEDInterceptor(TestLookupUtils.
              createSEDInterceptor());
    }
    assertEquals(iSize + iCnt, mTestInstance.getSEDInterceptors().size());

  }

  @Test
  public void testGetSEDProcessor() throws Exception {
    System.out.println("getSEDProcessor");
    mTestInstance.addSEDProcessor(TestLookupUtils.createSEDProcessor());
    SEDProcessor rs = mTestInstance.getSEDProcessors().get(0);
    SEDProcessor rs2 = mTestInstance.getSEDProcessor(rs.getId());

    assertEquals(rs, rs2);

  }

  @Test
  public void testGetSEDProcessors() throws Exception {
    int iSize = mTestInstance.getSEDProcessors().size();
    int iCnt = 4;
    for (int i = 0; i < iCnt; i++) {
      mTestInstance.
              addSEDProcessor(TestLookupUtils.createSEDProcessor());
    }
    assertEquals(iSize + iCnt, mTestInstance.getSEDProcessors().size());
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
  public void testRemoveSEDInterceptor() throws Exception {
    System.out.println("removeSEDInterceptor");
    SEDInterceptor init = TestLookupUtils.createSEDInterceptor();
    boolean result = mTestInstance.addSEDInterceptor(init);
    assertTrue(result);

    SEDInterceptor sb2 = mTestInstance.getSEDInterceptorByName(init.
            getName());
    assertNotNull(sb2);

    result = mTestInstance.removeSEDInterceptor(sb2);
    assertTrue(result);

    SEDInterceptor sb3 = mTestInstance.getSEDInterceptorByName(init.
            getName());
    assertNull(sb3);

  }

  @Test
  public void testRemoveSEDProcessor() throws Exception {
    System.out.println("removeSEDProcessor");
    SEDProcessor init = TestLookupUtils.createSEDProcessor();
    boolean result = mTestInstance.addSEDProcessor(init);
    assertTrue(result);

    SEDProcessor sb2 = mTestInstance.getSEDProcessors().get(0);
    assertNotNull(sb2);

    result = mTestInstance.removeSEDProcessor(sb2);
    assertTrue(result);

    SEDProcessor sb3 = mTestInstance.getSEDProcessor(sb2.getId());
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
  public void testUpdateSEDInterceptor() throws Exception {
    System.out.println("updateSEDInterceptor");
    for (int ind = 0; ind < 4; ind++) {
      SEDInterceptor init = TestLookupUtils.createSEDInterceptor();
      boolean result = mTestInstance.addSEDInterceptor(init);
      assertTrue(result);

      SEDInterceptor res = mTestInstance.getSEDInterceptorByName(init.
              getName());

      // second time create copy of object - test hibernate cache
      if (ind % 2 == 0) {
        res = XMLUtils.deepCopyJAXB(res);
      }

      boolean active = !init.getActive();

      String interceptEvent = init.getInterceptEvent() + "_test";
      String interceptRole = init.getInterceptRole() + "_test";

      String taskPlugin = init.getSEDInterceptorInstance().getPlugin() + "_test";
      String taskPluginVersion = init.getSEDInterceptorInstance().
              getPluginVersion() + "_test";
      String taskType = init.getSEDInterceptorInstance().getType() + "_test";
      String propertyValue = "New propertyValue";
      String ruleProperty = "NewRule";
      String rulePredicate = "!=";
      String ruleValue = "New Value";

      int iPropSize = res.getSEDInterceptorInstance().
              getSEDInterceptorProperties().size();
      int iRuleSize = res.getSEDInterceptorRules().size();

      res.setActive(active);
      res.setInterceptEvent(interceptEvent);
      res.setInterceptRole(interceptRole);
      
      for (int i = 0; i < res.getSEDInterceptorRules().size(); i++) {
        res.getSEDInterceptorRules().get(i).setProperty(ruleProperty);
        res.getSEDInterceptorRules().get(i).setPredicate(rulePredicate);
        res.getSEDInterceptorRules().get(i).setValue(ruleValue);
      }

      res.getSEDInterceptorInstance().setPlugin(taskPlugin);
      res.getSEDInterceptorInstance().setPluginVersion(taskPluginVersion);
      res.getSEDInterceptorInstance().setType(taskType);

      // remove or add  one element
      if (ind < 2) {
        res.getSEDInterceptorInstance().
                getSEDInterceptorProperties().remove(0);

        iPropSize--;
        
        res.getSEDInterceptorRules().remove(0);
        iRuleSize--;
      } else {
        SEDInterceptorProperty stp = new SEDInterceptorProperty();
        stp.setKey("new.key");
        stp.setValue(propertyValue);
        res.getSEDInterceptorInstance().
                getSEDInterceptorProperties().add(stp);
        iPropSize++;
        
        SEDInterceptorRule sir = new SEDInterceptorRule();
        sir.setPredicate(rulePredicate);
        sir.setProperty(ruleProperty);
        sir.setValue(ruleValue);
        res.getSEDInterceptorRules().add(sir);
        iRuleSize++;
                
      }
      // change values
      for (int i = 0; i < res.getSEDInterceptorInstance().
              getSEDInterceptorProperties().size(); i++) {
        res.getSEDInterceptorInstance().getSEDInterceptorProperties()
                .get(i).setValue(propertyValue);

      }
      result = mTestInstance.updateSEDInterceptor(res);
      assertTrue(result);

      SEDInterceptor res2 = mTestInstance.getSEDInterceptorByName(init.
              getName());
      assertNotNull(res2);
      assertNotNull(res2.getId());
      assertNotNull(res2.getSEDInterceptorInstance());
      assertEquals(active, res2.getActive());
      assertEquals(interceptEvent, res2.getInterceptEvent());
      assertEquals(interceptRole, res2.getInterceptRole());
      assertEquals(iRuleSize, res2.getSEDInterceptorRules().size());

      for (int i = 0; i < init.getSEDInterceptorRules().size(); i++) {
        assertEquals(ruleProperty, res.getSEDInterceptorRules().get(i).
                getProperty());
        assertEquals(rulePredicate, res.getSEDInterceptorRules().get(i).
                getPredicate());
        assertEquals(ruleValue, res.getSEDInterceptorRules().get(i).
                getValue());
      }

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
  public void testUpdateSEDProcessor() throws Exception {
    System.out.println("updateSEDProcessor");
    SEDProcessor sb = null;
    for (int ind = 0; ind < 4; ind++) {
      int iPSize = mTestInstance.getSEDProcessors().size();
      SEDProcessor init = TestLookupUtils.createSEDProcessor();
      boolean result = mTestInstance.addSEDProcessor(init);
      assertTrue(result);
      assertEquals(iPSize + 1, mTestInstance.getSEDProcessors().size());

      SEDProcessor res = mTestInstance.getSEDProcessorByName(init.
              getName());
      assertNotNull(res);
      assertNotNull(res.getId());

      // second time create copy of object - test hibernate cache
      if (ind % 2 == 0) {
        res = XMLUtils.deepCopyJAXB(res);
      }
      
     
     
      String name = res.getName()+"_test";
      boolean bActive = !res.getActive();
      boolean bDOS = !res.getDeliveredOnSuccess();
      
      String ruleProperty = "NewRule";
      String rulePredicate = "!=";
      String ruleValue = "New Value";

      int iInstSize = res.getSEDProcessorInstances().size();
      int iRuleSize = res.getSEDProcessorRules().size();
      
      res.setName(name);
      res.setActive(bActive);
      res.setDeliveredOnSuccess(bDOS);

      for (int i = 0; i < res.getSEDProcessorRules().size(); i++) {
        res.getSEDProcessorRules().get(i).setProperty(ruleProperty);
        res.getSEDProcessorRules().get(i).setPredicate(rulePredicate);
        res.getSEDProcessorRules().get(i).setValue(ruleValue);
      }

      // remove or add  one element
      if (ind < 2) {
        res.getSEDProcessorInstances().remove(0);
        iInstSize--;
        
        res.getSEDProcessorRules().remove(0);
        iRuleSize--;
      } else {
        SEDProcessorInstance stp = TestLookupUtils.createSEDProcessorInstance();
        res.getSEDProcessorInstances().add(stp);
        iInstSize++;
        
        SEDProcessorRule sir = new SEDProcessorRule();
        sir.setPredicate(rulePredicate);
        sir.setProperty(ruleProperty);
        sir.setValue(ruleValue);
        res.getSEDProcessorRules().add(sir);
        iRuleSize++;
      }

      result = mTestInstance.updateSEDProcessor(res);
      assertTrue(result);
      SEDProcessor res2 = mTestInstance.getSEDProcessor(res.getId());
      assertNotNull(res2);
      
      assertEquals(name, res2.getName());
      assertEquals(bActive, res2.getActive());
      assertEquals(bDOS, res2.getDeliveredOnSuccess());
      
      assertEquals(iInstSize, res2.getSEDProcessorInstances().size());

      assertEquals(iRuleSize, res2.getSEDProcessorRules().size());

      for (int i = 0; i < init.getSEDProcessorRules().size(); i++) {
        assertEquals(ruleProperty, res.getSEDProcessorRules().get(i).
                getProperty());
        assertEquals(rulePredicate, res.getSEDProcessorRules().get(i).
                getPredicate());
        assertEquals(ruleValue, res.getSEDProcessorRules().get(i).
                getValue());
      }

    }
  }

  @Test
  public void testUpdateSEDProcessorInstaceOrder() throws Exception {
    System.out.println("testUpdateSEDProcessorInstaceOrder");
    SEDProcessor init = TestLookupUtils.createSEDProcessor();

    while (init.getSEDProcessorInstances().size() < 5) {
      init.getSEDProcessorInstances().add(TestLookupUtils.
              createSEDProcessorInstance());
    }
    boolean result = mTestInstance.addSEDProcessor(init);
    assertTrue(result);

    SEDProcessor res = mTestInstance.getSEDProcessorByName(init.
            getName());

    List<BigInteger> lstID = new ArrayList<>();
    for (SEDProcessorInstance pi : res.getSEDProcessorInstances()) {
      lstID.add(pi.getId());
    }

    SEDProcessorInstance mv = res.getSEDProcessorInstances().remove(3);
    BigInteger bi = lstID.remove(3);

    res.getSEDProcessorInstances().add(1, mv);
    lstID.add(1, bi);

    result = mTestInstance.updateSEDProcessor(res);
    assertTrue(result);

    SEDProcessor res3 = mTestInstance.getSEDProcessorByName(init.
            getName());

    for (int i = 0; i < lstID.size(); i++) {
      assertEquals(lstID.get(i), res3.getSEDProcessorInstances().get(i).getId());
    }

  }

  @Test
  public void updateSEDProcessorInstance() throws Exception {
    SEDProcessor init = TestLookupUtils.createSEDProcessor();
    boolean result = mTestInstance.addSEDProcessor(init);
    assertTrue(result);

    SEDProcessorInstance res = mTestInstance.getSEDProcessorByName(init.
            getName()).getSEDProcessorInstances().get(0);

    for (int ind = 0; ind < 4; ind++) {
      String taskPlugin = res.getPlugin() + "_test";
      String taskPluginVersion = res.
              getPluginVersion() + "_test";
      String taskType = res.getType() + "_test";
      String propertyValue = "New propertyValue";
      int iPropSize = res.getSEDProcessorProperties().size();

      // second time create copy of object - test hibernate cache
      if (ind % 2 == 0) {
        res = XMLUtils.deepCopyJAXB(res);
      }

      res.setPlugin(taskPlugin);
      res.setPluginVersion(taskPluginVersion);
      res.setType(taskType);
      // change values
      for (int i = 0; i < res.getSEDProcessorProperties().size(); i++) {
        res.getSEDProcessorProperties().get(i).setValue(propertyValue);

      }

      // remove or add  one element
      if (ind < 2) {
        res.getSEDProcessorProperties().remove(0);
        iPropSize--;
      } else {
        SEDProcessorProperty stp = new SEDProcessorProperty();
        stp.setKey("new.key");
        stp.setValue(propertyValue);
        res.getSEDProcessorProperties().add(stp);
        iPropSize++;
      }

      result = mTestInstance.updateSEDProcessorInstance(res);
      assertTrue(result);
      SEDProcessor rl2 = mTestInstance.getSEDProcessor(init.getId());
      SEDProcessorInstance res2 = null;
      for (SEDProcessorInstance pri : rl2.getSEDProcessorInstances()) {
        if (Objects.equals(pri.getId(), res.getId())) {
          res2 = pri;
          break;
        }
      }
      assertNotNull(res2);
      assertEquals(taskPlugin, res2.getPlugin());
      assertEquals(taskPluginVersion, res2.getPluginVersion());
      assertEquals(taskType, res2.getType());
      assertEquals(iPropSize, res2.getSEDProcessorProperties().size());
      for (int i = 0; i < res2.getSEDProcessorProperties().size(); i++) {
        assertEquals(propertyValue, res2.getSEDProcessorProperties().get(i).
                getValue());
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
