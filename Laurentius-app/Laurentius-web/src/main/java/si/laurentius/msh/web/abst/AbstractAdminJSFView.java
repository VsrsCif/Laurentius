/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.abst;

import java.util.List;
import javax.faces.bean.ManagedProperty;
import org.primefaces.context.RequestContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.web.gui.dlg.DialogDelete;

/**
 *
 * @author Jože Rihtaršič
 * @param <T>
 */
abstract public class AbstractAdminJSFView<T> extends AbstractJSFView {
  


  public static final String CB_PARA_SAVED = "saved";
  public static final String CB_PARA_REMOVED = "removed";

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
        if (persistEditable()) {
          setNew(null);
          bsuc = true;
        }
      } else {
        if (updateEditable()) {
          setEditable(null);
          bsuc = true;
        }
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

  public String getSelectedDesc() {
    if (mtSelected != null) {
      return mtSelected.toString();
    }
    return null;
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
  abstract public boolean removeSelected();

  /**
   *
   */
  public void removeSelectedWithWarning() {
    DialogDelete dlg = getDlgDelete();
    dlg.setCurrentJSFView(this);
    RequestContext context = RequestContext.getCurrentInstance();
    context.execute("PF('DlgDelete').show();");
    context.update("dlgalert:deleteDialog");
  };
  
 public  Object getBean(final String beanName) {
    final Object returnObject = facesContext().getELContext().getELResolver().
            getValue( facesContext().getELContext(), null, beanName);    
    if (returnObject == null) {
      LOG.formatedWarning("Bean with name %s was not found!", beanName);
    }
    return returnObject;
  }


  public DialogDelete getDlgDelete() {
    return (DialogDelete)getBean("dialogDelete");
  }


  /**
   *
   * @param edtbl
   */
  public void setEditable(T edtbl) {
    if (edtbl != null) {
      // create a copy
      this.mtEditable = XMLUtils.deepCopyJAXB(edtbl);
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
    this.mtEditable = edtbl;
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

  public String getUpdateTargetTable() {
    return null;
  }

  ;
   
   public void addCallbackParam(String val, boolean bval) {
    RequestContext.getCurrentInstance().addCallbackParam(val, bval);
  }

}
