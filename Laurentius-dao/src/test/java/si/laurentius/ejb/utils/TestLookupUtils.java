/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb.utils;

import java.io.File;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import si.laurentius.application.SEDApplication;
import si.laurentius.cert.SEDCertPassword;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTask;
import si.laurentius.cron.SEDTaskProperty;
import si.laurentius.ebox.SEDBox;
import si.laurentius.interceptor.SEDInterceptorInstance;
import si.laurentius.interceptor.SEDInterceptorProperty;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.interceptor.SEDInterceptorRule;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.IMPartProperty;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.outbox.payload.OMPartProperty;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessorProperty;
import si.laurentius.process.SEDProcessor;
import si.laurentius.process.SEDProcessorRule;
import si.laurentius.property.SEDProperty;
import si.laurentius.rule.SEDDecisionRule;

import si.laurentius.user.SEDUser;

/**
 *
 * @author sluzba
 */
public class TestLookupUtils {
  
  
  public static final String FILEBLOB_1 = "Test content 1";
  public static final String FILEBLOB_2 = "Test content 2";
  
  public static final StorageUtils S_STORAGE_UTIL  = new StorageUtils();

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
    
     int iInstSize = RANDOM_VALUE.nextInt(10) + 3;
    for (int i = 0; i < iInstSize; i++) {
      SEDTask tsk = createSEDTask();
      sb.getSEDTasks().add(tsk);
    }

   
    return sb;

  }
  
  public static SEDTask createSEDTask() {
    SEDTask st = new SEDTask();
    st.setPlugin(UUID.randomUUID().toString());
    st.setPluginVersion("VERSION");
    st.setType("TYPE");
    st.setActive(Boolean.TRUE);
    st.setName(UUID.randomUUID().toString());
    int iPropSize = RANDOM_VALUE.nextInt(10) + 3;

    for (int i = 0; i < iPropSize; i++) {
      SEDTaskProperty sp = new SEDTaskProperty();
      sp.setKey(String.format("test.key.%02d", i));
      sp.setValue(String.format("value_%02d", i));
      st.getSEDTaskProperties().add(sp);
    }
    return st;
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

  static public SEDApplication createSEDApplication(boolean withBoxes) {
    SEDApplication sb = new SEDApplication();

    Calendar c = Calendar.getInstance();
    Date dtFrom = c.getTime();
    c.add(Calendar.MONTH, 1);
    Date dtTo = c.getTime();

    sb.setName(Utils.getUUID("name"));
    sb.setActiveFromDate(dtFrom);
    sb.setActiveToDate(dtTo);
    sb.setApplicationId(Utils.getUUID("id"));
    sb.setDesc("Description");
    sb.setEmail("Email");

    if (withBoxes) {
      int iboxSize = RANDOM_VALUE.nextInt(10) + 3;
      for (int i = 0; i < iboxSize; i++) {
        SEDBox pi = createSEDBox();
        sb.getSEDBoxes().add(pi);
      }
    }
    return sb;
  }

  static public SEDCertPassword createSEDCertPassword() {
    SEDCertPassword entity = new SEDCertPassword();
    entity.setAlias(UUID.randomUUID().toString());
    entity.setKeyPassword(true);
    entity.setPassword(UUID.randomUUID().toString());
    return entity;
  }

  static public SEDProperty createSEDProperty() {
    SEDProperty entity = new SEDProperty();
    entity.setGroup(UUID.randomUUID().toString().substring(0, 32));
    entity.setKey(UUID.randomUUID().toString());
    entity.setValue(UUID.randomUUID().toString());
    return entity;
  }

  public static MSHOutMail createOutMail() throws StorageException {

    MSHOutMail om = new MSHOutMail();

    om.setSenderMessageId("SM_ID-" + UUID.randomUUID().toString());
    om.setAction("DeliveryNotification");
    om.setService("LegalDelivery_ZPP");
    om.setConversationId(UUID.randomUUID().toString());
    om.setReceiverName("Mr. Receiver Name");
    om.setReceiverEBox("receiver.name@test-sed.si");
    om.setSenderName("Mr. Sender Name");
    om.setSenderEBox("izvrsba@test-sed.si");

    om.setMSHOutPayload(new MSHOutPayload());
    MSHOutPart op = new MSHOutPart();
    op.setFilename("Test.txt");
    op.setDescription("test attachment");
    op.setIsSent(Boolean.TRUE);
    op.setIsReceived(Boolean.FALSE);
    op.setGeneratedFromPartId(BigInteger.ONE);;
    
    File f =  S_STORAGE_UTIL.storeOutFile(MimeValue.MIME_TXT.getMimeType(), FILEBLOB_1.getBytes());
    op.setFilepath(StorageUtils.getRelativePath(f));
    op.setMimeType(MimeValue.MIME_TEXI.getMimeType());

    OMPartProperty iprop1 = new OMPartProperty();
    iprop1.setName("Property 1");
    iprop1.setValue("value");
    OMPartProperty iprop2 = new OMPartProperty();
    iprop2.setName("Property 2");
    iprop2.setValue("value");

    op.getOMPartProperties().add(iprop1);
    op.getOMPartProperties().add(iprop2);

    om.getMSHOutPayload().getMSHOutParts().add(op);

    return om;

  }
  public static MSHInMail createInMail() throws StorageException {

    MSHInMail im = new MSHInMail();

    im.setMessageId("SM_ID-" + UUID.randomUUID().toString());
    im.setSenderMessageId("SM_ID-" + UUID.randomUUID().toString());
    im.setAction("DeliveryNotification");
    im.setService("LegalDelivery_ZPP");
    im.setConversationId(UUID.randomUUID().toString());
    im.setReceiverName("Mr. Receiver Name");
    im.setReceiverEBox("receiver.name@test-sed.si");
    im.setSenderName("Mr. Sender Name");
    im.setSenderEBox("izvrsba@test-sed.si");

    im.setMSHInPayload(new MSHInPayload());
    MSHInPart ip = new MSHInPart();
    ip.setFilename("Test.txt");
    ip.setDescription("test attachment");
    
    ip.setIsSent(Boolean.TRUE);
    ip.setIsReceived(Boolean.FALSE);
    ip.setGeneratedFromPartId(BigInteger.TEN);
    
    File f =  S_STORAGE_UTIL.storeOutFile(MimeValue.MIME_TXT.getMimeType(), FILEBLOB_1.getBytes());
    ip.setFilepath(StorageUtils.getRelativePath(f));
    ip.setMimeType(MimeValue.MIME_TEXI.getMimeType());
    ip.setEbmsId("SM_ID-" + UUID.randomUUID().toString());
    
    

    IMPartProperty iprop1 = new IMPartProperty();
    iprop1.setName("Property 1");
    iprop1.setValue("value");
    IMPartProperty iprop2 = new IMPartProperty();
    iprop2.setName("Property 2");
    iprop2.setValue("value");

    ip.getIMPartProperties().add(iprop1);
    ip.getIMPartProperties().add(iprop2);

    im.getMSHInPayload().getMSHInParts().add(ip);

    return im;
  }



}
