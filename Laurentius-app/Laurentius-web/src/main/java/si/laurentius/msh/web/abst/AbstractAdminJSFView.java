/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.abst;

import java.util.List;
import org.primefaces.context.RequestContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;

/**
 *
 * @author Jože Rihtaršič
 * @param <T>
 */
abstract public class AbstractAdminJSFView<T> extends AbstractJSFView {
private static final SEDLogger LOG = new SEDLogger(AbstractAdminJSFView.class);
  private T mtEditable;
  private T mtNew;
  private T mtSelected;

  /**
   *
   */
  public void addOrUpdateEditable() {
    long l = LOG.logStart();
    boolean bsuc = false;
    
    if (validateData()) {
      if (isEditableNew()) {
        bsuc = persistEditable();
        setNew(null);
      } else {
        bsuc = updateEditable();
        setEditable(null);
      }
    }
    RequestContext.getCurrentInstance().addCallbackParam("saved", bsuc);
    LOG.logEnd(l);
  }

  abstract public boolean validateData();

  /**
   *
   */
  abstract public void createEditable();

  /**
   *
   * @return
   */
  public T getEditable() {
    return mtEditable;
  }

  /**
   *
   * @return
   */
  abstract public List<T> getList();

  /**
   *
   * @return
   */
  public T getNew() {
    return mtNew;
  }

  /**
   *
   * @return
   */
  public T getSelected() {
    return mtSelected;
  }

  /**
   *
   * @return
   */
  public boolean isEditableNew() {
    return getEditable() != null && getEditable() == getNew();
  }

  /**
   *
   */
  abstract public boolean persistEditable();

  /**
   *
   */
  abstract public void removeSelected();

  /**
   *
   * @param edtbl
   */
  public void setEditable(T edtbl) {
    if (edtbl!= null) {
      // create a copy
      this.mtEditable =  XMLUtils.deepCopyJAXB(edtbl);    
    } else {
      this.mtEditable = null;
    }
  }

  /**
   *
   * @param edtbl
   */
  public void setNew(T edtbl) {
    this.mtNew = edtbl;
    this.mtEditable =edtbl;
  }

  /**
   *
   * @param slct
   */
  public void setSelected(T slct) {
    this.mtSelected = slct;
  }

  ;

  /**
     *
     */
  public void startEditSelected() {
    setEditable(getSelected());
  }

  /**
   *
   * @return 
   */
  abstract public boolean updateEditable();

}
