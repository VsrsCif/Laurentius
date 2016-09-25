/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.pmode;

import javax.xml.bind.JAXBException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

/**
 *
 * @author sluzba
 */
public abstract class AbstractPModeJSFView<T> extends AbstractAdminJSFView<T> {
  
  public static SEDLogger LOG = new SEDLogger(AbstractPModeJSFView.class);
  String curre = null;

  /**
   *
   * @return
   */
  public String getCurrentObjectAsString() {
    long l = LOG.logStart();
    String pmrs = "";
    Object val = getEditable();
    if (val != null) {
      try {
        pmrs = XMLUtils.serializeToString(val);
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
  public void setCurrentObjectAsString(String strPMode) {
    long l = LOG.logStart();
    Object pmed = getEditable();
    if (pmed != null) {
      try {
        T pmdNew = (T) XMLUtils.deserialize(strPMode, pmed.getClass());
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
  
}
