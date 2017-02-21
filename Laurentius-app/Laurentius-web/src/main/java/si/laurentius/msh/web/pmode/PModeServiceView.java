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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.web.gui.dlg.DialogDelete;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "pModeServiceView")
public class PModeServiceView extends AbstractPModeJSFView<Service> {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(PModeServiceView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPModeInteface;


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
    Service pmodeService = new Service();
    setNew(pmodeService);

  }

  /**
   *
   */
  @Override
  public void removeSelected() {

    long l = LOG.logStart();

    Service srv = getSelected();
    if (srv != null) {
      mPModeInteface.removeService(srv);
    }

  }

   @Override
  public boolean validateData() {
    
    return true;
  }
  /**
   *
   */
  @Override
  public boolean persistEditable() {
    long l = LOG.logStart();
    boolean bsuc = false;
    Service sv = getEditable();
    if (sv != null) {      
      mPModeInteface.addService(sv);
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
    Service sv = getEditable();
    if (sv != null) {      
      mPModeInteface.updateService(sv);
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
  public List<Service> getList() {
    long l = LOG.logStart();
    List<Service> lst = mPModeInteface.getServices();
    LOG.logEnd(l);
    return lst;

  }


}
