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
package si.laurentius.commons.exception;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public enum SVEVReturnCodes {

  /**
     *
     */
  OK(1, "OK"),

  /**
     *
     */
  RESOURCE_READING_ERROR(2, "Resource reading error");
  int miVal;
  String mstrDesc;

  private SVEVReturnCodes(int iVal, String strDesc) {
    miVal = iVal;
    mstrDesc = strDesc;
  }

  /**
   *
   * @return
   */
  public int getValue() {
    return miVal;
  }

  /**
   *
   * @return
   */
  public String getDesc() {
    return mstrDesc;
  }

}
