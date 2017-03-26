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
package si.laurentius.commons.pmode;

import si.laurentius.msh.pmode.Action;
import si.laurentius.msh.pmode.AgreementRef;
import si.laurentius.msh.pmode.MEPLegType;
import si.laurentius.msh.pmode.MEPType;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.PartyIdentitySetType;
import si.laurentius.msh.pmode.ReceptionAwareness;
import si.laurentius.msh.pmode.Security;
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.pmode.TransportChannelType;

/**
 * User message context for sending message of received message
 * 
 * @author Jože Ritharšič
 */
public class EBMSMessageContext {

  Action mAction;
  AgreementRef mExchangeAgreementRef;
  TransportChannelType mTransportChannelType;
  PMode mPMode;
  PartyIdentitySet mReceiverPartyIdentitySet;
  String mReceivingRole;
  ReceptionAwareness mReceptionAwareness;
  PartyIdentitySet mSenderPartyIdentitySet;
  String mSendingRole;
  Service mService;
  PartyIdentitySet.TransportProtocol mTransportProtocol;
  boolean mbPushTransfrer;
  MEPType mpMEPType;
  MEPLegType mMEPLegType;
  Security security;
  int miReceptionAwarenessRetry;

  public MEPLegType getMEPLegType() {
    return mMEPLegType;
  }

  public void setMEPLegType(MEPLegType mlt) {
    this.mMEPLegType = mlt;
  }

  public Action getAction() {
    return mAction;
  }

  public void setAction(Action action) {
    this.mAction = action;
  }

  public AgreementRef getExchangeAgreementRef() {
    return mExchangeAgreementRef;
  }

  public void setExchangeAgreementRef(AgreementRef mExchangeAgreementRef) {
    this.mExchangeAgreementRef = mExchangeAgreementRef;
  }

  public TransportChannelType getTransportChannelType() {
    return mTransportChannelType;
  }

  public void setTransportChannelType(TransportChannelType mTransportChannelType) {
    this.mTransportChannelType = mTransportChannelType;
  }

  public PMode getPMode() {
    return mPMode;
  }

  public void setPMode(PMode mPMode) {
    this.mPMode = mPMode;
  }

  public PartyIdentitySet getReceiverPartyIdentitySet() {
    return mReceiverPartyIdentitySet;
  }

  public void setReceiverPartyIdentitySet(PartyIdentitySet mReceiverPartyIdentitySet) {
    this.mReceiverPartyIdentitySet = mReceiverPartyIdentitySet;
  }

  public String getReceivingRole() {
    return mReceivingRole;
  }

  public void setReceivingRole(String mReceivingRole) {
    this.mReceivingRole = mReceivingRole;
  }

  public ReceptionAwareness getReceptionAwareness() {
    return mReceptionAwareness;
  }

  public void setReceptionAwareness(ReceptionAwareness mReceptionAwareness) {
    this.mReceptionAwareness = mReceptionAwareness;
  }

  public PartyIdentitySet getSenderPartyIdentitySet() {
    return mSenderPartyIdentitySet;
  }

  public void setSenderPartyIdentitySet(PartyIdentitySet senderPartyIdentitySet) {
    this.mSenderPartyIdentitySet = senderPartyIdentitySet;
  }

  public String getSendingRole() {
    return mSendingRole;
  }

  public void setSendingRole(String mSendingRole) {
    this.mSendingRole = mSendingRole;
  }

  public Service getService() {
    return mService;
  }

  public void setService(Service mService) {
    this.mService = mService;
  }

  public PartyIdentitySetType.TransportProtocol getTransportProtocol() {
    return mTransportProtocol;
  }

  public void setTransportProtocol(PartyIdentitySetType.TransportProtocol mTransportProtocol) {
    this.mTransportProtocol = mTransportProtocol;
  }

  public boolean isPushTransfrer() {
    return mbPushTransfrer;
  }

  public void setPushTransfrer(boolean mbPushTransfrer) {
    this.mbPushTransfrer = mbPushTransfrer;
  }

  public MEPType getMEPType() {
    return mpMEPType;
  }

  public void setMEPType(MEPType mpMEPType) {
    this.mpMEPType = mpMEPType;
  }

  public Security getSecurity() {
    return security;
  }

  public void setSecurity(Security security) {
    this.security = security;
  }

  public int getReceptionAwarenessRetry() {
    return miReceptionAwarenessRetry;
  }

  public void setReceptionAwarenessRetry(int miReceptionAwarenessRetry) {
    this.miReceptionAwarenessRetry = miReceptionAwarenessRetry;
  }



  
   


}
