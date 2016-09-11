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
package si.laurentius.commons.exception;

import static java.lang.String.format;

/**
 * SEDSecurityException message for class KeyUtils.
 *
 * @author Jože Rihtaršič
 */
public class SEDSecurityException extends Exception {

  String[] messageParams;
  SEDSecurityExceptionCode mshErrorCode;

  /**
   * Constructor
   * 
   * @param ec - exception code with no parameters for exception description
   */
  public SEDSecurityException(SEDSecurityExceptionCode ec) {
    mshErrorCode = ec;

  }

  /**
   * Constructor
   * 
   * @param ec - exception code
   * @param params parameteres for exception description
   */
  public SEDSecurityException(SEDSecurityExceptionCode ec, String... params) {
    super(ec.getName());
    mshErrorCode = ec;

    messageParams = params;

  }

  /**
   * Constructor
   * 
   * @param ec - exception code
   * @param cause - excetion cause
   * @param params - parameteres for exception description
   */
  public SEDSecurityException(SEDSecurityExceptionCode ec, Throwable cause, String... params) {
    super(ec.getName(), cause);
    mshErrorCode = ec;
    messageParams = params;
  }

  /**
   * Constructor
   * 
   * @param ec - exception code with parameteres for exception description
   * @param cause - excetion cause
   */
  public SEDSecurityException(SEDSecurityExceptionCode ec, Throwable cause) {
    super(ec.getName(), cause);
    mshErrorCode = ec;
  }

  /**
   * getter for error code
   * 
   * @return current excetion code
   */
  public SEDSecurityExceptionCode getMSHErrorCode() {
    return mshErrorCode;
  }

  /**
   * getter for formated error message
   * 
   * @return string message
   */

  @Override
  public String getMessage() {
    if (messageParams == null) {
      messageParams = new String[mshErrorCode.getDescParamCount()];
    }

    if (messageParams.length != mshErrorCode.getDescParamCount()) {
      String[] newMP = new String[mshErrorCode.getDescParamCount()];
      for (int i = 0; i < newMP.length; i++) {
        newMP[i] = i < messageParams.length ? messageParams[i] : "";
      }
      messageParams = newMP;

    }
    return format(mshErrorCode.getDescriptionFormat(), (Object[]) messageParams);
  }

  /**
     *
     */
  public enum SEDSecurityExceptionCode {

    /**
         *
         */
    NoSuchAlgorithm("SEC:0001", "NoSuchAlgorithm", "No such algorithm: %s, msg: %s", 2),

    /**
         *
         */
    NoSuchPadding("SEC:0002", "NoSuchPadding", "No such padding: %s, msg: %s", 2),

    /**
         *
         */
    InvalidKey("SEC:0003", "InvalidKey", "Invalid key: %s, msg: %s", 2),

    /**
         *
         */
    EncryptionException("SEC:0004", "EncryptionError", "Encryption error: %s", 1),

    /**
         *
         */
    PasswordFileError("SEC:0005", "PasswordFileError", "Security error: %s", 1),

    /**
         *
         */
    ReadWriteFileException("SEC:0006", "ReadWriteFileException", "Read write file exception: %s", 1),

    /**
         *
         */
    KeyStoreException("SEC:0007", "KeyStoreException", "Key store exception %s", 1),

    /**
         *
         */
    CertificateException("SEC:0008", "CertificateException", "Certificate exception %s", 1),

    /**
         *
         */
    InitializeException("SEC:0009", "InitializeException", "Initialize exception %s", 1),

    /**
         *
         */
    CreateSignatureException("SEC:0010", "CreateSignatureException",
        "Create Signature exception %s", 1),

    /**
         *
         */
    CreateTimestampException("SEC:0011", "CreateTimestampException",
        "Create Timestamp exception %s", 1),

    /**
         *
         */
    XMLParseException("SEC:0012", "XMLParseException", "XMLParse exception %s", 1),

    /**
         *
         */
    SignatureNotFound("SEC:0013", "SignatureNotFound", "Signature Not Found exception %s", 1),

    /**
         *
         */
    KeyForAliasNotExists("SEC:0014", "KeyForAliasNotExists", "Key for alias %s not found!", 1), ;
    ;

    String code;
    String name;
    String description;
    int paramCount;

    SEDSecurityExceptionCode(String cd, String nm, String desc, int pc) {
      code = cd;
      name = nm;
      description = desc;
      paramCount = pc;
    }

    /**
     *
     * @return
     */
    public String getCode() {
      return code;
    }

    /**
     *
     * @return
     */
    public String getName() {
      return name;
    }

    /**
     *
     * @return
     */
    public String getDescriptionFormat() {
      return description;
    }

    /**
     *
     * @return
     */
    public int getDescParamCount() {
      return paramCount;
    }
  }

}
