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
package si.vsrs.cif.filing.enums;

import java.util.Objects;

/**
 *
 * @author Jože Rihtaršič
 */
public enum ECFService {
  
  CourtFiling("CourtFiling", "http://laurentius.si/ecf" );

  String service;
  String namespace;
  

  ECFService(String service, String namespace){
    this.service = service;
    this.namespace = namespace; 
  }

  public String getService() {
    return service;
  }

  public String getNamespace() {
    return namespace;
  }

  public static ECFService getValueByService(String service){
    for (ECFService ms: values()){
      if (Objects.equals(ms.getService(),service) ){
        return ms;
      }
    }
    return null;
  
  }
  
  public static String[] serviceNames(){
    ECFService[] v = values();
    String[] srvs = new String[v.length];
    for (int i =0; i< v.length; i++){
      srvs[i] = v[i].getService();
    }
    return srvs;
  
  }

  
  
}
