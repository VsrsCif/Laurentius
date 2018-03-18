/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.vsrs.cif.filing.enums;

/**
 *
 * @author sluzba
 */
public enum ECFAction {
  ServeFiling("ServeFiling");
  
  
  String value;
  private ECFAction(String act){
    value = act;
  }

  public String getValue() {
    return value;
  }
  
  
}
