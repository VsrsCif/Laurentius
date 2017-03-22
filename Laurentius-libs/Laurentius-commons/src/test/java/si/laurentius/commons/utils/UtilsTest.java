/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sluzba
 */
public class UtilsTest {

  public UtilsTest() {
  }

  @Test
  public void testIsValidEmailAddress() {
    System.out.println("isValidEmailAddress");
    
    String strEmailValid1 = "test@valid.com";
    String strEmailValid2 = "test.eeet@valid.com";

    String strEmailInvalid1 = "test.@invalid.com";
    String strEmailInvalid2 = "test.eeet.invalid.com";
    String strEmailInvalid3 = "test.eeet.invalid@com";
    String strEmailInvalid4 = "test.eeet@invalid@domain.com";

    assertTrue("valid mail: " +strEmailValid1, Utils.isValidEmailAddress(strEmailValid1));
    assertTrue("valid mail: " +strEmailValid2 , Utils.isValidEmailAddress(strEmailValid2));

    assertFalse("invalid mail: " +strEmailInvalid1, Utils.isValidEmailAddress(strEmailInvalid1));
    assertFalse("invalid mail: " +strEmailInvalid2, Utils.isValidEmailAddress(strEmailInvalid2));
    assertFalse("invalid mail: " +strEmailInvalid3, Utils.isValidEmailAddress(strEmailInvalid3));
    assertFalse("invalid mail: " +strEmailInvalid4, Utils.isValidEmailAddress(strEmailInvalid4));

  }

}
