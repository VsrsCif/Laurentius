package si.laurentius.msh.web.gui.dlg;

import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.xml.bind.JAXBException;
import org.primefaces.context.RequestContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import static si.laurentius.msh.web.abst.AbstractAdminJSFView.CB_PARA_SAVED;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("dialogXMLEdit")
public class DialogXMLEdit  implements Serializable{

  private static final SEDLogger LOG = new SEDLogger(DialogXMLEdit.class);

  AbstractAdminJSFView currentJSFView;
  String updateTarget;
  String value;
  
  Object editable = null;

  public AbstractAdminJSFView getCurrentJSFView() {
    return currentJSFView;
  }

  public void setCurrentJSFView(AbstractAdminJSFView currentJSFView,
          String update) {
    this.currentJSFView = currentJSFView;
    updateTarget = update;
    if (currentJSFView != null) {
      value = currentJSFView.getEditableAsString();
    } else {
      value = "";

    }
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

 

  public void saveCurrentValue() {
    if(currentJSFView!=null){
      currentJSFView.setEditableAsString(value);
         addCallbackParam(CB_PARA_SAVED, true);
    } else {
      String  msg = "No JSF view setted to XML Dialog!";
      LOG.logError(msg, null);
      addError(msg);
      addCallbackParam(CB_PARA_SAVED, false);
    }

  }

  public String getUpdateTarget() {
    return updateTarget;
  }

  public void addCallbackParam(String val, boolean bval) {
    RequestContext.getCurrentInstance().addCallbackParam(val, bval);
  }
  protected void addError(String desc) {
    FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    desc));

  }
  
  public void setEditable(Object obj){
    editable = obj;
    if (editable!=null){
      try {
        value = XMLUtils.serializeToString(editable);
      } catch (JAXBException ex) {
        LOG.logError("Error serializing message", ex);
      }
    }
  }
  public Object getEditable(){
      if (editable!=null){
        try {
          return XMLUtils.deserialize(value, editable.getClass());
        } catch (JAXBException ex) {
            LOG.logError("Error serializing message", ex);
        }
      } return null;
  }

}
