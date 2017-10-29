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
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.pmode.enums.MEPChannelBindingType;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.pmode.MEPLegType;
import si.laurentius.msh.pmode.MEPTransportType;
import si.laurentius.msh.pmode.MEPType;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.TransportChannelType;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("pModeMEPView")
public class PModeMEPView extends AbstractPModeJSFView<MEPType> {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(PModeMEPView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPModeInteface;

  @Inject
  PModeView pModeView;

  PartyIdentitySet editableParty;

  public PModeView getpModeView() {
    return pModeView;
  }

  public void setpModeView(PModeView pModeView) {
    this.pModeView = pModeView;

  }

  public MEPLegType getEditableFirstLeg() {
    MEPType mt = getEditable();
    if (mt != null) {
      if (mt.getLegs().isEmpty()) {
        mt.getLegs().add(new MEPLegType());
      }
      MEPLegType mlt = mt.getLegs().get(0);
      // build leg objects
      buildLeg(mlt);
      return mlt;
    }
    return null;
  }

  private void buildLeg(MEPLegType mlt) {
    if (Utils.isEmptyString(mlt.getMPC())) {
      mlt.setMPC(
              "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC");
    }
    if (mlt.getTransport() == null) {
      mlt.setTransport(new MEPTransportType());

    }
    if (mlt.getTransport().getForeChannel() == null) {
      mlt.getTransport().setForeChannel(new TransportChannelType());
    }
    if (mlt.getTransport().getBackChannel() == null) {
      mlt.getTransport().setBackChannel(new TransportChannelType());
    }
  }

  public MEPLegType getEditableSecondLeg() {
    MEPType mt = getEditable();
    if (mt != null) {
      while (mt.getLegs().size() < 2) {
        MEPLegType mlt = new MEPLegType();
        buildLeg(mlt);
        mt.getLegs().add(mlt);
      }
      return mt.getLegs().get(1);
    }
    return null;
  }

  public boolean hasSecondLeg() {
    MEPType mt = getEditable();
    return mt != null && Objects.equals(mt.getMEPType(),
            si.laurentius.commons.pmode.enums.MEPType.TwoWay.getValue())
            && (Objects.equals(mt.getMEPChannelBinding(),
                    MEPChannelBindingType.PullAndPush.getValue())
            || Objects.equals(mt.getMEPChannelBinding(),
                    MEPChannelBindingType.PushAndPull.getValue())
            || Objects.equals(mt.getMEPChannelBinding(),
                    MEPChannelBindingType.PushAndPush.getValue()));
  }

  @Override
  public boolean validateData() {
    return true;
  }

  @Override
  public void createEditable() {
    MEPType mt = new MEPType();

    setNew(mt);

  }

  @Override
  public List<MEPType> getList() {
    return pModeView.getEditable() != null ? pModeView.getEditable().getMEPS()
            : Collections.emptyList();
  }

  @Override
  public boolean persistEditable() {
    boolean bsuc = false;

    MEPType ecj = getEditable();
    if (ecj != null && pModeView.getEditable() != null) {

      bsuc = pModeView.getEditable().getMEPS().add(
              ecj);
    } else {
      addError("No editable payload!");
    }
    return bsuc;
  }

  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    MEPType ecj = getSelected();

    if (ecj != null && pModeView.getEditable() != null) {

      for (int i = 0; i < pModeView.getEditable().getMEPS().size(); i++) {
        MEPType mt = pModeView.getEditable().getMEPS().get(i);
        if (mt.equals(ecj)) {
          pModeView.getEditable().getMEPS().remove(i);
          bSuc = true;
          break;
        }
      }
    } else {
      addError("No editable payload!");
    }

    return bSuc;
  }

  @Override
  public boolean updateEditable() {
    boolean bSuc = false;
    MEPType ecj = getEditable();
    MEPType selct = getSelected();
    if (selct != null && ecj != null && pModeView.getEditable() != null) {
      // selected 

      for (int i = 0; i < pModeView.getEditable().getMEPS().size(); i++) {
        MEPType mt = pModeView.getEditable().getMEPS().get(i);
        if (mt.equals(selct)) {
          pModeView.getEditable().getMEPS().remove(i);
          pModeView.getEditable().getMEPS().add(i, ecj);
          bSuc = true;
          break;
        }
      }

    } else {
      addError("No editable payload!");
    }
    return bSuc;
  }

