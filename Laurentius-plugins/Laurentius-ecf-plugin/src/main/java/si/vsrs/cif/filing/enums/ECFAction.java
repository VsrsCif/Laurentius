/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.vsrs.cif.filing.enums;

/**
 * eCourt eFiling Action names as described in document:
 *  - Funkcionalne in tehnične zahteve za e-Vlaganje v varni elektronski predal sodišča 1.0. Date: 5.05.2022
 *
 * @author Jože Rihtaršič
 * @since 2.0
 */
public enum ECFAction {
  ServeFiling("ServeFiling");
  
  
  String value;
  ECFAction(String act){
    value = act;
  }

  public String getValue() {
    return value;
  }
  
  
}
