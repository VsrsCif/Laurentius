package si.laurentius.msh.web.gui.dlg;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "dialogDelete")
public class DialogDelete {
  private static final SEDLogger LOG = new SEDLogger(DialogDelete.class);

  AbstractAdminJSFView currentJSFView;

  public AbstractAdminJSFView getCurrentJSFView() {
    return currentJSFView;
  }

  public void setCurrentJSFView(AbstractAdminJSFView currentJSFView) {
    this.currentJSFView = currentJSFView;
  }

  public void removeSelectedRow() {
    if (currentJSFView != null) {
      currentJSFView.removeSelected();
    } else {
      LOG.logWarn("Remove selected row, but no view currentJSFView is setted!", null);
    }
  }

  public String getSelectedDesc() {
    if (currentJSFView != null) {
      return currentJSFView.getSelectedDesc();
    }
    return null;
  }

  public boolean getIsSelectedTableRow() {
    return currentJSFView != null && currentJSFView.getSelected() != null;
  }
  
  public String getTargetTable(){
    return currentJSFView != null? currentJSFView.getUpdateTargetTable():null;
  }

}
