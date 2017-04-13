/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.meps.web;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.xml.bind.JAXBException;
import si.jrc.msh.plugin.meps.AppConstant;
import si.jrc.msh.plugin.meps.utils.TestUtils;
import si.jrc.msh.plugin.meps.web.dlg.DialogProgress;

import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.plugin.meps.ServiceType;

/**
 *
 * @author sluzba
 */
public class MEPSTestAbstract {

  TestUtils mtUtils = new TestUtils();

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;
  


  private static final SEDLogger LOG = new SEDLogger(MEPSTestAbstract.class);


  String testReceiverEBox;
  String testSenderEBox;
  String testService = "PrintAndEnvelope-LegalZPP";
  String testSubject = "[StressTest] Test example %d";


  @ManagedProperty(value = "#{MEPSLookups}")
  private MEPSLookups pluginLookups;

  public MEPSLookups getPluginLookups() {
    return pluginLookups;
  }

  public void setPluginLookups(MEPSLookups pluginLookups) {
    this.pluginLookups = pluginLookups;
  }
  
  public String getTestReceiverEBox() {
    return testReceiverEBox;
  }

  public String getTestSenderEBox() {
    return testSenderEBox;
  }

  public String getTestService() {
    return testService;
  }

  public String getTestSubject() {
    return testSubject;
  }
  
  public String getUserName() {
    return ((LoginManager)getBean("loginManager")).getUsername();
  }

  public void validateMailData() {

  }

  public void setTestReceiverEBox(String testReceiverEBox) {
    this.testReceiverEBox = testReceiverEBox;
  }

  public void setTestSenderEBox(String testSenderEBox) {
    this.testSenderEBox = testSenderEBox;
  }

  public void setTestService(String ts) {
    if (Objects.equals(testService, ts)){
      return;
    }
    this.testService = ts;
    
  }

  public void setTestSubject(String testSubject) {
    this.testSubject = testSubject;
  }
  
  public ServiceType getServiceType(String name){
    return getPluginLookups().getServiceByName(name);
    
  }

 

  public void createOutMail(int indx,
          String sndBox,
          String rcBox,
          String service,
          String action,
          ServiceType st,
          String userName) throws StorageException, JAXBException {

    MSHOutMail mom = mtUtils.createOutMail(indx,
          sndBox,
          rcBox,
          service,
          action,
          st);
    mDB.serializeOutMail(mom, userName, AppConstant.PLUGIN_NAME, "");

    
  }
  
  public List<File> getTestFiles(){
    Random rnd = new Random(Calendar.getInstance().getTimeInMillis());
    return mtUtils.getRandomFiles(2, 5, rnd);
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

  protected void addError(String error, String desc) {
    facesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, error,
                    desc));

  }

  public boolean validateData() {
    if (Utils.isEmptyString(getTestSenderEBox())) {
      addError("Missing data", "Missing sender box address!");
      return false;
    }

    if (!getTestSenderEBox().contains("@")) {
      addError("Invalid data", "Invalid sender box address!");
      return false;
    }

    if (Utils.isEmptyString(getTestReceiverEBox())) {
      addError("Missing data", "Missing receiver box address!");
      return false;
    }
    if (!getTestReceiverEBox().contains("@")) {
      addError("Invalid data", "Invalid receiver box address!");
      return false;
    }
    if (Utils.isEmptyString(getTestService())) {
      addError("Missing data", "Missing service!");
      return false;
    }

    return true;

  }

  public Object getBean(final String beanName) {
    final Object returnObject = facesContext().getELContext().getELResolver().
            getValue(facesContext().getELContext(), null, beanName);
    if (returnObject == null) {
      LOG.formatedWarning("Bean with name %s was not found!", beanName);
    }
    return returnObject;
  }

  public DialogProgress getDlgProgress() {
    return (DialogProgress) getBean("dialogProgress");
  }
  
}
