package si.laurentius.lce.enc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.Reference;
import org.apache.xml.security.encryption.ReferenceList;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.KeyName;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.utils.xml.XMLUtils;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class SEDCrypto {

  private static final String ENC_SIMETRIC_KEY_ALG = XMLCipher.RSA_OAEP;

  static {
    org.apache.xml.security.Init.init();
  }

  /**
     *
     */
  public SEDCrypto() {}

  /**
   *
   * @param elKey
   * @param rsaKey
   * @param targetKeyAlg
   * @return
   * @throws SEDSecurityException
   */
  public Key decryptEncryptedKey(Element elKey, Key rsaKey, SymEncAlgorithms targetKeyAlg)
      throws SEDSecurityException {

    Key keyDec = null;

    XMLCipher keyCipher;
    try {
      keyCipher = XMLCipher.getInstance(ENC_SIMETRIC_KEY_ALG);
      keyCipher.init(XMLCipher.UNWRAP_MODE, null);
    } catch (XMLEncryptionException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm,
          ex, ENC_SIMETRIC_KEY_ALG);
    }

    // get cert
    X509Certificate xc;
    EncryptedKey key;
    try {
      key = keyCipher.loadEncryptedKey(elKey.getOwnerDocument(), elKey);
      xc = key.getKeyInfo().getX509Certificate();
    } catch (XMLEncryptionException | KeyResolverException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.EncryptionException, ex, ex.getMessage());
    }
    /*
     * Key rsaKey = CertificateUtils.getInstance().getPrivateKeyForX509Cert(xc); if (rsaKey ==
     * null){ throw new
     * SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.CertificateException,
     * "x.509 cert for decrypting Encrypted key not found"); }
     */

    XMLCipher chDec;
    try {
      chDec = XMLCipher.getInstance();
      chDec.init(XMLCipher.UNWRAP_MODE, rsaKey);
      // TODO change decrypting to enryptin key
      // key.getEncryptionMethod().getAlgorithm()
      keyDec = chDec.decryptKey(key, targetKeyAlg.getURI());
    } catch (XMLEncryptionException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.EncryptionException, ex, ex.getMessage());
    }

    return keyDec;

  }

  /**
   * Method encrypts input stream to output stream with given key
   *
   * @param fIn input stream to encrypt
   * @param fOut - output stream with encrypted data
   * @param skey - secret key to encrypt stream
   * @throws SEDSecurityException
   */
  public void decryptFile(File fIn, File fOut, Key skey) throws SEDSecurityException {
    try (FileInputStream fis = new FileInputStream(fIn);
        FileOutputStream fos = new FileOutputStream(fOut)) {
      encryptDecryptStream(fis, fos, skey, Cipher.DECRYPT_MODE);
    } catch (IOException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.ReadWriteFileException, ex, ex.getMessage());
    }
  }

  /**
   *
   * @param strKey
   * @param rsaKey
   * @param targetKeyAlg
   * @return
   * @throws SEDSecurityException
   */
  public Key decryptKey(String strKey, Key rsaKey, SymEncAlgorithms targetKeyAlg)
      throws SEDSecurityException {

    Key keyDec = null;
    Document doc;
    try {
      doc = XMLUtils.deserializeToDom(strKey);
    } catch (IOException | ParserConfigurationException | SAXException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.InvalidKey, ex,
          ex.getMessage());
    }
    return decryptKey(doc, rsaKey, targetKeyAlg);
  }
   public Key decryptKey(File fnKey, Key rsaKey, SymEncAlgorithms targetKeyAlg)
      throws SEDSecurityException {

    Key keyDec = null;
    Document doc;
    try {
      doc = XMLUtils.deserializeToDom(fnKey);
    } catch (IOException | ParserConfigurationException | SAXException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.InvalidKey, ex,
          ex.getMessage());
    }
    return decryptKey(doc, rsaKey, targetKeyAlg);
  }
   
  public Key decryptKey( Document docKey, Key rsaKey, SymEncAlgorithms targetKeyAlg)
      throws SEDSecurityException {

    Key keyDec = null;
   

    XMLCipher keyCipher;
    try {
      keyCipher = XMLCipher.getInstance(ENC_SIMETRIC_KEY_ALG);
      keyCipher.init(XMLCipher.UNWRAP_MODE, null);
    } catch (XMLEncryptionException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm,
          ex, ENC_SIMETRIC_KEY_ALG);
    }

    // get cert
    X509Certificate xc;
    EncryptedKey key;
    try {
      key = keyCipher.loadEncryptedKey(docKey, docKey.getDocumentElement());
      xc = key.getKeyInfo().getX509Certificate();
    } catch (XMLEncryptionException | KeyResolverException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.EncryptionException, ex, ex.getMessage());
    }
    /*
     * Key rsaKey = CertificateUtils.getInstance().getPrivateKeyForX509Cert(xc); if (rsaKey ==
     * null){ throw new
     * SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.CertificateException,
     * "x.509 cert for decrypting Encrypted key not found"); }
     */

    XMLCipher chDec;
    try {
      chDec = XMLCipher.getInstance();
      chDec.init(XMLCipher.UNWRAP_MODE, rsaKey);
      // TODO change decrypting to enryptin key
      // key.getEncryptionMethod().getAlgorithm()
      keyDec = chDec.decryptKey(key, targetKeyAlg.getURI());
    } catch (XMLEncryptionException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.EncryptionException, ex, ex.getMessage());
    }

    return keyDec;

  }

  /**
   * Method decrypts input stream to output stream with given key
   *
   * @param is input encrypted stream
   * @param os - output decrypted stream
   * @param skey - secret key to denrypt stream
   * @throws SEDSecurityException
   */
  public void decryptStream(InputStream is, OutputStream os, Key skey) throws SEDSecurityException {
    encryptDecryptStream(is, os, skey, Cipher.DECRYPT_MODE);
  }

  /**
   *
   * @param e
   * @return
   * @throws SEDSecurityException
   */
  public EncryptedKey file2SimetricEncryptedKey(File encKeyFile) throws SEDSecurityException {

    Document doc;
    try {
      doc = XMLUtils.deserializeToDom(encKeyFile);
    } catch (IOException | ParserConfigurationException | SAXException ex) {
       throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.XMLParseException,
          ex, ENC_SIMETRIC_KEY_ALG);
    }
    return element2SimetricEncryptedKey(doc.getDocumentElement());
    
  }
  
  public EncryptedKey element2SimetricEncryptedKey(Element e) throws SEDSecurityException {

    XMLCipher keyCipher;
    try {
      keyCipher = XMLCipher.getInstance(ENC_SIMETRIC_KEY_ALG);
      keyCipher.init(XMLCipher.UNWRAP_MODE, null);
    } catch (XMLEncryptionException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm,
          ex, ENC_SIMETRIC_KEY_ALG);
    }

    EncryptedKey key;
    try {
      key = keyCipher.loadEncryptedKey(e.getOwnerDocument(), e);
    } catch (XMLEncryptionException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.EncryptionException, ex, ex.getMessage());
    }
    return key;
  }

  /**
   * Method encrypt decrypts input stream to output stream with given key
   *
   * @param is input encrypted stream
   * @param os - output decrypted stream
   * @param skey - secret key to denrypt stream
   * @param chiperMode - chiper mode: Cipher.ENCRYPT_MODE, Cipher.DECRYPT_MODE
   * @throws SEDSecurityException
   */
  private void encryptDecryptStream(InputStream is, OutputStream os, Key skey, int chiperMode)
      throws SEDSecurityException {

    Cipher dcipher;
    try {
      dcipher = Cipher.getInstance(skey.getAlgorithm());
    } catch (NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm,
          ex, skey.getAlgorithm());
    } catch (NoSuchPaddingException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.NoSuchPadding,
          ex, skey.getAlgorithm(), ex.getMessage());
    }
    try {
      dcipher.init(chiperMode, skey);
    } catch (InvalidKeyException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.InvalidKey, ex,
          skey.getAlgorithm(), ex.getMessage());
    }

    try (CipherOutputStream cos = new CipherOutputStream(os, dcipher)) {

      byte[] block = new byte[1024];
      int i;
      while ((i = is.read(block)) != -1) {
        cos.write(block, 0, i);
      }
    } catch (IOException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.ReadWriteFileException, ex, ex.getMessage());
    }
  }

  /**
   * Method encrypts input stream to output stream with given key
   *
   * @param fIn input stream to encrypt
   * @param fOut - output stream with encrypted data
   * @param skey - secret key to encrypt stream
   * @throws SEDSecurityException
   */
  public void encryptFile(File fIn, File fOut, Key skey) throws SEDSecurityException {
    try (FileInputStream fis = new FileInputStream(fIn);
        FileOutputStream fos = new FileOutputStream(fOut)) {
      encryptDecryptStream(fis, fos, skey, Cipher.ENCRYPT_MODE);
    } catch (IOException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.ReadWriteFileException, ex, ex.getMessage());
    }
  }

  /**
   * Method generates ecrypts symmetric key with x.509 certs and returs W3c EncryptedKey as string
   *
   * @param key
   * @param rsaCert
   * @param recipient
   * @param keyId
   * @return gW3c EncryptedKey as string
   * @throws SEDSecurityException
   *
   */
  public String encryptKeyWithReceiverPublicKey(Key key, X509Certificate rsaCert, String recipient,
      String keyId) throws SEDSecurityException {

    // create document factory
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder db;
    try {
      db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.EncryptionException, ex, ex.getMessage());
    }
    Document document = db.newDocument();

    XMLCipher keyCipher;
    try {

      keyCipher = XMLCipher.getInstance(ENC_SIMETRIC_KEY_ALG);
    } catch (XMLEncryptionException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm,
          ex, ENC_SIMETRIC_KEY_ALG);
    }
    try {
      keyCipher.init(XMLCipher.WRAP_MODE, rsaCert.getPublicKey());
    } catch (XMLEncryptionException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.EncryptionException, ex, ex.getMessage());
    }

    EncryptedKey encryptedKey;
    try {
      // create EncryptedKey
      encryptedKey = keyCipher.encryptKey(document, key);
      encryptedKey.setCarriedName(rsaCert.getSubjectDN().toString());
      encryptedKey.setRecipient(recipient);
      // add reference list
      ReferenceList dataRefList = keyCipher.createReferenceList(ReferenceList.DATA_REFERENCE);
      Reference dataRef1 = dataRefList.newDataReference(keyId);
      dataRefList.add(dataRef1);
      encryptedKey.setReferenceList(dataRefList);
      // add cert data
      org.apache.xml.security.keys.KeyInfo keyInfo2 =
          new org.apache.xml.security.keys.KeyInfo(document);
      KeyName kn = new KeyName(document, rsaCert.getSubjectDN().toString());
      keyInfo2.add(kn);
      X509Data x509Data = new X509Data(document);
      x509Data.addCertificate(rsaCert);
      keyInfo2.add(x509Data);
      encryptedKey.setKeyInfo(keyInfo2);

    } catch (XMLSecurityException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.EncryptionException, ex, ex.getMessage());
    }

    /*
     * Encrypt the contents of the document element.
     */
    return XMLUtils.serializeToString(keyCipher.martial(encryptedKey), false);

  }

  /**
   * Method encrypts input stream to output stream with given key
   *
   * @param is input stream to encrypt
   * @param os - output stream with encrypted data
   * @param skey - secret key to encrypt stream
   * @throws SEDSecurityException
   */
  public void encryptStream(InputStream is, OutputStream os, Key skey) throws SEDSecurityException {
    encryptDecryptStream(is, os, skey, Cipher.ENCRYPT_MODE);

  }

  /**
   *
   * @param key
   * @param rsaCert
   * @param recipient
   * @param keyId
   * @return
   * @throws SEDSecurityException
   */
  public Element encryptedKeyWithReceiverPublicKey(Key key, X509Certificate rsaCert,
      String recipient, String keyId) throws SEDSecurityException {

    // create document factory
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder db;
    try {
      db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.EncryptionException, ex, ex.getMessage());
    }
    Document document = db.newDocument();

    XMLCipher keyCipher;
    try {

      keyCipher = XMLCipher.getInstance(ENC_SIMETRIC_KEY_ALG);
    } catch (XMLEncryptionException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm,
          ex, ENC_SIMETRIC_KEY_ALG);
    }
    try {
      keyCipher.init(XMLCipher.WRAP_MODE, rsaCert.getPublicKey());
    } catch (XMLEncryptionException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.EncryptionException, ex, ex.getMessage());
    }

    EncryptedKey encryptedKey;
    try {
      // create EncryptedKey
      encryptedKey = keyCipher.encryptKey(document, key);
      encryptedKey.setCarriedName(rsaCert.getSubjectDN().toString());
      encryptedKey.setRecipient(recipient);
      // add reference list
      ReferenceList dataRefList = keyCipher.createReferenceList(ReferenceList.DATA_REFERENCE);
      Reference dataRef1 = dataRefList.newDataReference(keyId);
      dataRefList.add(dataRef1);
      encryptedKey.setReferenceList(dataRefList);
      // add cert data
      org.apache.xml.security.keys.KeyInfo keyInfo2 =
          new org.apache.xml.security.keys.KeyInfo(document);
      KeyName kn = new KeyName(document, rsaCert.getSubjectDN().toString());
      keyInfo2.add(kn);
      X509Data x509Data = new X509Data(document);
      x509Data.addCertificate(rsaCert);
      keyInfo2.add(x509Data);
      encryptedKey.setKeyInfo(keyInfo2);

    } catch (XMLSecurityException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.EncryptionException, ex, ex.getMessage());
    }

    /*
     * Encrypt the contents of the document element.
     */
    return keyCipher.martial(encryptedKey);

  }

  /**
   *
   * @param in
   * @return
   * @throws SEDSecurityException
   */
  public X509Certificate getCertificate(InputStream in) throws SEDSecurityException {
    X509Certificate cert;
    try {
      CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      cert = (X509Certificate) certFactory.generateCertificate(in);
    } catch (CertificateException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CertificateException, ex, ex.getMessage());
    }
    return cert;

  }

  /**
   * Metod generates crypto key
   *
   * @param ag key algoritem
   * @return generated secret key
   * @throws SEDSecurityException
   */
  public SecretKey getKey(SymEncAlgorithms ag) throws SEDSecurityException {
    KeyGenerator kgen;
    try {
      kgen = KeyGenerator.getInstance(ag.getJCEName());
    } catch (NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm,
          ex, ag.getJCEName());
    }
    if (ag.getKeyLength() != -1) {
      kgen.init(ag.getKeyLength());
    }
    return kgen.generateKey();
  }

  /**
     *
     */
  public enum SymEncAlgorithms {

    /**
         *
         */
    AES128_CBC("http://www.w3.org/2001/04/xmlenc#aes128-cbc", "AES", 128),

    /**
         *
         */
    AES192_CBC("http://www.w3.org/2001/04/xmlenc#aes192-cbc", "AES", 192),

    /**
         *
         */
    AES256_CBC("http://www.w3.org/2001/04/xmlenc#aes256-cbc", "AES", 256);

    private final String mstrW3_uri;
    private final String mstrJce_name;
    private final int miKey_len;

    private SymEncAlgorithms(String uri, String name, int iKeyLen) {
      this.mstrW3_uri = uri;
      this.mstrJce_name = name;
      this.miKey_len = iKeyLen;
    }

    /**
     *
     * @return
     */
    public String getJCEName() {
      return mstrJce_name;
    }

    /**
     *
     * @return
     */
    public String getURI() {
      return mstrW3_uri;
    }

    /**
     *
     * @return
     */
    public int getKeyLength() {
      return miKey_len;
    }

  }

}
