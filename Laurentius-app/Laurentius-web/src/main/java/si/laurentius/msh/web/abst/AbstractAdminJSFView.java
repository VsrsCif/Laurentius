/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.abst;

import java.util.List;
import javax.xml.bind.JAXBException;
import org.primefaces.context.RequestContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.web.gui.dlg.DialogDelete;
import si.laurentius.msh.web.gui.dlg.DialogXMLEdit;

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
  public void removeSelectedWithWarning(String updateTarget) {
    DialogDelete dlg = getDlgDelete();
    dlg.setCurrentJSFView(this, updateTarget);
    RequestContext context = RequestContext.getCurrentInstance();
    context.execute("PF('DlgDelete').show();");
    context.update("dlgalert:deleteDialog");
  };
  
  /**
   *
   */
  public void showEditableAsString(String updateTarget) {
    DialogXMLEdit dlg = getDialogXMLEdit();
    dlg.setCurrentJSFView(this, updateTarget);
    RequestContext context = RequestContext.getCurrentInstance();
    context.execute("PF('dialogXMLEdit').show();");
    context.update("dlgEntityEdit:dialogLayout");
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
  
  public DialogXMLEdit getDialogXMLEdit() {
    return (DialogXMLEdit)getBean("dialogXMLEdit");
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


 /**
   *
   * @return
   */
  public String getEditableAsString() {
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
  public void setEditableAsString(String strPMode) {

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
        LOG.logError("Error parsing xml string", ex);
      }
    }
  }

}
