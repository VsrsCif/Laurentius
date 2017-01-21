/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.test;

import si.jrc.msh.interceptor.EBMSInInterceptorTest;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;

/**
 *
 * @author Jože Rihtaršič
 */
public class SEDTestCertBean implements SEDCertStoreInterface {

  SEDCertStore mCertStore;
  KeystoreUtils mku = new KeystoreUtils();
  public static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
      EBMSInInterceptorTest.class);

  public SEDTestCertBean(SEDCertStore cs) {
    try {
      mCertStore = cs;
      mku.refreshCertStore(mCertStore);
    } catch (SEDSecurityException ex) {
      LOG.error(ex.getMessage(), ex);
    }
  }

  @Override
  public SEDCertStore getCertificateStore()
      throws SEDSecurityException {

    return mCertStore;
  }

  @Override
  public SEDCertStore getRootCACertificateStore()
      throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void refreshCrlLists() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
