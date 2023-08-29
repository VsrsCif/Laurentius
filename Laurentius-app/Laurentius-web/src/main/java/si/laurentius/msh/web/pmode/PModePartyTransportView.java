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
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.ebox.SEDBox;
import si.laurentius.msh.pmode.PartyIdentitySetType;
import si.laurentius.msh.pmode.Protocol;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("pModePartyTransportView")
public class PModePartyTransportView extends AbstractPModeJSFView<PartyIdentitySetType.TransportProtocol> {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(
          PModePartyTransportView.class);

  @Inject
  PModePartyView pModePartyView;

  public PModePartyView getPModePartyView() {
    return pModePartyView;
  }

  public void setPModePartyView(PModePartyView pmpw) {
    this.pModePartyView = pmpw;
  }

  @Override
  public boolean validateData() {
    return true;
  }

  @Override
  public void createEditable() {

    PartyIdentitySetType.TransportProtocol np = new PartyIdentitySetType.TransportProtocol();
    String transport = "transport.%03d";
    int i = 1;
    while (getTransportByName(String.format(transport, i)) != null) {
      i++;
    }
    
    np.setId(String.format(transport, i));
    np.setAddress(new Protocol.Address());
    np.getAddress().setChunked(Boolean.FALSE);
    np.getAddress().setConnectionTimeout(120000);
    np.getAddress().setReceiveTimeout(120000);
    np.getAddress().setValue("http://localhost:8081/laurentius/msh");
            
    
            
    setNew(np);

  }
  
  public PartyIdentitySetType.TransportProtocol getTransportByName(String id){
    List<PartyIdentitySetType.TransportProtocol>  tplst = getList();
    for (PartyIdentitySetType.TransportProtocol tp: tplst){
      if (Objects.equals(tp.getId(), id)){
        return tp;
      }
    }
    return null;
  
  }

  @Override
  public List<PartyIdentitySetType.TransportProtocol> getList() {
    if (pModePartyView.getEditable() != null) {
      return pModePartyView.getEditable().getTransportProtocols();
    }
    return Collections.emptyList();
  }

  @Override
  public boolean persistEditable() {
    boolean bsuc = false;

    PartyIdentitySetType.TransportProtocol ecj = getEditable();
    if (ecj != null && pModePartyView.getEditable() != null) {

      bsuc = pModePartyView.getEditable().getTransportProtocols().add(ecj);
    } else {
      addError("No editable party!");
    }
    return bsuc;
  }

  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    PartyIdentitySetType.TransportProtocol ecj = getSelected();
    if (ecj != null && pModePartyView.getEditable() != null) {
      bSuc = pModePartyView.getEditable().getTransportProtocols().remove(ecj);
    } else {
      addError("No editable party!");
    }

    return bSuc;
  }

  @Override
  public boolean updateEditable() {
    boolean bSuc = false;
    PartyIdentitySetType.TransportProtocol ecj = getEditable();
    if (ecj != null && pModePartyView.getEditable() != null) {
      getSelected().setAddress(ecj.getAddress());
      getSelected().setBase64Encoded(ecj.isBase64Encoded());
      getSelected().setGzipCompress(ecj.isGzipCompress());
      getSelected().setIsTwoWayProtocol(ecj.isIsTwoWayProtocol());
      getSelected().setSOAPVersion(ecj.getSOAPVersion());
      getSelected().setTLS(ecj.getTLS());
      getSelected().setSMTP(ecj.getSMTP());

      bSuc = true;
    } else {
      addError("No editable payload!");
    }
    return bSuc;
  }

  @Override
  public String getSelectedDesc() {
    return getSelected() != null ? getSelected().toString() : "";
  }

  public Protocol.TLS getEditableTransportTLS() {
    PartyIdentitySetType.TransportProtocol tc = getEditable();

    if (tc != null) {
      if (tc.getTLS() == null) {
        tc.setTLS(new Protocol.TLS());
      }
      return tc.getTLS();
    }
    return null;
  }

  public Protocol.Address getEditableAddress() {
    PartyIdentitySetType.TransportProtocol tc = getEditable();
    if (tc != null) {
      if (tc.getAddress() == null) {
        tc.setAddress(new Protocol.Address());
      }
      return tc.getAddress();
    }
    return null;
  }

}
