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
package si.jrc.msh.plugin.zpp.enums;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public enum ZPPPartPropertyType {

  /**
   *
   */
  RefPartEbmsId("http://www.sodisce.si/ZPP/RefPartEbmsId"),
  RefPartDigestSHA256("http://www.sodisce.si/ZPP/RefPartDigestSHA256"),
  RefPartMimeType("http://www.sodisce.si/ZPP/RefPartMimeType"),
  RefPartName("http://www.sodisce.si/ZPP/RefPartName"),
  RefPartDesc("http://www.sodisce.si/ZPP/RefPartDesc"),
  RefPartType("http://www.sodisce.si/ZPP/RefPartType"),
  PartCreated("http://www.sodisce.si/ZPP/PartCreated"),
  
  
  PartKeyAlg("http://www.sodisce.si/ZPP/KeyAlgorithm"),
  PartKeyFormat("http://www.sodisce.si/ZPP/Keyformat"),
  PartKeyValue("http://www.sodisce.si/ZPP/KeyValue"),
  PartKeySize("http://www.sodisce.si/ZPP/KeySize"),
  
  ;
 



  String mstrType;


  private ZPPPartPropertyType(String val) {
    mstrType = val;
   
  }

  public String getType() {
    return mstrType;
  }


 
}
