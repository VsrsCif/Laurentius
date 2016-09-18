/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.pmode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.msh.web.gui.entities.BTAction;

/**
 *
 * @author Jože Rihtaršič
 */
@ManagedBean(name = "businessTransaction")
@SessionScoped
public class BusinessTransaction implements Serializable {

  List<BTAction> mlst = new ArrayList<>();

  private BTAction selectedAction;

  private boolean suspendEvent;

  /**
     *
     */
  public void addAction() {

  }

  /**
   *
   * @return
   */
  public List<BTAction> getActions() {
    return mlst;
  }

  /**
   *
   * @return
   */
  public BTAction getSelectedAction() {
    return selectedAction;
  }

  /**
     *
     */
  @PostConstruct
  public void init() {
    mlst.add(new BTAction("DeliveryNotify", "sq-right", "sq-left-arrow"));
    mlst.add(new BTAction("AdviceOfDelivery", "sq-right-arrow", "sq-left"));
    mlst.add(new BTAction("DeliveryFiction", "sq-right", "sq-left-arrow"));
    mlst.add(new BTAction("Test - 1", "sq-right", "sq-left-arrow"));
    mlst.add(new BTAction("Test - 2", "sq-right-arrow", "sq-left"));
  }

  /**
   *
   * @param selectedAction
   */
  public void setSelectedAction(BTAction selectedAction) {
    this.selectedAction = selectedAction;
  }

}
