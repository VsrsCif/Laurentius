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
import javax.faces.bean.SessionScoped;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.pmode.PMode;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "pModeView")
public class PModeView extends AbstractPModeJSFView<PMode> {

  /**
   *
   */
  public static SEDLogger LOG = new SEDLogger(PModeView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPModeInteface;

  /**
   *
   */
  @PostConstruct
  public void init() {

  }

  @Override
  public boolean validateData() {

    return true;
  }

  /**
   *
   */
  @Override
  public void createEditable() {
    PMode pmodePMode = new PMode();
    setNew(pmodePMode);

  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    PMode srv = getSelected();
    if (srv != null) {
      mPModeInteface.removePMode(srv);
      bSuc = true;
    }
    return bSuc;

  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {

    long l = LOG.logStart();
    boolean bsuc = false;
    PMode sv = getEditable();
    if (sv != null) {
      mPModeInteface.addPMode(sv);
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
    PMode sv = getEditable();
    if (sv != null) {
      mPModeInteface.updatePMode(sv);
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
  public List<PMode> getList() {
    long l = LOG.logStart();
    List<PMode> lst = mPModeInteface.getPModes();
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
