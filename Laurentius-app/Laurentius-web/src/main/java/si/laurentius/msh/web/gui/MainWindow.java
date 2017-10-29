package si.laurentius.msh.web.gui;

import java.io.IOException;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.primefaces.event.TabChangeEvent;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("mainWindow")
public class MainWindow  implements Serializable{
  
  private static final SEDLogger LOG = new SEDLogger(MainWindow.class);

  String mstrWindowShow = AppConstant.S_PANEL_INBOX;
  int currentProgressVal =0;
  String currentProgressLabel ="";
  private boolean exportLookupsWithPasswords = true;
  
 
  /**
   *
   * @param summary
   * @param detail
   */
  public void addMessage(String summary, String detail) {
    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
    FacesContext.getCurrentInstance().addMessage(null, message);
  }

  /**
   *
   * @return
   */
  public String currentPanel() {
    return mstrWindowShow;
  }
  
  public void setCurrentPanel(String strVal) {
    this.mstrWindowShow = strVal;
  }

  /**
   *
   * @param event
   */
  public void onToolbarButtonAction(ActionEvent event) {
    if (event != null) {
      String res = (String) event.getComponent().getAttributes().get("panel");
      mstrWindowShow = res;
    }
  }

  /**
   *
   * @param event
   */
  public void onToolbarTabChange(TabChangeEvent event) {
    if (event != null) {
      mstrWindowShow = event.getTab().getId();
      LOG.formatedDebug("Tab Changed %s.", event.getTab().getId());    
    }
  }

  public int getCurrentProgressVal() {
    return currentProgressVal;
  }

  public void setCurrentProgressVal(int currentProgressVal) {
    this.currentProgressVal = currentProgressVal;
  }

  public String getCurrentProgressLabel() {
    return currentProgressLabel;
  }

  public void setCurrentProgressLabel(String currentProgressLabel) {
    this.currentProgressLabel = currentProgressLabel;
  }
  

}
