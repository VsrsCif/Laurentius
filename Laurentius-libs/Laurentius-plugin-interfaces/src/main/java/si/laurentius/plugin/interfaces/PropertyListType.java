/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.plugin.interfaces;

/**
 *
 * @author sluzba
 */
public enum PropertyListType {
  InMailStatus("InMailStatus"),
  OutMailStatus("OutMailStatus"),
  KeystoreCertAll("KeystoreCertAll"),
  KeystoreCertKeys("KeystoreCertKeys"),
  LocalBoxes("LocalBoxes");
  
  String type;
  private PropertyListType(String tp){
    type = tp;
  }

  public String getType() {
    return type;
  }

}
