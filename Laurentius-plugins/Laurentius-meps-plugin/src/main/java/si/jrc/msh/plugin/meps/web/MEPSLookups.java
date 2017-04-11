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
package si.jrc.msh.plugin.meps.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.ejb.EJB;
import javax.faces.application.ViewHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import si.jrc.msh.plugin.meps.ejb.MEPSDataInterface;
import si.laurentius.commons.SEDGUIConstants;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.plugin.meps.PhysicalAddressType;
import si.laurentius.plugin.meps.ServiceType;
import si.laurentius.user.SEDUser;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "MEPSLookups")
public class MEPSLookups {


  @EJB(mappedName = "java:global/plugin-meps/MEPSDataBean!si.jrc.msh.plugin.meps.ejb.MEPSDataInterface")
  MEPSDataInterface mdata;
  
  
  public List<ServiceType> getServices(){
    return mdata.getServices();
  }
  
  public List<PhysicalAddressType> getAddresses(){
    return mdata.getAddresses();
  }
          
  public PhysicalAddressType getSenderAddress(){
    return mdata.getSenderAddress();
  }
}
