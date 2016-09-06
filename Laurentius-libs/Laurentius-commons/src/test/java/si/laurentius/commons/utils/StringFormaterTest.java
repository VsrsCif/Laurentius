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

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import si.laurentius.commons.utils.entity.TestReflectClass;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class StringFormaterTest {
  
  public StringFormaterTest() {
  }
  
  @Before
  public void setUp() {
  }

  /**
   * Test of formatCVS method, of class StringFormater.
   */
  @Test
  public void testFormatCVS() {

    int i = 10;
    TestReflectClass trv = new TestReflectClass();
    trv.setName("Test");
    trv.setAge(55);
    trv.setHeight(null);
   
    List<String> properties = Arrays.asList(new String[] {"Name", "Height","Age"});
    
    StringFormater instance = new StringFormater();
    String expResult = "10.,Test,,55";
    String result = instance.formatCVS(properties, trv, i);
    assertEquals(expResult, result);
       
     i = 11;
    trv.setName("Test,test2");
    trv.setAge(12);
    trv.setHeight((long)170);
    expResult = "11.,Test\\,test2,170,12";
    result = instance.formatCVS(properties, trv, i);
    assertEquals(expResult, result);

  }

  /**
   * Test of format method, of class StringFormater.
   */
  @Test
  public void testFormat_Object() {
    TestReflectClass trv = new TestReflectClass();
    trv.setName("John");
    trv.setAge(35);
    trv.setHeight(null);
    
    String format = "${Name} is ${Age} old and his height is ${Height} cm.";
    StringFormater instance = new StringFormater();
    String expResult = "John is 35 old and his height is  cm.";
    String result = instance.format(format, trv);
    assertEquals(expResult, result);
  }

 
  /**
   * Test of replaceProperties method, of class StringFormater.
   */
  @Test
  public void testReplaceProperties() {
    System.setProperty("test.property","testValue");
    System.setProperty("test.key","Test key");
    String format = "${test.key} is ${test.property}.";
    String expResult = "Test key is testValue.";
    String result = StringFormater.replaceProperties(format);
    assertEquals(expResult, result);
  }
  
}
