/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.tc.utils;

import java.io.InputStream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sluzba
 */
public class TestCaseParserTest {
  
  public TestCaseParserTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @Before
  public void setUp() {
  }

  /**
   * Test of initTestCases method, of class TestCaseParser.
   */
  @Test
  public void testInitTestCases() {
    System.out.println("initTestCases");
    InputStream is = TestCaseParserTest.class.getResourceAsStream("/testcases.json");
    TestCaseParser instance = new TestCaseParser();
    instance.initTestCases(is);
    // TODO review the generated test code and remove the default call to fail.

  }
  
}
