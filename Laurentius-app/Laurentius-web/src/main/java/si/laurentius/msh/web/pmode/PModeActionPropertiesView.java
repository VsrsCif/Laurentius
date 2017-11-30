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
import si.laurentius.msh.pmode.Action;
import si.laurentius.msh.pmode.PayloadProfile;
import si.laurentius.msh.pmode.Property;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("pModeActionPropertiesView")
public class PModeActionPropertiesView extends AbstractPModeJSFView<Action.Properties.Property> {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(PModeActionPropertiesView.class);

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
    String sbname = "key_%03d";
    int i = 1;
    while (propertyExists(String.format(sbname, i))) {
      i++;
    }

    Action.Properties.Property  np = new Action.Properties.Property();
    np.setName(String.format(sbname, i));
    np.setRequired(Boolean.FALSE);
    setNew(np);

  }

  boolean propertyExists(String name) {
    List<Action.Properties.Property> pplst = getList();
    for (Property pp : pplst) {
      if (Objects.equals(pp.getName(), name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<Action.Properties.Property> getList() {
    if (serviceGraphView.getEditable() != null) {
      if (serviceGraphView.getEditable().getProperties() == null) {
        serviceGraphView.getEditable().setProperties(
                new Action.Properties());
      }
      return serviceGraphView.getEditable().getProperties().
              getProperties();
    }
    return Collections.emptyList();
  }

  @Override
  public boolean persistEditable() {
    boolean bsuc = false;

    Action.Properties.Property ecj = getEditable();
    if (ecj != null && serviceGraphView.getEditable() != null) {
      if (serviceGraphView.getEditable().getProperties() == null) {
        serviceGraphView.getEditable().setProperties(
                new Action.Properties());
      }

      bsuc = serviceGraphView.getEditable().getProperties().getProperties().add(ecj);
    } else {
      addError("No editable Property!");
    }
    return bsuc;
  }

  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    Action.Properties.Property ecj = getSelected();
    if (ecj != null && serviceGraphView.getEditable() != null) {
      if (serviceGraphView.getEditable().getProperties() != null) {
        for (int i = 0; i < serviceGraphView.getEditable().getProperties().
                getProperties().size(); i++) {
           Action.Properties.Property pp = serviceGraphView.getEditable().
                  getProperties().getProperties().get(i);
          if (Objects.equals(pp.getName(), ecj.getName())) {
            serviceGraphView.getEditable().getProperties().
                    getProperties().remove(i);
            bSuc = true;
            break;
          }
        }

      } else {
        addError("No editable Property!");
      }
    }
    return bSuc;
  }

  @Override
  public boolean updateEditable() {
    boolean bSuc = false;
      Action.Properties.Property ecj = getEditable();
    if (ecj != null && serviceGraphView.getEditable() != null) {
      if (serviceGraphView.getEditable().getProperties() != null) {
        for (int i = 0; i < serviceGraphView.getEditable().getProperties().
                getProperties().size(); i++) {
           Action.Properties.Property pp = serviceGraphView.getEditable().
                  getProperties().getProperties().get(i);
          if (Objects.equals(pp.getName(), ecj.getName())) {
            serviceGraphView.getEditable().getProperties().
                    getProperties().remove(i);
            serviceGraphView.getEditable().getProperties().
                    getProperties().add(i, ecj);
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
