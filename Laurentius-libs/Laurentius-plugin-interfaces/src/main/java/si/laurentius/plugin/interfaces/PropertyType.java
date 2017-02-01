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
public enum PropertyType {
  String("string"),
  Integer("int"),
  List("list"),
  MultiList("list"),
  KeystoreKeys("keystoreKeys"),
  KeystoreAll("keystoreAll"),
  Boolean ("boolean"),
  Date ("date");
  
  
  
  String type;
  private PropertyType(String tp){
    type = tp;
  }

  public String getType() {
    return type;
  }

}
