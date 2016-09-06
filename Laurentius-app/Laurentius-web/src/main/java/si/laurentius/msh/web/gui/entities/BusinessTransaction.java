/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.gui.entities;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sluzba
 */
public class BusinessTransaction {

  String description;
  String initiatorRole;
  List<BTAction> mlstActionList = new ArrayList<>();
  String name;
  String responderRole;

  /**
   *
   * @param name
   * @param initiatorRole
   * @param responderRole
   */
  public BusinessTransaction(String name, String initiatorRole, String responderRole) {
    this.name = name;
    this.initiatorRole = initiatorRole;
    this.responderRole = responderRole;
  }

  /**
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

}
