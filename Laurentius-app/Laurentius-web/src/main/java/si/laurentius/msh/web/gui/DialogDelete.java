package si.laurentius.msh.web.gui;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "dialogDelete")
public class DialogDelete {

  String dlgMessage;
  String dlgTitle;
  boolean yesNoOption;

 
  
  
  
  AbstractAdminJSFView currentJSFView;

  public AbstractAdminJSFView getCurrentJSFView() {
    return currentJSFView;
  }

  public void setCurrentJSFView(AbstractAdminJSFView currentJSFView) {
    this.currentJSFView = currentJSFView;
  }

  public String getMessage() {
    return dlgMessage;
  }

  public String getTitle() {
    return dlgTitle;
  }

  public boolean isYesNoOption() {
    return yesNoOption;
  }
  
   public void setDlgMessage(String dlgMessage) {
    this.dlgMessage = dlgMessage;
  }

  public void setDlgTitle(String dlgTitle) {
    this.dlgTitle = dlgTitle;
  }

  public void setYesNoOption(boolean yesNoOption) {
    this.yesNoOption = yesNoOption;
  }
  
  public void removeSelected(){
    if (currentJSFView!=null) {
      removeSelected();
    }
  }
 
 
   

}
