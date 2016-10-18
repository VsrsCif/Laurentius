package si.laurentius.msh.web.gui;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "MainWindow")
public class MainWindow {

  String mstrWindowShow = AppConstant.S_PANEL_INBOX;
  int currentProgressVal =0;
  String currentProgressLabel ="";
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
      FacesMessage msg = new FacesMessage("Tab Changed", "Active Tab: " + event.getTab().getId());
      FacesContext.getCurrentInstance().addMessage(null, msg);
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
