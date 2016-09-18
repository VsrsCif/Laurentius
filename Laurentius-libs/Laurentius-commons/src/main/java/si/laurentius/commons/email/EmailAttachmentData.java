/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.commons.email;

import java.io.File;

/**
 *
 * @author Jože Rihtaršič
 */
public class EmailAttachmentData {

  File mFile;

  String mstrFileName;
  String mstrMimeType;

  /**
   *
   * @param mstrFileName
   * @param mstrMimeType
   * @param mFile
   */
  public EmailAttachmentData(String mstrFileName, String mstrMimeType, File mFile) {
    this.mstrFileName = mstrFileName;
    this.mstrMimeType = mstrMimeType;
    this.mFile = mFile;
  }

  /**
   *
   * @return
   */
  public File getFile() {
    return mFile;
  }

  /**
   *
   * @return
   */
  public String getFileName() {
    return mstrFileName;
  }

  /**
   *
   * @return
   */
  public String getMimeType() {
    return mstrMimeType;
  }

  /**
   *
   * @param mFile
   */
  public void setFile(File mFile) {
    this.mFile = mFile;
  }

  /**
   *
   * @param mstrFileName
   */
  public void setFileName(String mstrFileName) {
    this.mstrFileName = mstrFileName;
  }

  /**
   *
   * @param mstrMimeType
   */
  public void setMimeType(String mstrMimeType) {
    this.mstrMimeType = mstrMimeType;
  }

}
