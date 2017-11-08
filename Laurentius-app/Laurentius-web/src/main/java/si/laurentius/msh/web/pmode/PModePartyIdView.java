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
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.pmode.PartyIdentitySetType;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("pModePartyIdView")
public class PModePartyIdView extends AbstractPModeJSFView<PartyIdentitySetType.PartyId> {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(PModePartyIdView.class);

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

    PartyIdentitySetType.PartyId np = new PartyIdentitySetType.PartyId();
    np.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered:si-svev:sed-box");
    np.setValueSource("address");

    setNew(np);

  }

  @Override
  public List<PartyIdentitySetType.PartyId> getList() {
    if (pModePartyView.getEditable() != null) {
      return pModePartyView.getEditable().getPartyIds();
    }
    return Collections.emptyList();
  }

  @Override
  public boolean persistEditable() {
    boolean bsuc = false;

    PartyIdentitySetType.PartyId ecj = getEditable();
    if (ecj != null && pModePartyView.getEditable() != null) {

      bsuc = pModePartyView.getEditable().getPartyIds().add(ecj);
    } else {
      addError("No editable party!");
    }
    return bsuc;
  }

  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    PartyIdentitySetType.PartyId ecj = getSelected();
    if (ecj != null && pModePartyView.getEditable() != null) {
      bSuc = pModePartyView.getEditable().getPartyIds().remove(ecj);
    } else {
      addError("No editable party!");
    }

    return bSuc;
  }

  @Override
  public boolean updateEditable() {
    boolean bSuc = false;
    PartyIdentitySetType.PartyId ecj = getEditable();
    if (ecj != null && pModePartyView.getEditable() != null) {
      getSelected().setType(ecj.getType());
      getSelected().setValueSource(ecj.getValueSource());
      getSelected().getIdentifiers().clear();
      getSelected().getIdentifiers().addAll(ecj.getIdentifiers());
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
  
  public List<String> getEditableIdentifiers(){
   if (getEditable() != null) {
      return getEditable().getIdentifiers();
    }
    return Collections.emptyList();
  }
  
  
  public void setEditableIdentifiers(List<String> lst){
   if (getEditable() != null) {
      getEditable().getIdentifiers().clear();
      if (lst!= null) {
        getEditable().getIdentifiers().addAll(lst);
      }
    }
  }

}
