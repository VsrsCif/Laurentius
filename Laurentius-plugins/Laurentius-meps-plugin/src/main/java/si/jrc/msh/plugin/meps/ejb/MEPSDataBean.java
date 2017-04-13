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
package si.jrc.msh.plugin.meps.ejb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.bind.JAXBException;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.plugin.meps.MEPSData;
import si.laurentius.plugin.meps.PartyType;
import si.laurentius.plugin.meps.PhysicalAddressType;
import si.laurentius.plugin.meps.ServiceType;

/**
 * @author Jože Rihtaršič
 */
@Startup
@Singleton
@Local(MEPSDataInterface.class)
public class MEPSDataBean implements MEPSDataInterface {

  public static final String FILE_INIT_DATA = "meps-data.xml";
  public static final String ROOT_FOLDER = "/meps/";
  public static final String BLOB_FOLDER = ROOT_FOLDER + "/test-pdf/";

  private static final SEDLogger LOG = new SEDLogger(MEPSDataBean.class);
  MEPSData mepsData = null;
  long mFileLastModifiedDate = 0;

  File pluginRootFolder = null;

  @PostConstruct
  void initialize() {
    pluginRootFolder = new File(SEDSystemProperties.getPluginsFolder(),
            ROOT_FOLDER);
    if (!pluginRootFolder.exists()) {
      initFolder(pluginRootFolder);
    }

    reload();

  }

  @Override
  public List<ServiceType> getServices() {
    
    return mepsData != null && mepsData.getServices()!=null ? mepsData.getServices().getServices() : Collections.emptyList();
  }

  @Override
  public List<PhysicalAddressType> getAddresses() {
    
    return mepsData != null && mepsData.getAddresses()!= null? mepsData.getAddresses().getAddresses() : Collections.emptyList();
  }

  @Override
  public PhysicalAddressType getSenderAddress() {
    return mepsData != null && mepsData.getParty()!=null?mepsData.getParty().getSenderAddress() : null;
  }

  
  @Override
  public PartyType getParty() {
    return mepsData != null ?mepsData.getParty() : null;
  }
  
  public void reload() {

    long l = LOG.logStart();
    File dataFile = new File(pluginRootFolder, FILE_INIT_DATA);
    if (dataFile.lastModified() > mFileLastModifiedDate) {
      try (FileInputStream fis = new FileInputStream(dataFile)) {
        reload(fis);
        mFileLastModifiedDate = dataFile.lastModified();
      } catch (IOException ex) {
        String msg = "Error init PModes from file '" + dataFile.
                getAbsolutePath() + "'";
        throw new RuntimeException(msg, ex);
      }
    }
    LOG.logEnd(l); 
  }

  public final void reload(InputStream is) {
    long l = LOG.logStart();
   
    
    try {
      mepsData = ( MEPSData) XMLUtils.deserialize(is, MEPSData.class);
    } catch (JAXBException ex) {
      String msg = "Error init MSH Settings!";
      throw new RuntimeException(msg, ex);
    }
    LOG.logEnd(l);
  }

  private void initFolder(File f) {
    if (!f.exists()) {
      f.mkdirs();
    }
    String source = "/init";
   // copyFromJar("/init", f);
  }

  
 
}
