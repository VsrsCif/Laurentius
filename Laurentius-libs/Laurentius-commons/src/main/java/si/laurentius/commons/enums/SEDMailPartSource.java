/*
 * Copyright 2017, Supreme Court Republic of Slovenia
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


/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public enum SEDMailPartSource {

  /**
     *
     */
  MAIL("mail", "Inital user mail payload part"),

  /**
     *
     */
  EBMS("ebms", "Soap header ebms signals, as4 recipet. etc"),

  /**
     *
     */
  PLUGIN("plugin", "Payload part is created by plugin (Usually is given plugintype)");

 
  
 
  String mstrVal;
  String mstrDesc;
 

  private SEDMailPartSource(String val, String strDesc) {
    mstrVal = val;
    mstrDesc = strDesc;
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

}
