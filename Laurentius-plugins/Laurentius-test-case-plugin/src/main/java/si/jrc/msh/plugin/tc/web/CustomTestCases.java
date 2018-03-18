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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.xml.ws.WebServiceContext;
import si.jrc.msh.plugin.tc.utils.TestUtils;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.StorageException;

import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.testcase.MailTestCases;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("customTestCases")
public class CustomTestCases extends TestCaseAbstract implements Serializable{

  private static final SEDLogger LOG = new SEDLogger(CustomTestCases.class);

  @Resource
  WebServiceContext context;

  public CustomTestCases() {
    setTestSubject("[CustomTest] Test example");
  }

  


  
  public List<MailTestCases.MailTestCase> getTestCases(){
    MailTestCases mtc = TestUtils.getGenericTestCases();
    return mtc == null? Collections.emptyList():mtc.getMailTestCases();
  }
   
  
  public void execute(MailTestCases.MailTestCase mtc ){
    if (!validateData()){
      return;
    }
    String senderBox = getTestSenderEBox();
    String receiverBox = getTestReceiverEBox();
    String service = getTestService();
    String action = getTestAction();    
    String subject = getTestSubject();
     String username = getUserName();
  
    List<File> lstfile = new ArrayList<>();
    String folder = 
            StringFormater.replaceProperties(TestUtils.GENERIC_FOLDER);
    for (MailTestCases.MailTestCase.Payload p:  mtc.getPayloads()){
      File f = new File(SEDSystemProperties.getPluginsFolder(), folder + p.getFilepath());
      if (f.exists()) {
        lstfile.add(f);
      } else {
        addError(String.format("Missing file %s", f.getName()),
                String.format("Check add file %s  or update testcase definition", f.getAbsolutePath()));
        return;
      }
    }
    
    try {
      createOutMail(username, senderBox,receiverBox,subject, service,
              action, lstfile);
      facesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
          String.format("Test %s completed", mtc.getName()), "Mail added to delivery!"));
    } catch (StorageException | PModeException ex) {
       addError(String.format("Error occured while dispatching test %s", mtc.getName()), ex.getMessage());
    }
   
  }



}
