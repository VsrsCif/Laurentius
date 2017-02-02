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
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.ReceptionAwareness;
import si.laurentius.msh.web.gui.DialogDelete;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "pModeReceptionAwarenessView")
public class PModeReceptionAwarenessView extends AbstractPModeJSFView<ReceptionAwareness> {

  /**
   *
   */
  public static SEDLogger LOG = new SEDLogger(PModeReceptionAwarenessView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPModeInteface;

   @ManagedProperty(value = "#{dialogDelete}")
  private DialogDelete dlgDelete;

  @Override
  public DialogDelete getDlgDelete() {
    return dlgDelete;
  }
  @Override
  public  void setDlgDelete(DialogDelete dlg){
    dlgDelete = dlg;
  }
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
    ReceptionAwareness pmodeReceptionAwareness = new ReceptionAwareness();
    setNew(pmodeReceptionAwareness);

  }

  /**
   *
   */
  @Override
  public void removeSelected() {

    long l = LOG.logStart();

    ReceptionAwareness srv = getSelected();
    if (srv != null) {
      mPModeInteface.removeReceptionAwareness(srv);
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
    ReceptionAwareness sv = getEditable();
    if (sv != null) {      
      mPModeInteface.addReceptionAwareness(sv);
      setEditable(null);
      bsuc= true;
    }
    return bsuc;
  }

  /**
   *
   */
  @Override
  public boolean  updateEditable() {
    long l = LOG.logStart();
    boolean bsuc = false;
    ReceptionAwareness sv = getEditable();
    if (sv != null) {      
      mPModeInteface.updateReceptionAwareness(sv);
      setEditable(null);
      bsuc= true;
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<ReceptionAwareness> getList() {
    long l = LOG.logStart();
    List<ReceptionAwareness> lst = mPModeInteface.getReceptionAwarenesses();
    LOG.logEnd(l);
    return lst;

  }


}
