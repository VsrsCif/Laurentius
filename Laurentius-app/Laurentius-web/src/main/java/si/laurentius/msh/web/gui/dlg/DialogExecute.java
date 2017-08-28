package si.laurentius.msh.web.gui.dlg;

import java.util.Collections;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.context.RequestContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.cron.SEDTask;
import si.laurentius.msh.web.abst.AbstractJSFView;
import si.laurentius.msh.web.admin.AdminSEDCronJobView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "executeDialog")
public class DialogExecute {

  private static final SEDLogger LOG = new SEDLogger(DialogExecute.class);

  AdminSEDCronJobView currentJSFView;
  String updateTarget;

  public AdminSEDCronJobView getCurrentJSFView() {
    return currentJSFView;
  }

  public void setCurrentJSFView(AdminSEDCronJobView currentJSFView,
          String update) {
    this.currentJSFView = currentJSFView;
    updateTarget = update;
  }

  public List<SEDTask> getTasks() {

    if (currentJSFView != null && currentJSFView.getSelected() != null
            && !currentJSFView.getSelected().getSEDTasks().isEmpty()) {
      return currentJSFView.getSelected().getSEDTasks();

    }

    return Collections.emptyList();
  }

  public void executeSelectedRow() {
    if (currentJSFView != null) {
      String msg = currentJSFView.executeSelected();
      addCallbackParam(AbstractJSFView.CB_PARA_SUCCESS, true);
    } else {
      LOG.logWarn("Execute selected row, but no view currentJSFView is setted!",
              null);
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

  public String getTargetTable() {
    return updateTarget;
  }

  public void addCallbackParam(String val, boolean bval) {
    RequestContext.getCurrentInstance().addCallbackParam(val, bval);
  }

}
