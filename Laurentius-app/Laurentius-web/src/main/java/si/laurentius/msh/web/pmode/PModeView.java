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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.xml.bind.JAXBException;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "pModeView")
public class PModeView extends AbstractAdminJSFView<PMode> {

  /**
     *
     */
  public static SEDLogger LOG = new SEDLogger(PModeView.class);

//  PModeManager pm = new PModeManager();
  String curre = null;

  private Map<String, String> mLookupMep;
  private Map<String, String> mLookupMepBinding;

  /**
     *
     */
  @PostConstruct
  public void init() {
    mLookupMep = new HashMap<>();
    mLookupMep.put("One-Way MEP",
        "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/oneWay");
    mLookupMep.put("Two-Way MEP",
        "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/twoWay");
    mLookupMepBinding = new HashMap<>();
    mLookupMepBinding.put("Push",
        "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/push");
    mLookupMepBinding.put("Pull",
        "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pull");
    mLookupMepBinding.put("Sync",
        "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/sync");
    mLookupMepBinding.put("PushAndPush",
        "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pushAndPush");
    mLookupMepBinding.put("PushAndPull",
        "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pushAndPull");
    mLookupMepBinding.put("PullAndPush",
        "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pullAndPush");
  }

  /**
     *
     */
  @Override
  public void createEditable() {
    PMode pmode = new PMode();
    setNew(pmode);

  }

  /**
     *
     */
  @Override
  public void removeSelected() {
   /*
    long l = LOG.logStart();
    try {
      System.out.println("remove selected");
      if (getSelected() != null) {
        System.out.println("remove selected 1");
        pm.removePMode(getSelected());
      }
      System.out.println("Save PModes");
      pm.savePMode();
    } catch (PModeException ex) {
      LOG.logError(l, null, ex);
    }*/
  }

  /**
     *
     */
  @Override
  public void persistEditable() {
    long l = LOG.logStart();
    /*PMode pmode = getEditable();
    System.out.println("persistEditable");
    if (pmode != null) {
      System.out.println("persistEditable 2");
      pm.add(pmode);
      setEditable(null);
      try {
        System.out.println("age persistEditable");
        pm.savePMode();
      } catch (PModeException ex) {
        LOG.logError(l, null, ex);
      }
    }*/
  }

  /**
     *
     */
  @Override
  public void updateEditable() {
    long l = LOG.logStart();
    PMode pmode = getEditable();
  /*  if (pmode != null) {
      try {
        pm.savePMode();
      } catch (PModeException ex) {
        LOG.logError(l, null, ex);
      }
    }*/
  }

  /**
   *
   * @return
   */
  @Override
  public List<PMode> getList() {
    long l = LOG.logStart();
/*
    try {
      return pm.getPModeList();
    } catch (PModeException ex) {
      LOG.logError(l, null, ex);
    }*/
    return null;
  }

  /**
   *
   * @return
   */
  public String getCurrentPModeAsString() {
    long l = LOG.logStart();
    String pmrs = "";
    PMode pmed = getEditable();
    if (pmed != null) {
      try {
        pmrs = XMLUtils.serializeToString(pmed);
      } catch (JAXBException ex) {
        LOG.logError(l, null, ex);
      }
    }
    return pmrs;
  }

  /**
   *
   * @param strPMode
   */
  public void setCurrentPModeAsString(String strPMode) {
    long l = LOG.logStart();

    PMode pmed = getEditable();
    if (pmed != null) {
      try {
        PMode pmdNew = (PMode) XMLUtils.deserialize(strPMode, PMode.class);

        if (pmed == getNew()) {
          setNew(pmdNew);
        } else {
          setEditable(pmdNew);
//          pm.replace(pmdNew, pmed.getId());
        }

      } catch (JAXBException ex) {
        LOG.logError(l, null, ex);
      }
    }

  }

  /**
   *
   * @return
   */
  public Map<String, String> getLookupMEP() {
    return mLookupMep;
  }

  /**
   *
   * @return
   */
  public Map<String, String> getLookupMEPBinding() {
    return mLookupMepBinding;
  }

  /**
     *
     */
  public void formatPMode() {
    // / setPModeString(XMLUtils.format(getPModeString()));
  }

}
