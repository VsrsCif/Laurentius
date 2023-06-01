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

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public enum ZKPPartType {

  /**
   *
   */
  DeliveryNotification("http://www.sodisce.si/ZKP/DeliveryNotification", "ObvestiloOPosiljki", "Obvestilo o prispeli pošiljki", "application/pdf", "pdf"),
  DeliveryReciept("http://www.sodisce.si/ZKP/DeliveryReciept", "Potrdilo o vročitvi", "Potrdilo o vročitvi v varni ePredal", "application/pdf", "pdf"),
  AdviceOfDelivery("http://www.sodisce.si/ZKP/AdviceOfDelivery", "Vrocilnica", "Vročilnica", "application/pdf", "pdf"),
  FictionNotification("http://www.sodisce.si/ZKP/FictionNotification", "ObvestiloOFikciji", "Obvestilo naslovniku o fiktivni vročitvi", "application/pdf", "pdf"),
  AdviceOfDeliveryFiction("http://www.sodisce.si/ZKP/AdviceOfDeliveryFiction", "FiktivnaVrocitev", "Obvestilo pošiljatelju o fiktivni vročitvi", "application/pdf", "pdf"),  
  EncryptedPart("http://www.sodisce.si/ZKP/EncryptedPart", "SifriranaPosiljka", "Sifriran dok.: %s", "application/octet-stream", "zkp.enc"),
  LocalEncryptionKey("http://www.sodisce.si/ZKP/LocalEncryptionKey", "LocalEncryptionKey", "Kljuc za sifriranje priponk", "application/xml", "xml"),
  EncryptedKey("http://www.sodisce.si/ZKP/EncryptedKey", "EncryptedKey", "Kljuc za desifriranje priponk", "application/xml", "xml"),
  RecieptAddressNotExists("http://www.sodisce.si/ZKP/RecieptAddressNotExists", "RecieptAddressNotExists", "Naslovnik ne obstaja", "application/pdf", "pdf"),
  
    ;
 
  


  String mstrPartType;
  String mstrPartName;
  String mstrMimeType;
  String mstrFileSuffix;
  String mstrDescriptionFormat;

  private ZKPPartType(String partType, String partName, String partDescFormat, String partMimetype, String partFileSuffix) {
    mstrPartType = partType;
    mstrPartName = partName;
    mstrDescriptionFormat = partDescFormat;
    mstrMimeType = partMimetype;
    mstrFileSuffix = partFileSuffix;
    
  }

  public String getPartType() {
    return mstrPartType;
  }

  public String getPartName() {
    return mstrPartName;
  }

  public String getMimeType() {
    return mstrMimeType;
  }

  public String getFileSuffix() {
    return mstrFileSuffix;
  }

  public String getDescriptionFormat() {
    return mstrDescriptionFormat;
  }
  
  public String getDescription(String  arg) {
    return String.format(mstrDescriptionFormat, arg);
  }

 
}
