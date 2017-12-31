/*
 * Copyright 2015, Supreme Court Republic of Slovenia
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
package si.jrc.msh.plugin.zpp.utils;

import static com.jrc.xml.DateAdapter.parseDateTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.getInstance;
import java.util.Date;
import java.util.GregorianCalendar;
import si.laurentius.commons.SEDSystemProperties;

/**
 * XSLT extension for FOP transformations. 
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class XSLTExtensions {

  private static final ThreadLocal<DateFormat> S_DATE_FORMAT = new ThreadLocal<DateFormat>() {
    @Override
    protected DateFormat initialValue() {
      return new SimpleDateFormat("dd. MM. yyyy");
    }
  };

  private static final ThreadLocal<DateFormat> S_DATE_TIME_FORMAT = new ThreadLocal<DateFormat>() {
    @Override
    protected DateFormat initialValue() {
      return new SimpleDateFormat("dd. MM. yyyy HH:mm");
    }
  };

  /**
   * Method returs current date string representation 
   * @return  current date
   */
  public static Object currentDate() {
    return S_DATE_FORMAT.get().format(getInstance().getTime());
  }

  /**
   * Method returs current dateTime string representation 
   * @return  current dateTime
   */
  public static Object currentDateTime() {
    return S_DATE_TIME_FORMAT.get().format(getInstance().getTime());
  }

  /**
   * Parse date from string
   * @param str - date string representation
   * @return
   */
  public static Object formatDate(String str) {
    if (str == null || str.trim().isEmpty()) {
      return null;
    }

    Date dt = parseDateTime(str);
    return S_DATE_FORMAT.get().format(dt);
  }

  /**
   * Return fiction date for start date
   * @param str
   * @return
   */
  public static Object getZPPFictionDate(String str) {
    if (str == null || str.trim().isEmpty()) {
      return null;
    }
    Date dt = parseDateTime(str);
    Calendar c = new GregorianCalendar();
    c.setTime(dt);
    c.add(DAY_OF_MONTH, 15);
    return S_DATE_FORMAT.get().format(c.getTime());

  }
  
   /**
   * Return fiction date for start date
   * @param str
   * @return
   */
  public static String systemDomain() {
    return SEDSystemProperties.getLocalDomain();

  }
  
  public static String domainForAddress(String address) {
    return address!=null && address.contains("@") ? 
            address.substring(address.indexOf('@')):"";

  }
  

}
