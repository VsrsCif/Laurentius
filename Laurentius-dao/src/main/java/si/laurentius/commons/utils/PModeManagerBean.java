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
package si.laurentius.commons.utils;

import java.util.Calendar;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.ReceptionAwareness;
import si.laurentius.msh.pmode.Security;
import si.laurentius.msh.pmode.Service;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.pmode.FilePModeManager;

/**
 *
 * @author Jože Rihtaršič
 */
@Startup
@Singleton
@AccessTimeout(value = 60000)
public class PModeManagerBean implements PModeInterface {

  protected static SEDLogger LOG = new SEDLogger(PModeManagerBean.class);
  protected long m_iLastRefreshTime = 0;
  protected long m_iRefreshInterval = 1800 * 1000; // 30 min

  FilePModeManager mPModeManager = new FilePModeManager();

  @Override
  public PMode getPModeForLocalPartyAsSender(String senderRefId, String actionSendingRole,
      String receiverRefId, String serviceId)
      throws PModeException {
    return getPModeManager().getPModeForLocalPartyAsSender(senderRefId, actionSendingRole,
        receiverRefId,
        serviceId);

  }

  @Override
  public PMode getPModeForExchangePartyAsSender(String senderRefId, String actionSendingRole,
      String receiverRefId, String serviceId)
      throws PModeException {
    return getPModeManager().getPModeForExchangePartyAsSender(senderRefId, actionSendingRole,
        receiverRefId, serviceId);
  }

  @Override
  public PMode getPModeById(String pmodeId)
      throws PModeException {
    return getPModeManager().getPModeById(pmodeId);
  }

  @Override
  public PMode getByAgreementRef(String agrRef, String agrRefType, String agrPMode)
      throws PModeException {
    return getPModeManager().getByAgreementRef(agrRef, agrRefType, agrPMode);
  }

  @Override
  public PartyIdentitySet getPartyIdentitySetForSEDAddress(String address)
      throws PModeException {
    return getPModeManager().getPartyIdentitySetForSEDAddress(address);
  }

  @Override
  public PartyIdentitySet getPartyIdentitySetById(String partyIdentiySetId)
      throws PModeException {
    return getPModeManager().getPartyIdentitySetById(partyIdentiySetId);
  }

  @Override
  public PartyIdentitySet getPartyIdentitySetForPartyId(String partyType, String partyIdValue)
      throws PModeException {
    return getPModeManager().getPartyIdentitySetForPartyId(partyType, partyIdValue);
  }

  @Override
  public Service getServiceByNameAndTypeAndAction(String serviceName, String serviceType,
      String action)
      throws PModeException {
    return getPModeManager().getServiceByNameAndTypeAndAction(serviceName, serviceType,
        action);
  }

  @Override
  public Service getServiceById(String serviceId)
      throws PModeException {
    return getPModeManager().getServiceById(serviceId);
  }

  @Override
  public ReceptionAwareness getReceptionAwarenessById(String raId)
      throws PModeException {
    return getPModeManager().getReceptionAwarenessById(raId);
  }

  @Override
  public Security getSecurityById(String securityId)
      throws PModeException {
    return getPModeManager().getSecurityById(securityId);
  }

  private FilePModeManager getPModeManager() {

    if (m_iLastRefreshTime + m_iRefreshInterval < Calendar.getInstance().getTimeInMillis()) {
      long l = LOG.logStart();
      try {

        mPModeManager.reloadPModes();
      } catch (PModeException ex) {
        LOG.logError(l, ex);
      }
      LOG.logEnd(l);
      m_iLastRefreshTime = Calendar.getInstance().getTimeInMillis();
    }
    return mPModeManager;
  }

  @Override
  public EBMSMessageContext createMessageContextForOutMail(MSHOutMail mail)
      throws PModeException {
    return getPModeManager().createMessageContextForOutMail(mail);
  }
}
