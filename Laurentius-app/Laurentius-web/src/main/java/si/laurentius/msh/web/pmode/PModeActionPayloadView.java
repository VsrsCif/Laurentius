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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.pmode.enums.ActionRole;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.pmode.Service;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "pModeActionPayloadView")
public class PModeActionPayloadView extends AbstractPModeJSFView<Service.Action.PayloadProfiles.PayloadProfile> {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(PModeActionPayloadView.class);

  @ManagedProperty(value = "#{pModeServiceGraphView}")
  PModeServiceGraphView serviceGraphView;

  public PModeServiceGraphView getServiceGraphView() {
    return serviceGraphView;
  }

  public void setServiceGraphView(PModeServiceGraphView serviceGraphView) {
    this.serviceGraphView = serviceGraphView;
  }

  @Override
  public boolean validateData() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void createEditable() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Service.Action.PayloadProfiles.PayloadProfile> getList() {
    if (serviceGraphView.getEditable()!=null ){
      if (serviceGraphView.getEditable().getPayloadProfiles()==null){
        serviceGraphView.getEditable().setPayloadProfiles(new Service.Action.PayloadProfiles());
      }
      return serviceGraphView.getEditable().getPayloadProfiles().getPayloadProfiles();
    }
    return Collections.emptyList();
  }

  @Override
  public boolean persistEditable() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean removeSelected() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean updateEditable() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  
}
