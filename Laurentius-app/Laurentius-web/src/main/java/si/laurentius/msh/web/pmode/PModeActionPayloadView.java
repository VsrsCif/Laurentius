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

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.pmode.Action;
import si.laurentius.msh.pmode.PayloadProfile;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("pModeActionPayloadView")
public class PModeActionPayloadView extends AbstractPModeJSFView<PayloadProfile> {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(PModeActionPayloadView.class);

  @Inject
  PModeServiceGraphView serviceGraphView;

  public PModeServiceGraphView getServiceGraphView() {
    return serviceGraphView;
  }

  public void setServiceGraphView(PModeServiceGraphView serviceGraphView) {
    this.serviceGraphView = serviceGraphView;
  }

  @Override
  public boolean validateData() {
    return true;
  }

  @Override
  public void createEditable() {
    String sbname = "payload_%03d";
    int i = 1;
    while (payloadExists(String.format(sbname, i))) {
      i++;
    }

    PayloadProfile np = new PayloadProfile();
    np.setName(String.format(sbname, i));
    np.setMIME(MimeValue.MIME_BIN.getMimeType());
    np.setMaxOccurs(1);
    np.setMinOccurs(1);
    np.setMaxSize(BigInteger.valueOf(15 * 1024 * 1024));
    setNew(np);

  }

  boolean payloadExists(String name) {
    List<PayloadProfile> pplst = getList();
    for (PayloadProfile pp : pplst) {
      if (Objects.equals(pp.getName(), name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<PayloadProfile> getList() {
    if (serviceGraphView.getEditable() != null) {
      if (serviceGraphView.getEditable().getPayloadProfiles() == null) {
        serviceGraphView.getEditable().setPayloadProfiles(
                new Action.PayloadProfiles());
      }
      return serviceGraphView.getEditable().getPayloadProfiles().
              getPayloadProfiles();
    }
    return Collections.emptyList();
  }

  @Override
  public boolean persistEditable() {
    boolean bsuc = false;

    PayloadProfile ecj = getEditable();
    if (ecj != null && serviceGraphView.getEditable() != null) {
      if (serviceGraphView.getEditable().getPayloadProfiles() == null) {
        serviceGraphView.getEditable().setPayloadProfiles(
                new Action.PayloadProfiles());
      }

      bsuc = serviceGraphView.getEditable().getPayloadProfiles().
              getPayloadProfiles().add(ecj);
    } else {
      addError("No editable payload!");
    }
    return bsuc;
  }

  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    PayloadProfile ecj = getSelected();
    if (ecj != null && serviceGraphView.getEditable() != null) {
      if (serviceGraphView.getEditable().getPayloadProfiles() != null) {
        for (int i = 0; i < serviceGraphView.getEditable().getPayloadProfiles().
                getPayloadProfiles().size(); i++) {
          PayloadProfile pp = serviceGraphView.getEditable().
                  getPayloadProfiles().getPayloadProfiles().get(i);
          if (Objects.equals(pp.getName(), ecj.getName())) {
            serviceGraphView.getEditable().getPayloadProfiles().
                    getPayloadProfiles().remove(i);
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
     PayloadProfile ecj = getEditable();
    if (ecj != null && serviceGraphView.getEditable() != null) {
      if (serviceGraphView.getEditable().getPayloadProfiles() != null) {
        for (int i = 0; i < serviceGraphView.getEditable().getPayloadProfiles().
                getPayloadProfiles().size(); i++) {
          PayloadProfile pp = serviceGraphView.getEditable().
                  getPayloadProfiles().getPayloadProfiles().get(i);
          if (Objects.equals(pp.getName(), ecj.getName())) {
            serviceGraphView.getEditable().getPayloadProfiles().
                    getPayloadProfiles().remove(i);
            serviceGraphView.getEditable().getPayloadProfiles().
                    getPayloadProfiles().add(i, ecj);
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
    return getSelected() != null ? getSelected().getName() : "";
  }

}
