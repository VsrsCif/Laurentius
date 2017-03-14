/*
 * Copyright 2015, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.commons.enums;

import java.util.ArrayList;
import java.util.List;
import static si.laurentius.commons.enums.SEDInboxMailStatus.values;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public enum SEDTaskStatus {

  /**
     *
     */
  INIT("INIT", "Task initialize.", "orange"),

  /**
     *
     */
  PROGRESS("PROGRESS", "Task is in progress.", "gray"),

  /**
     *
     */
  SUCCESS("SUCCESS", "Task ended successfully", "blue"),

  /**
     *
     */
  ERROR("ERROR", "Error occured ", "red");
  
  private static final List<String> listOfValues;

  static {
    listOfValues = new ArrayList<>();
    for (SEDTaskStatus enmVal : values()) {
      listOfValues.add(enmVal.getValue());
    }
  }

  public static List<String> listOfValues() {
    return listOfValues;
  }

  String mstrVal;
  String mstrDesc;
  String mstrColor;

  private SEDTaskStatus(String val, String strDesc, String strColor) {
    mstrVal = val;
    mstrDesc = strDesc;
    mstrColor = strColor;
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

  /**
   *
   * @return
   */
  public String getColor() {
    return mstrColor;
  }

  /**
   *
   * @param strName
   * @return
   */
  public static String getColor(String strName) {

    for (SEDTaskStatus st : values()) {
      if (st.getValue().equals(strName)) {
        return st.getColor();
      }
    }
    return strName;
  }
}
