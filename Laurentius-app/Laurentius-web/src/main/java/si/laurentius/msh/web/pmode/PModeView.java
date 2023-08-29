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
package si.laurentius.msh.web.pmode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PModePartyInfo;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.PartyIdentitySetType;
import si.laurentius.msh.pmode.Service;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("pModeView")
public class PModeView extends AbstractPModeJSFView<PMode> {

  /**
   *
   */
  public static SEDLogger LOG = new SEDLogger(PModeView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPModeInteface;

  PartyIdentitySet editableLocalParty;
  Service editableService;

  public String getEditableLocalPartyId() {
    PMode pme = getEditable();
    return pme != null && pme.getLocalPartyInfo() != null ? pme.
            getLocalPartyInfo().getPartyIdentitySetIdRef() : null;
  }

  public void setEditableLocalPartyId(String elp) {
    PMode pme = getEditable();
    editableLocalParty = null;
    if (pme != null) {
      List<PartyIdentitySet> lstPs = mPModeInteface.getPartyIdentitySets();
      for (PartyIdentitySet pis : lstPs) {
        if (pis.isIsLocalIdentity() && Objects.equals(pis.getId(), elp)) {
          editableLocalParty = pis;
          pme.getLocalPartyInfo().setPartyIdentitySetIdRef(elp);
          if (!pis.getTransportProtocols().isEmpty()) {
            pme.getLocalPartyInfo().setPartyDefTransportIdRef(pis.
                    getTransportProtocols().get(0).getId());
          }
          break;
        }

      }
    }

  }

  public String getEditableServiceId() {
    PMode pme = getEditable();
    if (pme != null) {
      return pme.getServiceIdRef();
    }
    return null;
  }

  public void setEditableServiceId(String es) {
    PMode pme = getEditable();
    this.editableService = null;
    if (pme != null && !Utils.isEmptyString(es)) {
      List<Service> lstSrv = mPModeInteface.getServices();

      for (Service srv : lstSrv) {
        if (Objects.equals(srv.getId(), es)) {
          this.editableService = srv;
          pme.setServiceIdRef(es);
          break;
        }
      }
    }
  }

  public PartyIdentitySet getEditableLocalParty() {
    return editableLocalParty;
  }

  public void setEditableLocalParty(PartyIdentitySet editableLocalParty) {
    this.editableLocalParty = editableLocalParty;
  }

  public Service getEditableService() {
    return editableService;
  }

  public void setEditableService(Service editableService) {
    this.editableService = editableService;
  }

  public List<String> getEditableServiceRoles() {
    Service es = getEditableService();
    List<String> lstRoles = null;
    if (es != null) {
      lstRoles = new ArrayList<>();
      if (es.getInitiator() != null) {
        lstRoles.add(es.getInitiator().getRole());
      }
      if (es.getExecutor() != null) {
        lstRoles.add(es.getExecutor().getRole());
      }

    } else {
      lstRoles = Collections.emptyList();
    }
    return lstRoles;

  }

  @Override
  public boolean validateData() {

    return true;
  }

  @Override
  public void startEditSelected() {
    super.startEditSelected();
    updateDataFromEditable();
  }

  public void updateDataFromEditable() {
    PMode pme = getEditable();
    if (pme == null) {
      return;
    }
    // update service
    List<Service> lstSrv = mPModeInteface.getServices();
    this.editableService = null;
    for (Service srv : lstSrv) {
      if (Objects.equals(srv.getId(), pme.getServiceIdRef())) {
        this.editableService = srv;
        break;
      }
    }

    // update localParty
    if (pme.getLocalPartyInfo() != null
            && !Utils.isEmptyString(pme.getLocalPartyInfo().
                    getPartyIdentitySetIdRef())) {
      List<PartyIdentitySet> lstPrt = mPModeInteface.getPartyIdentitySets();
      this.editableLocalParty = null;
      for (PartyIdentitySet prt : lstPrt) {

        if (prt.isIsLocalIdentity() && Objects.equals(prt.getId(),
                pme.getLocalPartyInfo().getPartyIdentitySetIdRef())) {
          this.editableLocalParty = prt;
          break;
        }
      }
    }

  }

  /**
   *
   */
  @Override
  public void createEditable() {
    
    editableService = null;
    editableLocalParty = null;
           

    String sbname = "pmode_%03d";
    int i = 1;

    while (mPModeInteface.getPModeById(String.format(sbname, i)) != null) {
      i++;
    }
    String pmode = String.format(sbname, i);
    PMode pm = new PMode();
    pm.setId(pmode);

    // -- set service
    pm.setLocalPartyInfo(new PModePartyInfo());
    pm.setExchangeParties(new PMode.ExchangeParties());
    
    List<Service> lst = mPModeInteface.getServices();
    if (!lst.isEmpty()) {
      editableService = lst.get(0);
      pm.setServiceIdRef(editableService.getId());
      pm.getLocalPartyInfo().getRoles().add(editableService.getInitiator() != null ?
              editableService.getInitiator().getRole() : null);
      pm.getLocalPartyInfo().getRoles().add(editableService.getExecutor() != null ? 
              editableService.getExecutor().getRole() : null);
    }

    List<PartyIdentitySet> lstPs = mPModeInteface.getPartyIdentitySets();
    for (PartyIdentitySet pi : lstPs) {
      if (pi.isIsLocalIdentity()) {
        editableLocalParty = pi;
        LOG.formatedWarning("Add local party: ", pi.getId());
        pm.getLocalPartyInfo().setPartyIdentitySetIdRef(pi.getId());
        if (!pi.getTransportProtocols().isEmpty()) {
          LOG.formatedWarning("Add local party protocol: ", pi.getId());
          pm.getLocalPartyInfo().setPartyDefTransportIdRef(pi.
                  getTransportProtocols().get(0).getId());
        }
        break;
      }
    }

    setNew(pm);
  }

  public PModePartyInfo getEditableLocalPartyIdentityInfo() {
    PMode pm = getEditable();
    if (pm != null) {
      if (pm.getLocalPartyInfo() == null) {
        pm.setLocalPartyInfo(new PModePartyInfo());
      }
      return pm.getLocalPartyInfo();

    }
    return null;
  }

  public boolean getEditableLocPartyHasInitiatorRole() {
    Service srv = getEditableService();
    boolean hasIR = false;
    if (srv != null && srv.getInitiator() != null) {
      hasIR = getEditableLocPartyHasRole(srv.getInitiator().getRole());
    }
    return hasIR;
  }

  public boolean getEditableLocPartyHasExecutorRole() {
    Service srv = getEditableService();
    boolean hasIR = false;
    if (srv != null && srv.getExecutor() != null) {
      hasIR = getEditableLocPartyHasRole(srv.getExecutor().getRole());
    }
    return hasIR;
  }

  public boolean getEditableLocPartyHasRole(String role) {

    PModePartyInfo lp = getEditableLocalPartyIdentityInfo();

    boolean hasIR = false;
    if (lp != null && !Utils.isEmptyString(role)) {
      hasIR = lp.getRoles().contains(role);
    }

    return hasIR;
  }

  public void setEditableLocPartyHasInitiatorRole(boolean bVal) {
    Service srv = getEditableService();
    if (srv != null && srv.getInitiator() != null) {
      setEditableLocPartyRole(bVal, srv.getInitiator().getRole());
    }
  }

  public void setEditableLocPartyHasExecutorRole(boolean bVal) {
    Service srv = getEditableService();
    if (srv != null && srv.getExecutor() != null) {
      setEditableLocPartyRole(bVal, srv.getExecutor().getRole());
    }
  }

  public void setEditableLocPartyRole(boolean bVal, String role) {

    PModePartyInfo lp = getEditableLocalPartyIdentityInfo();
    if (lp != null && !Utils.isEmptyString(role)) {
      if (bVal) {
        if (!lp.getRoles().contains(role)) {
          lp.getRoles().add(role);
        }
      } else {
        if (lp.getRoles().contains(role)) {
          lp.getRoles().remove(role);
        }
      }
    }

  }

  public List<PartyIdentitySetType.TransportProtocol> getEditableLocalPartyTransports() {
    PMode pm = getEditable();
    String pis = pm != null && pm.getLocalPartyInfo() != null && !Utils.
            isEmptyString(
                    pm.getLocalPartyInfo().getPartyIdentitySetIdRef()) ? pm.
            getLocalPartyInfo().getPartyIdentitySetIdRef() : null;

    PartyIdentitySetType pist = pis != null ? mPModeInteface.
            getPartyIdentitySetById(pis) : null;

    return pist != null ? pist.getTransportProtocols() : Collections.emptyList();
  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    PMode srv = getSelected();
    if (srv != null) {
      mPModeInteface.removePMode(srv);
      bSuc = true;
    }
    return bSuc;

  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {

    long l = LOG.logStart();
    boolean bsuc = false;
    PMode sv = getEditable();
    if (sv != null) {
      mPModeInteface.addPMode(sv);
      setEditable(null);
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    long l = LOG.logStart();
    boolean bsuc = false;
    PMode sv = getEditable();
    if (sv != null) {
      mPModeInteface.updatePMode(sv);
      setEditable(null);
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<PMode> getList() {
    long l = LOG.logStart();
    List<PMode> lst = mPModeInteface.getPModes();
    LOG.logEnd(l);
    return lst;
  }

  @Override
  public String getSelectedDesc() {
    if (getSelected() != null) {
      return getSelected().getId();
    }
    return null;
  }

  public List<PartyIdentitySet> getLocalParties() {
    List<PartyIdentitySet> lstLP = new ArrayList<>();
    for (PartyIdentitySet ps : mPModeInteface.getPartyIdentitySets()) {
      if (ps.isIsLocalIdentity()) {
        lstLP.add(ps);
      }
    }
    return lstLP;

  }

  public List<PartyIdentitySet> getParties() {
    return mPModeInteface.getPartyIdentitySets();
  }

}
