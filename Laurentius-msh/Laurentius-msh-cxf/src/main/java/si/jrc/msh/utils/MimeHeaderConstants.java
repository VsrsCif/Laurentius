/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.utils;

/**
 *
 * @author sluzba
 */
public class MimeHeaderConstants {
  public static final String KEY_ID="id";
  public static final String KEY_CONTENT_DISPOSITION="Content-Disposition";
  public static final String KEY_CONTENT_ENCODING="Content-Transfer-Encoding";
  
  public static final String VAL_CONTENT_ENCODING_BASE64="base64";
  public static final String VAL_CONTENT_ENCODING_BINARY="binary";
  public static final String VAL_CONTENT_DISPOSITION_ATT_NAME="attachment; filename=%s";
}
