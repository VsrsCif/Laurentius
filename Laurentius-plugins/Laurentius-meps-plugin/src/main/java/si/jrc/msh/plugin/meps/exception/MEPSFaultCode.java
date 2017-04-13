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
package si.jrc.msh.plugin.meps.exception;

/**
 *
 * @author Jože Rihtaršič
 */
public enum MEPSFaultCode {

  
  /**
     *
     */
  Other("MEPS:0004", "Other", "failure", "Content", "Other error", "ebms");

  /**
     *
     */
 
  
  
  


  String code;
  String name;
  String severity;
  String category;
  String description;
  String origin;

  MEPSFaultCode(String cd, String nm, String sv, String ct, String desc, String org) {
    code = cd;
    name = nm;
    severity = sv;
    category = ct;
    description = desc;
    origin = org;
  }

  /**
   *
   * @return
   */
  public String getCode() {
    return code;
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
   * @return
   */
  public String getSeverity() {
    return severity;
  }

  /**
   *
   * @return
   */
  public String getCategory() {
    return category;
  }

  /**
   *
   * @return
   */
  public String getDescription() {
    return description;
  }

  /**
   *
   * @return
   */
  public String getOrigin() {
    return origin;
  }

}
