/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb.utils;

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
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.interceptor.SEDInterceptorRule;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessorProperty;
import si.laurentius.process.SEDProcessor;
import si.laurentius.process.SEDProcessorRule;
import si.laurentius.rule.SEDDecisionRule;

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

  public static <T> T fillDecisionRule(String prp, T drule) {
    SEDDecisionRule dr = (SEDDecisionRule) drule;
    dr.setProperty(prp);
    dr.setValue(prp + "value");
    dr.setPredicate("=");
    return drule;

  }

  static public SEDInterceptor createSEDInterceptor() {

    SEDInterceptor sb = new SEDInterceptor();
    sb.setName(Utils.getUUID("INT"));
    sb.setActive(true);

    sb.setInterceptEvent("InterceptEvent");
    sb.setInterceptRole("InterceptRole");
    sb.getSEDInterceptorRules().add(fillDecisionRule("action",
            new SEDInterceptorRule()));
    sb.getSEDInterceptorRules().add(fillDecisionRule("service",
            new SEDInterceptorRule()));
    sb.getSEDInterceptorRules().add(fillDecisionRule("receiverEBox",
            new SEDInterceptorRule()));
    sb.getSEDInterceptorRules().add(fillDecisionRule("senderEBox",
            new SEDInterceptorRule()));

    SEDInterceptorInstance st = new SEDInterceptorInstance();
    st.setPlugin("PLUGIN");
    st.setPluginVersion("VERSION");
    st.setType("TYPE");
    int iPropSize = RANDOM_VALUE.nextInt(10) + 3;
    for (int i = 0; i < iPropSize; i++) {
      SEDInterceptorProperty sp = new SEDInterceptorProperty();
      sp.setKey(String.format("test.key.%02d", i));
      sp.setValue(String.format("value_%02d", i));
      st.getSEDInterceptorProperties().add(sp);
    }
    sb.setSEDInterceptorInstance(st);;
    return sb;
  }

  static public SEDProcessor createSEDProcessor() {
    SEDProcessor sb = new SEDProcessor();
    
    sb.setName(Utils.getUUID("PROC"));
    sb.setActive(true);
    sb.setDeliveredOnSuccess(true);
    sb.getSEDProcessorRules().add(fillDecisionRule("action",
            new SEDProcessorRule()));
    sb.getSEDProcessorRules().add(fillDecisionRule("service",
            new SEDProcessorRule()));
    sb.getSEDProcessorRules().add(fillDecisionRule("receiverEBox",
            new SEDProcessorRule()));
    sb.getSEDProcessorRules().add(fillDecisionRule("senderEBox",
            new SEDProcessorRule()));

    int iInstSize = RANDOM_VALUE.nextInt(10) + 3;
    for (int i = 0; i < iInstSize; i++) {
      SEDProcessorInstance pi = createSEDProcessorInstance();
      sb.getSEDProcessorInstances().add(pi);
    }

    return sb;
  }

  static public SEDProcessorInstance createSEDProcessorInstance() {

    SEDProcessorInstance pi = new SEDProcessorInstance();

    pi.setPlugin(Utils.getUUID("PLG"));
    pi.setPluginVersion(RANDOM_VALUE.nextInt(100) + "");
    pi.setType(Utils.getUUID("TYPE"));

    int iPropSize = RANDOM_VALUE.nextInt(10) + 3;
    for (int j = 0; j < iPropSize; j++) {
      SEDProcessorProperty sp = new SEDProcessorProperty();
      sp.setKey(String.format("test.key.%02d", j));
      sp.setValue(String.format("value_%02d", j));
      pi.getSEDProcessorProperties().add(sp);
    }

    return pi;
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
      int iboxSize = RANDOM_VALUE.nextInt(10) + 3;
      for (int i = 0; i < iboxSize; i++) {
        SEDBox pi = createSEDBox();
        sb.getSEDBoxes().add(pi);
      }
    }
    return sb;
  }

}
