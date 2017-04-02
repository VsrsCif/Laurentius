/*
 * Copyright 2015, Supreme Court Republic of Slovenia
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
package si.jrc.msh.plugin.meps;

import java.util.Properties;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;
import org.apache.cxf.binding.soap.SoapMessage;
import si.jrc.msh.plugin.meps.enums.MEPSActions;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.ebox.SEDBox;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.outbox.property.MSHOutProperties;
import si.laurentius.msh.outbox.property.MSHOutProperty;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class MEPSInInterceptor implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(MEPSInInterceptor.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface msedLookup;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPMode;

  /**
   *
   */
  @Resource
  public UserTransaction mutUTransaction;

 @Override
  public MailInterceptorDef getDefinition() {
    MailInterceptorDef def = new MailInterceptorDef();
    def.setType("CEFInInterceptor");
    def.setName("CEFInInterceptor");
    def.setDescription("CEF Digital testin module");
    return def;
  }

  /**
   *
   * @param msg
   */
  @Override
  public boolean handleMessage(SoapMessage msg) {
    long l = LOG.logStart();
    boolean isBackChannel = SoapUtils.isRequestMessage(msg);
     MSHInMail mInMail = SoapUtils.getMSHInMail(msg);
    
    if(!isBackChannel){
      if(mInMail!= null && MEPSActions.ADD_MAIL.getValue().equals(mInMail.getAction())){
          
      
      
      }
    }
   
    return true;
  }
  
  public void ProcessAddMail( MSHInMail mInMail){
    // test data
    
    if (mInMail.getMSHInPayload()==null || mInMail.getMSHInPayload().getMSHInParts().size()< 2){
      // throw exception - invalid payload - in data -pdf 
    
    }
    
   
  }

  /**
   *
   * @param t
   */
  @Override
  public void handleFault(SoapMessage t) {
    // ignore
  }

}
