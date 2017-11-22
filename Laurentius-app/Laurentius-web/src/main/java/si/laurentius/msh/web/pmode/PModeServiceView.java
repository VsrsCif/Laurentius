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
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.pmode.enums.ActionRole;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.pmode.Action;
import si.laurentius.msh.pmode.PayloadProfile;
import si.laurentius.msh.pmode.Service;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("pModeServiceView")
public class PModeServiceView extends AbstractPModeJSFView<Service> {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(PModeServiceView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPModeInteface;

  @Inject
  PModeServiceGraphView serviceGraphView;

  public PModeServiceGraphView getServiceGraphView() {
    return serviceGraphView;
  }

  public void setServiceGraphView(PModeServiceGraphView serviceGraphView) {
    this.serviceGraphView = serviceGraphView;
  }

  /**
   *
   */
  @Override
  public void createEditable() {

    String sbname = "service_%03d";
    int i = 1;

    while (mPModeInteface.getServiceById(String.format(sbname, i)) != null) {
      i++;
    }
    String service = String.format(sbname, i);
    Service srv = new Service();
    srv.setId(service);
    srv.setServiceName(service);
    srv.setInitiator(new Service.Initiator());
    srv.setExecutor(new Service.Executor());

    srv.getInitiator().setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
    srv.getExecutor().setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");

    srv.setServiceType(
            "http://" + SEDSystemProperties.getLocalDomain() + "/service");
    srv.setUseSEDProperties(Boolean.TRUE);

    Action act = new Action();
    act.setName("TestAction");
    act.setInvokeRole(ActionRole.Initiator.getValue());
    act.setPayloadProfiles(new Action.PayloadProfiles());

    PayloadProfile pf = new PayloadProfile();
    pf.setName("payload");
    pf.setMIME(MimeValue.MIME_BIN.getMimeType());
    pf.setMinOccurs(0);
    pf.setMaxOccurs(100);
    pf.setMaxSize(BigInteger.valueOf(10 * 1024 * 1024));
    
    act.getPayloadProfiles().setMaxSize(BigInteger.valueOf(10 * 1024 * 1024));
    act.getPayloadProfiles().getPayloadProfiles().add(pf);

    srv.getActions().add(act);

    setNew(srv);
    serviceGraphView.setService(getEditable());

  }

  public String getEditableInitiatorRole() {
    Service ed = getEditable();
    if (ed != null) {
      if (ed.getInitiator() == null) {
        ed.setInitiator(new Service.Initiator());
      }
      return ed.getInitiator().getRole();
    }
    return null;
  }

  public void setEditableInitiatorRole(String val) {
    Service ed = getEditable();
    if (ed != null) {
      if (ed.getInitiator() == null) {
        ed.setInitiator(new Service.Initiator());
      }
      ed.getInitiator().setRole(val);
    }

  }

  public String getEditableExecutorRole() {
    Service ed = getEditable();
    if (ed != null) {
      if (ed.getExecutor() == null) {
        ed.setExecutor(new Service.Executor());
      }
      return ed.getExecutor().getRole();
    }
    return null;
  }

  public void setEditableExecutorRole(String val) {
    Service ed = getEditable();
    if (ed != null) {
      if (ed.getExecutor() == null) {
        ed.setExecutor(new Service.Executor());
      }
      ed.getExecutor().setRole(val);
    }
  }

  @Override
  public void startEditSelected() {
    super.startEditSelected();
    // set editable service to view
    serviceGraphView.setService(getEditable());
  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;

    Service srv = getSelected();
    if (srv != null) {
      mPModeInteface.removeService(srv);
      bSuc = true;
    }
    return bSuc;

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


  @Override
  public String getSelectedDesc() {
    if (getSelected() != null) {
      return getSelected().getId();
    }
    return null;
  }
}
