/*
 * Copyright 2016, Supreme Court Republic of Slovenia
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
package si.laurentius.plugin.interfaces;

import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import si.laurentius.msh.inbox.mail.MSHInMail;


/**
 *
 * @author Jože Rihtaršič
 */
@Local
public interface  InMailProcessorInterface extends PluginComponentInterface  {
   /**
    * 
    * @param instance
    * @param mi
    * @param map
    * @return 
    */
  public boolean proccess(String instance, MSHInMail mi, Map<String, Object> map);
  
  List<String> getInstanceIds();
  
  
}
