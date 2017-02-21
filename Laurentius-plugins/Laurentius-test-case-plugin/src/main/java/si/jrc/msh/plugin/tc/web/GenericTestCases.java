/*
 * Copyright 2016, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.jrc.msh.plugin.tc.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.xml.ws.WebServiceContext;
import si.jrc.msh.plugin.tc.utils.TestUtils;
import static si.jrc.msh.plugin.tc.web.TCStressTest.DWR_ACTION;
import static si.jrc.msh.plugin.tc.web.TCStressTest.DWR_SERVICE;
import static si.jrc.msh.plugin.tc.web.TCStressTest.ZPP_ACTION;
import static si.jrc.msh.plugin.tc.web.TCStressTest.ZPP_SERVICE;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.testcase.MailTestCase;
import si.laurentius.testcase.MailTestCases;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "genericTestCases")
public class GenericTestCases {

  private static final SEDLogger LOG = new SEDLogger(GenericTestCases.class);

  @Resource
  WebServiceContext context;

    @ManagedProperty(value = "#{TCStressTest}")
  private TCStressTest stressTestBean;

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface mDBLookUp;

  TestUtils mTestUtils = new TestUtils();

  
  String genericTestService;
  /**
   *
   * @return
   */
  protected ExternalContext externalContext() {
    return facesContext().getExternalContext();
  }
  
  public List<MailTestCases.MailTestCase> getTestCases(){
    MailTestCases mtc = TestUtils.getGenericTestCases();
    return mtc == null? Collections.emptyList():mtc.getMailTestCases();
  }
   
  
  public void submitTestCaseMail(MailTestCases.MailTestCase mtc ){
    
    if (Utils.isEmptyString( stressTestBean.getTestSenderEBox())) {
      return;
    }
    if (Utils.isEmptyString(stressTestBean.getTestReceiverEBox())) {
      return;
    }
    String sndBox = stressTestBean.getTestSenderEBox() + "@" + SEDSystemProperties.getLocalDomain();
    String rcvBox = stressTestBean.getTestReceiverEBox();

    if (!rcvBox.contains("@")) {
 
      return;
    }

   
    String rcName = rcvBox.substring(0, rcvBox.indexOf("@"));

    String service;
    String action;
    if (Objects.equals(getGenericTestService(), ZPP_SERVICE)) {
      service = ZPP_SERVICE;
      action = ZPP_ACTION;
    } else {
      service = DWR_SERVICE;
      action = DWR_ACTION;
    }
    List<File> lstfile = new ArrayList<>();
    String folder = StringFormater.replaceProperties(TestUtils.GENERIC_FOLDER);
    for (MailTestCase.Payload p:  mtc.getPayloads()){
      File f = new File(folder + p.getFilepath());
      lstfile.add(f);
    }
    
    
    MSHOutMail mout = mTestUtils.createOutMail(0, sndBox, rcName, rcvBox, service,
              action, lstfile);
      
    try {
      mDB.serializeOutMail(mout, "test", "test-plugin", "");
    } catch (StorageException ex) {
      Logger.getLogger(GenericTestCases.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    facesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
          "Končano", "Pošiljka oddana v pošiljanje!" + mtc.getName()));
  }


  /**
   *
   * @return
   */
  protected FacesContext facesContext() {
    return FacesContext.getCurrentInstance();
  }

  public String getGenericTestService() {
    return genericTestService;
  }

  public void setGenericTestService(String genericTestService) {
    this.genericTestService = genericTestService;
  }

  public TCStressTest getStressTestBean() {
    return stressTestBean;
  }

  public void setStressTestBean(TCStressTest stressTestBean) {
    this.stressTestBean = stressTestBean;
  }


}
