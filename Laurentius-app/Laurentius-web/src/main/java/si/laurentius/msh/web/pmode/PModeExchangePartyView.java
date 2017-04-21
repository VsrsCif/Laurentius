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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
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
import static si.laurentius.msh.web.pmode.PModeView.LOG;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "pModeExchangePartyView")
public class PModeExchangePartyView extends AbstractPModeJSFView<PMode.ExchangeParties.PartyInfo> {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(PModeExchangePartyView.class);
  
  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPModeInteface;

  @ManagedProperty(value = "#{pModeView}")
  PModeView pModeView;

  
  PartyIdentitySet editableParty;
    
  public PModeView getpModeView() {
    return pModeView;
  }

  public void setpModeView(PModeView pModeView) {
    this.pModeView = pModeView;
  }
  
  
  public String getEditablePartyId() {
    PMode.ExchangeParties.PartyInfo pme = getEditable();
    return pme != null?pme.getPartyIdentitySetIdRef():null;    
  }

  public void setEditablePartyId(String elp) {
    PMode.ExchangeParties.PartyInfo pi = getEditable();
    editableParty = null;
    if (pi != null) {
      List<PartyIdentitySet> lstPs = mPModeInteface.getPartyIdentitySets();
      for (PartyIdentitySet pis : lstPs) {
        if (Objects.equals(pis.getId(), elp)) {
          editableParty = pis;
          pi.setPartyIdentitySetIdRef(elp);
          if (!pis.getTransportProtocols().isEmpty()){
            pi.setPartyDefTransportIdRef(pis.getTransportProtocols().get(0).getId());
          }
          break;
        }

      }
    }

  }
  
  public boolean getEditablePartyHasInitiatorRole() {
    
    Service srv = pModeView.getEditableService();
    boolean hasIR = false;
    if (srv != null && srv.getInitiator() != null) {
      hasIR = getEditablePartyHasRole(srv.getInitiator().getRole());
    }
    return hasIR;
  }

  public boolean getEditablePartyHasExecutorRole() {
    Service srv = pModeView.getEditableService();
    boolean hasIR = false;
    if (srv != null && srv.getExecutor() != null) {
      hasIR = getEditablePartyHasRole(srv.getExecutor().getRole());
    }
    return hasIR;
  }

  public boolean getEditablePartyHasRole(String role) {
    PMode.ExchangeParties.PartyInfo pi =  getEditable();

    boolean hasIR = false;
    if (pi != null && !Utils.isEmptyString(role)) {
      hasIR = pi.getRoles().contains(role);
    }

    return hasIR;
  }

            
  public void setEditablePartyHasInitiatorRole(boolean bVal) {
    Service srv = pModeView.getEditableService();
    if (srv != null && srv.getInitiator() != null) {
      setEditablePartyRole(bVal, srv.getInitiator().getRole());
    }
  }

  public void setEditablePartyHasExecutorRole(boolean bVal) {
    Service srv = pModeView.getEditableService();
    if (srv != null && srv.getExecutor() != null) {
      setEditablePartyRole(bVal, srv.getExecutor().getRole());
    }
  }

  public void setEditablePartyRole(boolean bVal, String role) {

   PMode.ExchangeParties.PartyInfo pi =  getEditable();
    if (pi != null && !Utils.isEmptyString(role)) {
      if (bVal) {
        if (!pi.getRoles().contains(role)) {
          pi.getRoles().add(role);
        }
      } else {
        if (pi.getRoles().contains(role)) {
          pi.getRoles().remove(role);
        }
      }
    }

  }

  public List<PartyIdentitySetType.TransportProtocol> getEditableLocalPartyTransports() {
     PMode.ExchangeParties.PartyInfo pi =  getEditable();
    
    if (pi != null) {

      try {
        PartyIdentitySetType pist = mPModeInteface.getPartyIdentitySetById(pi.getPartyIdentitySetIdRef());

        return pist.getTransportProtocols();
      } catch (PModeException ex) {
        LOG.formatedWarning(
                "Error %s occured while retrieving party identity set %s ", ex.
                        getMessage(), pi.getPartyIdentitySetIdRef());
      }
    }
    return Collections.emptyList();
  }


 

  @Override
  public boolean validateData() {
    return true;
  }

  @Override
  public void createEditable() {
    

    PMode.ExchangeParties.PartyInfo np = new PMode.ExchangeParties.PartyInfo();

   // np.setAgreementRef(new AgreementRef());
   
    
    
    setNew(np);

  }

  public boolean partyExists(String id) {
    List<PMode.ExchangeParties.PartyInfo> pplst = getList();
    for (PMode.ExchangeParties.PartyInfo pp : pplst) {
      if (Objects.equals(pp.getPartyIdentitySetIdRef(), id)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<PMode.ExchangeParties.PartyInfo> getList() {
    if (pModeView.getEditable() != null) {
      if (pModeView.getEditable().getExchangeParties() == null) {
        pModeView.getEditable().setExchangeParties(new PMode.ExchangeParties());
      }
      return pModeView.getEditable().getExchangeParties().
              getPartyInfos();
    }
    return Collections.emptyList();
  }

  @Override
  public boolean persistEditable() {
    boolean bsuc = false;

    PMode.ExchangeParties.PartyInfo ecj = getEditable();
    if (ecj != null && pModeView.getEditable() != null) {
      if (pModeView.getEditable().getExchangeParties() == null) {
        pModeView.getEditable().setExchangeParties(
                new PMode.ExchangeParties());
      }

      bsuc = pModeView.getEditable().getExchangeParties().getPartyInfos().add(ecj);
    } else {
      addError("No editable payload!");
    }
    return bsuc;
  }

  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    PMode.ExchangeParties.PartyInfo ecj = getSelected();
    if (ecj != null && pModeView.getEditable() != null) {
      if (pModeView.getEditable().getExchangeParties() != null) {
        for (int i = 0; i < pModeView.getEditable().getExchangeParties().getPartyInfos().size(); i++) {
          PMode.ExchangeParties.PartyInfo pp = pModeView.getEditable().
                  getExchangeParties().getPartyInfos().get(i);
          if (Objects.equals(pp.getPartyIdentitySetIdRef(), ecj.getPartyIdentitySetIdRef())) {
            pModeView.getEditable().getExchangeParties().
                    getPartyInfos().remove(i);
            bSuc = true;
            break;
          }
        }

      } else {
        addError("No editable payload!");
      }
    }
    return bSuc;
  }

  @Override
  public boolean updateEditable() {
    boolean bSuc = false;
     PMode.ExchangeParties.PartyInfo ecj = getEditable();
    if (ecj != null && pModeView.getEditable() != null) {
      if (pModeView.getEditable().getExchangeParties() != null) {
        for (int i = 0; i < pModeView.getEditable().getExchangeParties().
                getPartyInfos().size(); i++) {
          PMode.ExchangeParties.PartyInfo pp = pModeView.getEditable().
                  getExchangeParties().getPartyInfos().get(i);
          if (Objects.equals(pp.getPartyIdentitySetIdRef(), ecj.getPartyIdentitySetIdRef())) {
            pModeView.getEditable().getExchangeParties().
                    getPartyInfos().remove(i);
            pModeView.getEditable().getExchangeParties().
                    getPartyInfos().add(i, ecj);
            bSuc = true;
            break;
          }
        }

      } else {
        addError("No editable payload!");
      }
    }
    return bSuc;
  }

  @Override
  public String getSelectedDesc() {
    return getSelected() != null ? getSelected().getPartyIdentitySetIdRef() : "";
  }

}
