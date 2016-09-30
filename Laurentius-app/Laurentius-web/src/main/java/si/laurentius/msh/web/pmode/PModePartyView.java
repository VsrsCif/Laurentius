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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.html.HtmlInputText;
import javax.faces.event.ValueChangeEvent;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.PartyIdentitySetType;
import si.laurentius.msh.pmode.Protocol;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "pModePartyView")
public class PModePartyView extends AbstractPModeJSFView<PartyIdentitySet> {

  /**
   *
   */
  public static SEDLogger LOG = new SEDLogger(PModePartyView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPModeInteface;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface mLookUp;

  PartyIdentitySetType.PartyId selectedPartyId;

  /**
   *
   */
  @PostConstruct
  public void init() {

  }

  /**
   *
   */
  @Override
  public void createEditable() {
    PartyIdentitySet pmodePartyIdentitySet = new PartyIdentitySet();

    setNew(pmodePartyIdentitySet);

  }

  @Override
  public void startEditSelected() {
    if (getSelected() != null && getSelected().getLocalPartySecurity() == null) {
      getSelected().setLocalPartySecurity(new PartyIdentitySetType.LocalPartySecurity());
    }
    if (getSelected() != null && getSelected().getExchangePartySecurity() == null) {
      getSelected().setExchangePartySecurity(new PartyIdentitySetType.ExchangePartySecurity());
    }
    super.startEditSelected(); // To change body of generated methods, choose Tools | Templates.
  }

  /**
   *
   */
  @Override
  public void removeSelected() {

    PartyIdentitySet srv = getSelected();
    if (srv != null) {
      mPModeInteface.removePartyIdentitySet(srv);
    }

  }

  /**
   *
   */
  @Override
  public void persistEditable() {
    long l = LOG.logStart();
    PartyIdentitySet sv = getEditable();
    if (sv != null) {
      mPModeInteface.addPartyIdentitySet(sv);
      setEditable(null);
    }
  }

  /**
   *
   */
  @Override
  public void updateEditable() {
    long l = LOG.logStart();
    PartyIdentitySet sv = getEditable();
    if (sv != null) {
      mPModeInteface.updatePartyIdentitySet(sv);
      setEditable(null);
    }
  }

  /**
   *
   * @return
   */
  @Override
  public List<PartyIdentitySet> getList() {
    long l = LOG.logStart();
    List<PartyIdentitySet> lst = mPModeInteface.getPartyIdentitySets();
    LOG.logEnd(l);
    return lst;

  }

  public void setEditableLocalIdentity(boolean bVal) {
    if (getEditable() != null) {
      getEditable().setIsLocalIdentity(bVal);
    }
  }

  public boolean getEditableLocalIdentity() {
    return getEditable() != null ? getEditable().getIsLocalIdentity() : false;
  }

  public List<SEDCertificate> getCurrentLocalKeystoreCerts() {
    if (getEditable() != null && getEditable().getLocalPartySecurity() != null &&
        !Utils.isEmptyString(getEditable().getLocalPartySecurity().getKeystoreName())) {
      String keystoreName = getEditable().getLocalPartySecurity().getKeystoreName();
      SEDCertStore cs = mLookUp.getSEDCertStoreByName(keystoreName);
      if (cs != null) {
        return cs.getSEDCertificates();
      }
    }
    return Collections.emptyList();
  }

  public List<SEDCertificate> getCurrentTLSKeyCerts() {
    if (getCurrrentTransportTLS() != null && 
        !Utils.isEmptyString(getCurrrentTransportTLS().getKeyStoreName())) {
      String keystoreName = getCurrrentTransportTLS().getKeyStoreName();
      SEDCertStore cs = mLookUp.getSEDCertStoreByName(keystoreName);
      if (cs != null) {
        return cs.getSEDCertificates();
      }
    }
    return Collections.emptyList();
  }
  
   public List<SEDCertificate> getCurrentExchangeTruststoreCerts() {
    if (getEditable() != null && getEditable().getExchangePartySecurity() != null &&
        !Utils.isEmptyString(getEditable().getExchangePartySecurity().getTrustoreName())) {
      String keystoreName = getEditable().getExchangePartySecurity().getTrustoreName();
      SEDCertStore cs = mLookUp.getSEDCertStoreByName(keystoreName);
      if (cs != null) {
        return cs.getSEDCertificates();
      }
    }
    return Collections.emptyList();
  }
  
  
  public String getListAsString(List<String> lst) {
    return lst != null ? String.join(",", lst) : "";
  }

  public PartyIdentitySetType.TransportProtocol getCurrentTransportProtocol() {
    if (getEditable() != null) {
      if (getEditable().getTransportProtocols().isEmpty()) {
        getEditable().getTransportProtocols().add(new PartyIdentitySetType.TransportProtocol());
      }
      return getEditable().getTransportProtocols().get(0);
    }
    return null;
  }

  public Protocol.TLS getCurrrentTransportTLS() {
    PartyIdentitySetType.TransportProtocol tc = getCurrentTransportProtocol();
    if (tc != null) {
      if (tc.getTLS() == null) {
        tc.setTLS(new Protocol.TLS());
      }
      return tc.getTLS();
    }
    return null;
  }
  
 
  public Protocol.Proxy getCurrrentProxy() {
    PartyIdentitySetType.TransportProtocol tc = getCurrentTransportProtocol();
    if (tc != null) {
      if (tc.getProxy() == null) {
        tc.setProxy(new Protocol.Proxy());
      }
      return tc.getProxy();
    }
    return null;
  }
  
   public Protocol.Address getCurrrentAddress() {
    PartyIdentitySetType.TransportProtocol tc = getCurrentTransportProtocol();
    if (tc != null) {
      if (tc.getAddress() == null) {
        tc.setAddress(new Protocol.Address());
      }
      return tc.getAddress();
    }
    return null;
  }

}
