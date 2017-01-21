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
package si.laurentius.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.plg.db.IMPDBInterface;
import si.laurentius.plugin.imp.IMPXslt;
import si.laurentius.plugin.interfaces.InMailProcessorInterface;

import si.laurentius.plugin.processor.InMailProcessorDef;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(InMailProcessorInterface.class)
public class ProcessXSLT implements InMailProcessorInterface {

  @EJB
  private IMPDBInterface mDB;

  @Override
  public boolean proccess(String instance, MSHInMail mi,
          Map<String, Object> map) {
    return false;
  }

  @Override
  public List<String> getInstanceIds() {
    List<String> lst = new ArrayList<>();
    for (IMPXslt im : mDB.getXSLTs()) {
      lst.add(im.getInstance());
    }
    return lst;
  }

  @Override
  public InMailProcessorDef getDefinition() {
    InMailProcessorDef impd = new InMailProcessorDef();
    impd.setType("xslt");
    impd.setName("XSLT processor");
    impd.setDescription("EXSLT transform processor");

    return impd;

  }

}
