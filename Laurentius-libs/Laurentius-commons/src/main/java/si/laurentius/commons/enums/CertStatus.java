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
 * @author Jože Rihtaršič
 */
public enum CertStatus {
  OK(0),  
  MISSING_PASSWD(1),
  INVALID_PASSWD(2),
  INVALID_BY_DATE(4),
  INVALID_BY_ROOTCA(8),
  CRL_REVOKED(16),
  CRL_NOT_CHECKED(32);
  
  
  
  int miCode;
  CertStatus(int iCode){
    miCode = iCode;
    
  }

 public int getCode(){
   return miCode;
 }
 
 public Integer addCode(Integer iSt){
    if(iSt == null){
     return miCode;
   }
   return iSt | miCode;
 }
 
 public boolean hasCode(Integer iSt){
    if(iSt == null){
     return false;
   }
   return (iSt & miCode) !=0;
 }
 
 public Integer removeCode(Integer iSt){
   if(iSt == null){
     return 0;
   }   
   return iSt & ~miCode;
 }
 
 public boolean containsCode(Integer iSt){
   if(iSt == null){
     return false;
   }
   return (iSt & miCode) != 0;
 }
  
}
