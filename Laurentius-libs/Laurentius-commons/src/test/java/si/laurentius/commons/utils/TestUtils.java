/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Calendar;


/**
 *
 * @author Jože Rihtaršič
 */
public class TestUtils {
  static public String readFileToString(File f, String charset)
          throws IOException {
    byte[] encoded = Files.readAllBytes(f.toPath());
    return new String(encoded, charset);
  }



  public TestUtils() {
  }

  protected File createFile(String data)
      throws IOException {

    return createFile(data.getBytes("UTF-8"));
  }

  protected File createFile(byte[] data)
      throws IOException {
    File f = File.createTempFile("hu-test", ".bin");

    try (final FileOutputStream fos = new FileOutputStream(f)) {
      fos.write(data);
    }

    return f;
  }


  protected File createFile(File parent, String content)
      throws IOException {
    File f = File.createTempFile("hu-test", ".bin", parent);

    try (final FileOutputStream fos = new FileOutputStream(f)) {
      if (content != null) {
        fos.write(content.getBytes("UTF-8"));
      } else {
        fos.write("Test data".getBytes("UTF-8"));
      }
    }

    return f;
  }

  protected File createEmptyFile()
      throws IOException {
    File f = File.createTempFile("hu-test", ".bin");
    return f;
  }

  public byte[] getTestByteArray()
      throws UnsupportedEncodingException {
    return ("testbuffer" + Calendar.getInstance().getTimeInMillis()).getBytes("utf-8");
  }

  

}
