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
package si.laurentius.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class HashUtilsTest  {
  
  TestUtils tuUtils = new TestUtils();

  /**
   * Test of getMD5Hash method, of class HashUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGetMD5Hash_File()
      throws Exception {
    File f =  tuUtils.createFile("first file");
    File f2 = tuUtils.createFile("second file");
    HashUtils huIstance = new HashUtils();

    String val1 = huIstance.getMD5Hash(f);
    String val2 = huIstance.getMD5Hash(f2);
    assertNotNull("Hashs for temp file is null", val1);
    assertNotNull("Hashs for temp file 2 is null", val2);
    assertNotEquals("equal hashes for different files", val1, val2);

    f.delete();
    f2.delete();
  }

  /**
   * Test of getMD5Hash method, of class HashUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGetMD5Hash_String()
      throws Exception {
    File f = tuUtils.createFile("first file");
    File f2 = tuUtils.createFile("second file");
    HashUtils huIstance = new HashUtils();

    String val1 = huIstance.getMD5Hash(f.getAbsolutePath());
    String val2 = huIstance.getMD5Hash(f2.getAbsolutePath());
    assertNotNull("Hashs for temp file is null for file: " + f.getAbsolutePath(), val1);
    assertNotNull("Hashs for temp file 2 is null for file" + f2.getAbsolutePath(), val2);
    assertNotEquals("equal hashes for different files", val1, val2);

    f.delete();
    f2.delete();
  }

  /**
   * Test of getMD5Hash method, of class HashUtils.
   * @throws java.lang.Exception
   */
  @Test
  public void testGetMD5Hash_InputStream()
      throws Exception {
    ByteArrayInputStream bis1 = new ByteArrayInputStream("first test file".getBytes("UTF-8"));
    ByteArrayInputStream bis2 = new ByteArrayInputStream("secodn test file".getBytes("UTF-8"));
    HashUtils huIstance = new HashUtils();

    String val1 = huIstance.getMD5Hash(bis1);
    String val2 = huIstance.getMD5Hash(bis2);
    assertNotNull("Hashs for first input stream is null" , val1);
    assertNotNull("Hashs for second input stream is null" , val2);
    assertNotEquals("equal hashes for different files", val1, val2);
  }


}
