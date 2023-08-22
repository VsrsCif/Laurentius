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
package si.vsrs.cif.laurentius.plugin.zkp.enums;

public enum ZKPPartPropertyType {

  /**
   *
   */
  RefPartEbmsId("http://www.sodisce.si/ZKP/RefPartEbmsId"),
  RefPartDigestSHA256("http://www.sodisce.si/ZKP/RefPartDigestSHA256"),
  RefPartMimeType("http://www.sodisce.si/ZKP/RefPartMimeType"),
  RefPartName("http://www.sodisce.si/ZKP/RefPartName"),
  RefPartDesc("http://www.sodisce.si/ZKP/RefPartDesc"),
  RefPartType("http://www.sodisce.si/ZKP/RefPartType"),
  PartCreated("http://www.sodisce.si/ZKP/PartCreated"),
  
  
  PartKeyAlg("http://www.sodisce.si/ZKP/KeyAlgorithm"),
  PartKeyFormat("http://www.sodisce.si/ZKP/Keyformat"),
  PartKeyValue("http://www.sodisce.si/ZKP/KeyValue"),
  PartKeySize("http://www.sodisce.si/ZKP/KeySize"),
  
  ;
 



  String mstrType;


  private ZKPPartPropertyType(String val) {
    mstrType = val;
   
  }

  public String getType() {
    return mstrType;
  }


 
}
