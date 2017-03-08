/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.enums;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sluzba
 */
public enum SEDRulePredicate {
  EQUALS("=", "Equals", Arrays.asList(String.class, Integer.class,
          BigInteger.class, Float.class, Date.class)),
  NOT_EQUALS("!=", "Not equal", Arrays.asList(String.class, Integer.class,
          BigInteger.class, Float.class, Date.class)),
  GREATER_THAN(">", "Greater than", Arrays.asList(Integer.class,
          BigInteger.class, Float.class, Date.class)),
  GREATER_THAN_EQUALS(">=", "Greater than or equals", Arrays.asList(
          Integer.class, BigInteger.class, Float.class, Date.class)),
  LESS_THAN("<", "Less than", Arrays.asList(Integer.class, BigInteger.class,
          Float.class, Date.class)),
  LESS_THAN_EQUALS("<=", "Lest than or equals", Arrays.asList(Integer.class,
          BigInteger.class, Float.class, Date.class)),
  IN("In", "In value", Arrays.asList(String.class)),
  NOT_IN("NotIn", "Not in value", Arrays.asList(String.class)),
  IN_LIST("InList", "In list of values separated by ';'", Arrays.asList(
          String.class)),
  NOT_IN_LIST("NotInList", "Not in list of values separated by ';'", Arrays.
          asList(String.class)),
  CONTAINS("Contains", "Contains", Arrays.asList(String.class)),
  NOT_CONTAIN("NotContain", "Not contain", Arrays.asList(String.class)),
  STARTS_WITH("StartsWith", "Starts with", Arrays.asList(String.class)),
  ENDS_WITH("EndsWith", "Ends with", Arrays.asList(String.class));

  String mstrVal;
  String mstrDesc;
  List<Class> mTypeClasses;

  private SEDRulePredicate(String val, String strDesc, List<Class> tc) {
    mstrVal = val;
    mstrDesc = strDesc;
    mTypeClasses = tc;

  }

  /**
   *
   * @return
   */
  public String getValue() {

    return mstrVal;
  }

  /**
   *
   * @return
   */
  public String getDesc() {
    return mstrDesc;
  }

  public List<Class> getTypeClasses() {
    return mTypeClasses;
  }
  
  public static SEDRulePredicate getByValue(String value){
    for(SEDRulePredicate pr: values()){
      if (Objects.equals(pr.getValue(), value)){
        return pr;
      }
    }
    return null;
  
  }
  
}
