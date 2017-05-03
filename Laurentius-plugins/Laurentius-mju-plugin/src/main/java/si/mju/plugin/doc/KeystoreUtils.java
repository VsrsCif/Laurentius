/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.mju.plugin.doc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.plugin.interfaces.exception.InMailProcessException;

/**
 *
 * @author sluzba
 */
public class KeystoreUtils {
    public KeyStore getKeyStore(String fileName, String type, String passwd) throws InMailProcessException {

    File fKS = new File(StringFormater.replaceProperties(fileName));
    if (!fKS.exists()) {
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.InitException,
              String.format("Keystore %s do not exist!", fileName));

    }

    KeyStore keyStore = null;
    try (FileInputStream fis = new FileInputStream(fKS)) {

      keyStore = KeyStore.getInstance(type);

      keyStore.load(fis, passwd.toCharArray());
    } catch (KeyStoreException ex) {

      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.InitException,
              String.format("Keystore  %s access exception %s!", fileName, ex.
                      getCause()), ex);
    } catch (NoSuchAlgorithmException ex) {
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.InitException,
              String.format("Keystore  %s NoSuchAlgorithm Exception  %s!",
                      fileName, ex.getCause()), ex);
    } catch (CertificateException ex) {
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.InitException,
              String.format("Keystore  %s Certificate Exception  %s!", fileName,
                      ex.getCause()), ex);

    } catch (IOException ex) {
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.InitException,
              String.format("Keystore  %s IO Exception  %s!", fileName, ex.
                      getCause()), ex);
    }
    return keyStore;
  }

  public KeyStore.PrivateKeyEntry getPrivateKeyEntryForAlias(KeyStore ks,
          String alias,
          String passwd)
          throws InMailProcessException {
    KeyStore.PrivateKeyEntry rsaKey;
    try {
      rsaKey
              = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
                      new KeyStore.PasswordProtection(passwd.toCharArray()));

    } catch (KeyStoreException | NoSuchAlgorithmException
            | UnrecoverableEntryException ex) {
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.InitException,
              String.format(
                      "Error %s occured while retrieving key %s IO Exception  %s!",
                      ex.getClass().getName(),
                      alias, ex.getMessage()), ex);
    }
    return rsaKey;
  }
}
