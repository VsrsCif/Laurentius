/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.test;

import generated.SedLookups;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBException;
import si.laurentius.application.SEDApplication;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.ebox.SEDBox;
import si.laurentius.user.SEDUser;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessor;
import si.laurentius.property.SEDProperty;

/**
 *
 * @author Jože Rihtaršič
 */
public class SEDTestLookup implements SEDLookupsInterface {

  public static final String INIT_LOOKUPS_RESOURCE_PATH = "/sed-lookups.xml";
  private final HashMap<Class, List<?>> mlstCacheLookup = new HashMap<>();
  String domain = null;

  public SEDTestLookup(InputStream is)
          throws IOException, JAXBException {
    init(is);
  }

  @Override
  public boolean addSEDApplication(SEDApplication sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean addSEDInterceptor(SEDInterceptor sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void clearAllCache() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void clearCache(Class cls) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SEDApplication getSEDApplicationById(String id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<SEDApplication> getSEDApplications() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SEDInterceptor getSEDInterceptorById(BigInteger id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SEDInterceptor getSEDInterceptorByName(String name) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<SEDInterceptor> getSEDInterceptors() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SEDProcessor getSEDProcessorByName(String name) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  private void init(InputStream is)
          throws IOException, JAXBException {
    SedLookups cls = (SedLookups) XMLUtils.deserialize(is, SedLookups.class);
    for (SEDProperty sp : cls.getSEDProperties().getSEDProperties()) {
      System.setProperty(sp.getKey(), sp.getValue());

      if (sp.getKey().equals(SEDSystemProperties.SYS_PROP_LAU_DOMAIN)) {
        domain = sp.getValue();

      }
    }

    mlstCacheLookup.put(SEDBox.class, cls.getSEDBoxes().getSEDBoxes());

    mlstCacheLookup.put(SEDCronJob.class, cls.getSEDCronJobs().getSEDCronJobs());
    mlstCacheLookup.put(SEDUser.class, cls.getSEDUsers().getSEDUsers());
  }

  private <T> List<T> getLookup(Class<T> c) {
    if (!mlstCacheLookup.containsKey(c)) {
      mlstCacheLookup.put(c, new ArrayList<>());
    }
    return (List<T>) mlstCacheLookup.get(c);
  }

  private <T> boolean add(T val) {
    List lst = getLookup(val.getClass());
    return lst.add(val);
  }

  private <T> boolean remove(T val) {
    List lst = getLookup(val.getClass());
    return lst.remove(val);
  }

  @Override
  public boolean addSEDBox(SEDBox sb) {
    return add(sb);
  }

  @Override
  public boolean addSEDCronJob(SEDCronJob sb) {
    return add(sb);
  }

  @Override
  public boolean addSEDUser(SEDUser sb) {
    return add(sb);
  }

  @Override
  public SEDBox getSEDBoxByAddressName(String strname) {
    if (strname != null && !strname.trim().isEmpty()) {
      String sedBox = strname.trim();

      if (Utils.isEmptyString(domain)) {
        String msg = "Missing domain parameter in configuration. Did you init application with domain parameter?";

        throw new RuntimeException(msg);
      }
      String lcdomain = "@" + domain.toLowerCase();
      if (!sedBox.toLowerCase().endsWith(lcdomain.toLowerCase())) {
        System.out.println("**************************** - domain not match");
        System.out.println("domain: " + lcdomain + " sedbox:  " + sedBox);
        return null;
      }
      List<SEDBox> lst = getSEDBoxes();
      for (SEDBox sb : lst) {
        if (strname.equalsIgnoreCase(sb.getLocalBoxName() + lcdomain)) {
          return sb;
        }
      }
    }
    return null;
  }

  @Override
  public SEDBox getSEDBoxByLocalName(String strname) {
    if (strname != null && !strname.trim().isEmpty()) {
      String localName = strname.trim();

      List<SEDBox> lst = getSEDBoxes();
      for (SEDBox sb : lst) {
        if (localName.equalsIgnoreCase(sb.getLocalBoxName())) {
          return sb;
        }
      }
    }
    return null;
  }

  @Override
  public List<SEDBox> getSEDBoxes() {
    return getLookup(SEDBox.class);
  }

  @Override
  public SEDCronJob getSEDCronJobById(BigInteger id) {
    if (id != null) {

      List<SEDCronJob> lst = getSEDCronJobs();
      for (SEDCronJob sb : lst) {
        if (id.equals(sb.getId())) {
          return sb;
        }
      }
    }
    return null;
  }

  @Override
  public List<SEDCronJob> getSEDCronJobs() {
    return getLookup(SEDCronJob.class);
  }

  @Override
  public SEDUser getSEDUserByUserId(String userId) {
    if (userId != null && !userId.trim().isEmpty()) {
      String ui = userId.trim();
      List<SEDUser> lst = getSEDUsers();
      for (SEDUser sb : lst) {
        if (sb.getUserId().equalsIgnoreCase(ui)) {
          return sb;
        }
      }
    }
    return null;
  }

  @Override
  public List<SEDUser> getSEDUsers() {
    return getLookup(SEDUser.class);
  }

  @Override
  public boolean removeSEDApplication(SEDApplication sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean removeSEDBox(SEDBox sb) {
    return remove(sb);
  }

  @Override
  public boolean removeSEDCronJob(SEDCronJob sb) {
    return remove(sb);
  }

  @Override
  public boolean removeSEDInterceptor(SEDInterceptor sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean removeSEDUser(SEDUser sb) {
    return remove(sb);
  }

  @Override
  public boolean updateSEDApplication(SEDApplication sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean updateSEDBox(SEDBox sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean updateSEDCronJob(SEDCronJob sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean updateSEDInterceptor(SEDInterceptor sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean updateSEDProcessorInstance(SEDProcessorInstance sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean updateSEDUser(SEDUser sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SEDCronJob getSEDCronJobByName(String name) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean addSEDProcessor(SEDProcessor sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }


  @Override
  public SEDProcessor getSEDProcessor(BigInteger id) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }


  @Override
  public List<SEDProcessor> getSEDProcessors() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }


  @Override
  public boolean removeSEDProcessor(SEDProcessor sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  

  @Override
  public boolean updateSEDProcessor(SEDProcessor sb) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
