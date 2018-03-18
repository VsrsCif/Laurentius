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
package si.laurentius.commons.interfaces;

import java.util.List;
import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.ReceptionAwareness;
import si.laurentius.msh.pmode.Security;
import si.laurentius.msh.pmode.Service;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.pmode.EBMSMessageContext;

/**
 *
 * @author Jože Rihtaršič
 */
@Local
public interface PModeInterface {

  /**
   * Method returs PMODE for given local sender Party. If pmode not exists or more than one PMode is
   * defined for given parameters PModeException is thrown.
   *
   * @param senderRefId - local sender party identity set id
   * @param actionSendingRole - bussines role sender must have in current action
   * @param receiverRefId - exchange receiver party idetity set id
   * @param serviceId - bussines service
   * @return PMode
   * @throws PModeException
   */
  @Lock(value = LockType.READ)
  public PMode getPModeForLocalPartyAsSender(String senderRefId, String actionSendingRole,
      String receiverRefId, String serviceId) throws PModeException ;

  /**
   * Method returs PMODE for given exchange sender Party. If pmode not exist null is returned 
   * 
   *
   * @param senderRefId - exchange sender party identity set id
   * @param actionSendingRole - bussines role sender must have in current action
   * @param receiverRefId - local receiver party idetity set id
   * @param serviceId - bussines service
   * @return PMode

   */
  @Lock(value = LockType.READ)
  public PMode getPModeForExchangePartyAsSender(String senderRefId, String actionSendingRole,
      String receiverRefId, String serviceId) throws PModeException;

  @Lock(value = LockType.READ)
  public PMode getPModeById(String pmodeId);

  
  /**
   * Method returns PMode for AgreementRef. if Agreement not exist PMode exception is thrown.
   *
   * @param agrRef
   * @param agrRefType
   * @param agrPMode
   * @return
   * @throws PModeException
   */
 public PMode getByAgreementRef(String agrRef, String agrRefType, String agrPMode)
      throws PModeException ;
  
 /**
  * Create EBMSMessageContext for outgoing 
  * @param mail
  * @return
  * @throws PModeException 
  */
 EBMSMessageContext createMessageContextForOutMail(MSHOutMail mail) throws PModeException;
  /**
   * Method returns PartyIdentitySet for address. Address must have [localpart]@[domainPart].
   *
   * @param address
   * @return
   * @throws PModeException
   */
  public PartyIdentitySet getPartyIdentitySetForSEDAddress(String address)
      throws PModeException;

  /**
   * Method returns PartyIdentitySet by id..
   *
   * @param partyIdentiySetId
   * @return
   * @throws PModeException
   */
  public PartyIdentitySet getPartyIdentitySetById(String partyIdentiySetId);
   

 
  /**
   * Method returns PartyIdentitySet for partyId and type. Method returs PartyIdentitySet if one of
   * partyID has matches with given type and partyIdValue ebmsMessage
   * <ns2:PartyId type="urn:oasis:names:tc:ebcore:partyid-type:unregistered">test</ns2:PartyId>
   *
   * @param partyType - type of party - urn:oasis:names:tc:ebcore:partyid-type:unregistered
   * @param partyIdValue - partyId value: test
   * @return  PartyIdentitySet or null if not found. if there is more than one result PModeException is thron- 
   * @throws si.laurentius.commons.exception.PModeException
   */
  public PartyIdentitySet getPartyIdentitySetForPartyId( String partyType, String partyIdValue)
       throws PModeException;
  /**
   * Method returs service if exists else throws PModeException!
   *
   * @param serviceName
   * @param serviceType
   * @param action
   * @return Service for serviceId
   * @throws PModeException
   */
  public Service getServiceByNameAndTypeAndAction(String serviceName, String serviceType,
      String action)
      throws PModeException;

  /**
   * Method returs service if exists else throws PModeException!
   *
   * @param serviceId
   * @return Service for serviceId
   * @throws PModeException
   */
  public Service getServiceById(String serviceId);

  /**
   * Method returs ReceptionAwareness if exists else throws PModeException!
   *
   * @param raId
   * @return ReceptionAwareness for id
   * @throws PModeException
   */
  public ReceptionAwareness getReceptionAwarenessById(String raId);

  /**
   * Method returs service if exists else throws PModeException!
   *
   * @param securityId
   * @return Security for securityId
   * @throws PModeException
   */
  public Security getSecurityById(String securityId);

  
  public boolean partyIdentitySetExists(String id);
  
  public List<PMode> getPModes();
  public PMode getPModeMSHOutMail(MSHOutMail outMail) throws PModeException ;
  public List<Service> getServices() ;
  public List<PartyIdentitySet> getPartyIdentitySets() ;
  public List<Security> getSecurities() ;
  public List<ReceptionAwareness> getReceptionAwarenesses() ;
  
  
  public void addPMode(PMode val);
  public void addService(Service val);
  public void addPartyIdentitySet(PartyIdentitySet val);
  public void addSecurity(Security val);
  public void addReceptionAwareness(ReceptionAwareness val);
  
  public void removePMode(PMode val);
  public void removeService(Service val);
  public void removePartyIdentitySet(PartyIdentitySet val);
  public void removeSecurity(Security val);
  public void removeReceptionAwareness(ReceptionAwareness val);
  
  public void updatePMode(PMode val);
  public void updateService(Service val);
  public void updatePartyIdentitySet(PartyIdentitySet val);
  public void updateSecurity(Security val);
  public void updateReceptionAwareness(ReceptionAwareness val);
  
}
  