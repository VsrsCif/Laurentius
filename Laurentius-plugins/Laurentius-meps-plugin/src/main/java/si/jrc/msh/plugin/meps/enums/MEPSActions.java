/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.meps.enums;

/**
 *
 * @author sluzba
 */
public enum MEPSActions {
  ADD_MAIL("AddMail"),
  REMOVE_MAIL("RemoveMail"),
  SERVICE_STATUS_NOTIFICATION("ServiceStatusNotification");
  
  
  String value;
  private MEPSActions(String act){
    value = act;
  }

  public String getValue() {
    return value;
  }
  
  
}
