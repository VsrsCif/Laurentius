/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import si.laurentius.commons.utils.Utils;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTask;
import si.laurentius.cron.SEDTaskProperty;
import si.laurentius.ebox.SEDBox;
import si.laurentius.interceptor.SEDInterceptorInstance;
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
public class TestLookupUtils {

  public static Random RANDOM_VALUE = new Random(Calendar.getInstance().
          getTimeInMillis());

  static public SEDBox createSEDBox() {
    String localName = "test." + Utils.getUUID("name");
    Calendar c = Calendar.getInstance();
    Date dtFrom = c.getTime();
    c.add(Calendar.MONTH, 1);
    Date dtTo = c.getTime();

    SEDBox sb = new SEDBox();
    sb.setLocalBoxName(localName);
    sb.setActiveFromDate(dtFrom);
    sb.setActiveToDate(dtTo);
    return sb;

  }

  static public SEDCronJob createSEDCronJob() {

    SEDCronJob sb = new SEDCronJob();
    sb.setName(Utils.getUUID("CronJob"));
    sb.setActive(true);
    sb.setDayOfMonth("1");
    sb.setDayOfWeek("2");
    sb.setHour("3");
    sb.setMinute("4");
    sb.setMonth("5");

    SEDTask st = new SEDTask();
    st.setPlugin("PLUGIN");
    st.setPluginVersion("VERSION");
    st.setType("TYPE");

    int iPropSize = RANDOM_VALUE.nextInt(10) + 3;

    for (int i = 0; i < iPropSize; i++) {
      SEDTaskProperty sp = new SEDTaskProperty();
      sp.setKey(String.format("test.key.%02d", i));
      sp.setValue(String.format("value_%02d", i));
      st.getSEDTaskProperties().add(sp);
    }

    sb.setSEDTask(st);

    return sb;

  }

  static public SEDInterceptorRule createSEDInterceptorRule() {

    SEDInterceptorRule sb = new SEDInterceptorRule();
    sb.setName(Utils.getUUID("INT"));
    sb.setActive(true);
    sb.setAction("Action");
    sb.setInterceptEvent("InterceptEvent");
    sb.setService("Service");
    sb.setReceiverEBox("ReceiverEBox");
    sb.setSenderEBox("SenderEBox");

    SEDInterceptorInstance st = new SEDInterceptorInstance();
    st.setPlugin("PLUGIN");
    st.setPluginVersion("VERSION");
    st.setType("TYPE");
    int iPropSize = RANDOM_VALUE.nextInt(10)  + 3;
    for (int i = 0; i < iPropSize; i++) {
      SEDInterceptorProperty sp = new SEDInterceptorProperty();
      sp.setKey(String.format("test.key.%02d", i));
      sp.setValue(String.format("value_%02d", i));
      st.getSEDInterceptorProperties().add(sp);
    }
    sb.setSEDInterceptorInstance(st);;
    return sb;
  }

  static public SEDProcessorRule createSEDProcessorRule() {
    SEDProcessorRule sb = new SEDProcessorRule();
    sb.setProcSetCode(Utils.getUUID("PRC"));
    sb.setAction("Action");
    sb.setService("Service");
    sb.setReceiverEBox("ReceiverEBox");
    sb.setSenderEBox("SenderEBox");

    return sb;
  }

  static public SEDProcessorSet createSEDProcessorSet() {
    SEDProcessorSet sb = new SEDProcessorSet();
    sb.setCode(Utils.getUUID("PSET"));
    sb.setName(Utils.getUUID("Name"));
    sb.setDescription("Description");
    int iPropSize = RANDOM_VALUE.nextInt(10)  + 3;
    for (int i = 0; i < iPropSize; i++) {
      SEDProcessorInstance pi = new SEDProcessorInstance();
      pi.setInstance(String.format("instnance_%02d", i));
      pi.setPlugin(String.format("plugin_%02d", i));
      pi.setPluginVersion(String.format("version_%02d", i));
      pi.setType(String.format("type_%02d", i));

      sb.getSEDProcessorInstances().add(pi);
    }
    return sb;
  }

  static public SEDUser createSEDUser(boolean withBoxes) {
    SEDUser sb = new SEDUser();

    Calendar c = Calendar.getInstance();
    Date dtFrom = c.getTime();
    c.add(Calendar.MONTH, 1);
    Date dtTo = c.getTime();

    sb.setName(Utils.getUUID("name"));
    sb.setActiveFromDate(dtFrom);
    sb.setActiveToDate(dtTo);
    sb.setUserId(Utils.getUUID("id"));
    sb.setDesc("Description");
    sb.setEmail("Email");
    sb.setAdminRole(true);
    if (withBoxes) {
      int iboxSize = RANDOM_VALUE.nextInt(10)  + 3;
      for (int i = 0; i < iboxSize; i++) {
        SEDBox pi = createSEDBox();
        sb.getSEDBoxes().add(pi);
      }
    }
    return sb;
  }

}
