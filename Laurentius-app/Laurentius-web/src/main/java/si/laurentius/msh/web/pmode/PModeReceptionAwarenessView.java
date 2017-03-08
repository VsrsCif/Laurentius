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
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.xml.datatype.Duration;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.pmode.ReceptionAwareness;

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
     String sbname = "RA_%03d";
    int i = 1;

    while (existRAById(String.format(sbname, i))) {
      i++;
    }
    ReceptionAwareness ra = new ReceptionAwareness();
    ra.setId(String.format(sbname, i));
    ra.setReceiptType("AS4Receipt");
    ra.setReplyPattern("response");
    ra.setDuplicateDetection(new ReceptionAwareness.DuplicateDetection());
    ra.setRetry(new ReceptionAwareness.Retry());
    
    ra.getDuplicateDetection().setEliminate(Boolean.TRUE);
    
    
    ra.getRetry().setMaxRetries(3);
    ra.getRetry().setMultiplyPeriod(5);
    ra.getRetry().setPeriod(5227);
    
    
    setNew(ra);

  }
  
  
   private boolean existRAById(String id){
     List<ReceptionAwareness> lst = mPModeInteface.getReceptionAwarenesses();
     for (ReceptionAwareness ra: lst){
       if (Objects.equals(ra.getId(), id)){
         return true;
       }
     }
     return false;
   }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    ReceptionAwareness srv = getSelected();
    if (srv != null) {
      mPModeInteface.removeReceptionAwareness(srv);
      bSuc = true;
    }
    return bSuc;

  }

   @Override
  public boolean validateData() {
    ReceptionAwareness cj = getEditable();
     if (isEditableNew() && existRAById(cj.getId())) {
      addError("Name: '" + cj.getId()+ "' already exists!");
      return false;
    }
    
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

@Override
  public String getUpdateTargetTable() {
    return ":forms:SettingsPModeReceptionAwarenesses:pmodeRAPanel:TblPModeReceptionAwareness";
  }

  @Override
  public String getSelectedDesc() {
     if (getSelected() != null) {
      return getSelected().getId();
    }
    return null;
  }
}
