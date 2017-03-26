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
package si.laurentius.commons.pmode;

import java.util.Objects;
import si.laurentius.msh.pmode.AgreementRef;
import si.laurentius.msh.pmode.MEPLegType;
import si.laurentius.msh.pmode.MEPTransportType;
import si.laurentius.msh.pmode.MEPType;

import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.Service;


import si.laurentius.msh.pmode.TransportChannelType;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.pmode.Action;

/**
 *
 * @author Jože Rihtaršič
 */
public class PModeUtils {

  public static final SEDLogger LOG = new SEDLogger(FilePModeManager.class);

  /**
   * Method returns MEP for action in PMode
   *
   * @param ctx
   * @param action
   * @param pm
   * @throws si.laurentius.commons.exception.PModeException
   */
  public static void fillTransportMEPForAction(EBMSMessageContext ctx, String action, PMode pm)
      throws PModeException {

    for (MEPType mt : pm.getMEPS()) {
      TransportChannelType tct = null;
      MEPLegType mepLeg= null;
      boolean foreChannel = true;
      for (MEPLegType mlt : mt.getLegs()) {
        MEPTransportType tr = mlt.getTransport();
        if (tr == null) {
          LOG.formatedWarning("Bad MEP definition leg with no Transport element in PMode: '%s' !",
              pm.getId());
          continue;
        }
        if (tr.getForeChannel() != null && Objects.equals(
            tr.getForeChannel().getAction(), action)) {
          mepLeg = mlt;
          tct = tr.getForeChannel();
          foreChannel = true;
          break;
        } else if (tr.getBackChannel() != null && Objects.equals(
            tr.getBackChannel().getAction(), action)) {
          mepLeg = mlt;
          tct = tr.getBackChannel();
          foreChannel = false;
          break;
        }
      }
      if (tct!=null) {
        ctx.setMEPType(mt);
        ctx.setMEPLegType(mepLeg);
        ctx.setTransportChannelType(tct);
        ctx.setPushTransfrer(foreChannel);
        break;
      }
    }
    if (ctx.getTransportChannelType() == null) {   
      throw new PModeException(
          String.format("PMode '%s' does not have MEP for action '%s'.", pm.getId(), action));
    }
  }

  /**
   * Method extract AgreementRef from pmode for exchange partyId:
   *
   * @param partyId - party
   * @param pm - PMode objects
   * @return AgreementRef object or null if not exists
   */
  public static AgreementRef getAgreementRefForExchangePartyId(String partyId, PMode pm) {
    if (pm.getExchangeParties() != null && !pm.getExchangeParties().getPartyInfos().isEmpty()) {
      for (PMode.ExchangeParties.PartyInfo pf : pm.getExchangeParties().getPartyInfos()) {
        if (Objects.equals(pf.getPartyIdentitySetIdRef(), partyId)) {
          return pf.getAgreementRef();
        }
      }
    }
    return null;
  }

  /**
   * Method returs action from service. If action not exists Exception is thrown
   *
   * @param action
   * @param srv - service
   * @return Service.Action - if exists else PModeException is thrown
   * @throws PModeException
   */
  public static Action getActionFromService(String action, Service srv)
      throws PModeException {
    Action act = null;
    for (Action a : srv.getActions()) {
      if (Objects.equals(a.getName(), action)) {
        act = a;
        break;
      }
    }
    if (act == null) {
      throw new PModeException(String.format("No action '%s' in service '%s'.",
          action, srv.getId()));
    }
    return act;
  }
  

}
