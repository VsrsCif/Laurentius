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

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import static java.lang.System.getProperty;

/**
 * class contains methods for formattig string using the specified format string
 * and arguments.
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class StringFormater {

  private static final SEDLogger LOG = new SEDLogger(StringFormater.class);
  private static final int IN_BRACKET = 2;
  private static final int NORMAL = 0;
  private static final int SEEN_DOLLAR = 1;
  private static final Object[] EMPTY_ARRAY = new Object[0];

  /**
   * Method is "borrowed" from org.jboss.util.StringPropertyReplacer; Go through
   * the input string and replace any occurance of ${p} with the
   * System.getProperty(p) value. If there is no such property p defined, then
   * the ${p} is replaced with "".
   *
   *
   * @param string - the string with possible ${} references
   * @return the input string with all property references replaced if any. If
   * there are no valid references the input string will be returned.
   */
  public static String replaceProperties(final String string) {
    return format(string, null);
  }

  public static String format(final String formatedString, Object obj) {

    return format(formatedString, obj, null, null);
  }

  public static String format(final String formatedString,
          final Object obj,
          final String namespace,
          final Object nsObj) {

    final char[] chars = formatedString.toCharArray();
    StringBuilder buffer = new StringBuilder();
    boolean properties = false;
    int state = NORMAL;
    int start = 0;
    for (int i = 0; i < chars.length; ++i) {
      char c = chars[i];

      // Dollar sign outside brackets
      if (c == '$' && state != IN_BRACKET) {
        state = SEEN_DOLLAR;
      } // Open bracket immediatley after dollar
      else if (c == '{' && state == SEEN_DOLLAR) {
        buffer.append(formatedString.substring(start, i - 1));
        state = IN_BRACKET;
        start = i - 1;
      } // No open bracket after dollar
      else if (state == SEEN_DOLLAR) {
        state = NORMAL;
      } // Closed bracket after open bracket
      else if (c == '}' && state == IN_BRACKET) {
        // No content
        if (start + 2 == i) {
          buffer.append("${}"); // REVIEW: Correct?
        } else {
           
          String key = formatedString.substring(start + 2, i);
          properties = true;
          String val = null;
          if (nsObj != null && !Utils.isEmptyString(namespace)
                  && key.startsWith(namespace)) {

            val = getPropertyValue(key.substring(namespace.length()), nsObj);
          } 
          
          if (val == null && obj != null) {
            val = getPropertyValue(key, obj);
          }
          // if val still null try system properties
          if (val == null) {
            val = getProperty(key);
          }

          if (val != null) {
            buffer.append(val);          
          } else {
             buffer.append(formatedString.substring(start, i+1));
          }

        }

        start = i + 1;
        state = NORMAL;
      }
    }

    // No properties
    if (properties == false) {
      return formatedString;
    }

    // Collect the trailing characters
    if (start != chars.length) {
      buffer.append(formatedString.substring(start, chars.length));
    }
    // Done
    return buffer.toString();

  }

  private static String getPropertyValue(final String property, final Object obj) {
    assert property != null : "Property must not be null";
    if (obj == null) {
      return null;
    }

    try {
      Method m = obj.getClass().getMethod("get" + property);
      Object value = m.invoke(obj, EMPTY_ARRAY);;
      return object2String(value);

    } catch (NoSuchMethodException ex) {
      return null;
    } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      LOG.formatedWarning("Error accesing property %s, for class %s. Ex: %s!",
              property, obj.getClass().getName(), ex.getMessage());
    }
    return null;
  }

  /**
   * Method returs CSV string for values of object properties.
   *
   * @param properties - object priperties in CSV string
   * @param obj - object
   * @param i - index
   * @return String CVS for object
   */
  public String formatCVS(List<String> properties, Object obj, int i) {
    long l = LOG.getTime();

    Class cls = obj.getClass();
    StringWriter sw = new StringWriter();
    sw.write(i + ".");

    properties.stream().forEach(
            (mth) -> {
              try {
                Method md = cls.getDeclaredMethod("get" + mth);
                Object res = md.invoke(obj, EMPTY_ARRAY);

                String value = object2String(res);
                sw.write(",");
                sw.write(value.replace("\\", "\\\\").replace(",", "\\,"));

              } catch (NoSuchMethodException | SecurityException | IllegalAccessException
              | IllegalArgumentException | InvocationTargetException ex) {
                LOG.logError(l,
                        "Error occured while formating object to CVS string!",
                        ex);
              }
            });
    return sw.toString();
  }

  /**
   * Returns a formatted string using the specified format string and arguments
   * for object with properties in given string. ex.: "${Id} has a value:
   * ${Value}" for object with properties (getId/setId and getValue/setValue )
   *
   * @param format - string parameteres with ${object.properties}
   * @param dce - object
   * @return formatted string
   *
   * public String format(String format, Object dce) { long l = LOG.getTime();
   * HashMap<String, Object> hm = new HashMap<>(); Method[] mthLst =
   * dce.getClass().getDeclaredMethods(); for (Method mt : mthLst) {
   *
   * if (mt.getName().startsWith("get")) { String strName =
   * mt.getName().substring(3); Object put = null; try { if
   * (mt.getParameterTypes().length == 0) { put = mt.invoke(dce, EMPTY_ARRAY); }
   * } catch (IllegalAccessException | IllegalArgumentException |
   * InvocationTargetException ex) { LOG.logError(l, "Error occured while
   * formating object to to formated string!", ex); } hm.put(strName, put);
   *
   * }
   * }
   * return format(format, hm); }
   */
  /**
   * Returns a formatted string using the specified format string and arguments
   * from map object.
   *
   * @param format - string format -> ex. "${Id} has a value: ${Value}"
   * @param values - map with keys ex.: Id, Value, etc
   * @return formated string.
   *
   * public String format(String format, Map<String, Object> values) {
   *
   * StringBuilder builder = new StringBuilder(format);
   *
   * values.entrySet().stream().forEach((entry) -> { int start; String pattern =
   * "${" + entry.getKey() + "}"; String value =
   * object2String(entry.getValue());
   *
   * // Replace every occurence of $(key) with value while ((start =
   * builder.indexOf(pattern)) != -1) { builder.replace(start, start +
   * pattern.length(), value); } });
   *
   * return builder.toString(); }
   */
  /**
   * Converts object to string
   *
   * @param o
   * @return
   */
  private static String object2String(Object o) {
    String res = null;
    if (o == null) {
      res = "";
    } else if (o instanceof String) {
      res = (String) o;
    } else if (o instanceof Integer) {
      res = ((Integer) o).toString();
    } else if (o instanceof BigInteger) {
      res = ((BigInteger) o).toString();
    } else if (o instanceof BigDecimal) {
      res = ((BigDecimal) o).toString();
    } else if (o instanceof Double) {
      res = ((Double) o).toString();
    } else if (o instanceof Date) {
      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm.ss");
      res = sdf.format((Date) o);
    } else {
      res = o.toString();
    }
    return res;

  }

}