  @Override
  public String getSelectedDesc() {
    return getSelected() != null ? getSelected().getMEPType() : "";
  }

  public boolean enableMEPBinnding(MEPChannelBindingType mt) {
    return Objects.
            equals(mt.getMepType().getValue(), getEditable().getMEPType());
  }

  public boolean firstLegInitLeft() {
    MEPType mt = getEditable();
    return mt == null
            || Objects.equals(mt.getMEPChannelBinding(),
                    MEPChannelBindingType.Push.getValue())
            || Objects.equals(mt.getMEPChannelBinding(),
                    MEPChannelBindingType.Sync.getValue())
            || Objects.equals(mt.getMEPChannelBinding(),
                    MEPChannelBindingType.PushAndPull.getValue())
            || Objects.equals(mt.getMEPChannelBinding(),
                    MEPChannelBindingType.PushAndPush.getValue());
  }

  public String firstLegMEP() {
    MEPType mt = getEditable();
    String mep = "";
    if (mt != null) {
      if (Objects.equals(mt.getMEPChannelBinding(), MEPChannelBindingType.Push.
              getValue())
              || Objects.equals(mt.getMEPChannelBinding(),
                      MEPChannelBindingType.PushAndPull.getValue())
              || Objects.equals(mt.getMEPChannelBinding(),
                      MEPChannelBindingType.PushAndPush.getValue())) {
        mep = "push";
      } else if (Objects.equals(mt.getMEPChannelBinding(),
              MEPChannelBindingType.Pull.getValue())
              || Objects.equals(mt.getMEPChannelBinding(),
                      MEPChannelBindingType.PullAndPush.getValue())) {
        mep = "pull";
      } else if (Objects.equals(mt.getMEPChannelBinding(),
              MEPChannelBindingType.Sync.getValue())) {
        mep = "sync";
      }

    }
    return mep;
  }

  public String secodLegMEP() {
    MEPType mt = getEditable();
    String mep = "";
    if (mt != null) {
      if (Objects.equals(mt.getMEPChannelBinding(),
              MEPChannelBindingType.PullAndPush.getValue())
              || Objects.equals(mt.getMEPChannelBinding(),
                      MEPChannelBindingType.PushAndPush.getValue())) {
        mep = "push";
      } else if (Objects.equals(mt.getMEPChannelBinding(),
              MEPChannelBindingType.PushAndPull.getValue())) {
        mep = "pull";
      }

    }
    return mep;
  }

  public boolean secondLegInitLeft() {
    MEPType mt = getEditable();
    return mt != null
            && Objects.equals(mt.getMEPChannelBinding(),
                    MEPChannelBindingType.PushAndPull.getValue());
  }

  public String getMEPTypeName(String strVal) {
    si.laurentius.commons.pmode.enums.MEPType mt = si.laurentius.commons.pmode.enums.MEPType.
            getByValue(strVal);
    return mt != null ? mt.getName() : strVal;
  }

  public String getMEPChannelBindingName(String strVal) {
    MEPChannelBindingType mt = MEPChannelBindingType.getByValue(strVal);
    return mt != null ? mt.getName() : strVal;
  }

  public String getMEPActions(MEPType mt) {
    String acts = "";
    for (MEPLegType mlt : mt.getLegs()) {
      MEPTransportType mtt = mlt.getTransport();
      if (mtt == null) {
        continue;
      }
      if (mtt.getForeChannel() != null && !Utils.isEmptyString(mtt.
              getForeChannel().getAction())) {
        acts += mtt.getForeChannel().getAction() + ",";
      }

      if (mtt.getBackChannel() != null && !Utils.isEmptyString(mtt.
              getBackChannel().getAction())) {
        acts += mtt.getBackChannel().getAction() + ",";
      }
    }
    return acts;

  }

}
