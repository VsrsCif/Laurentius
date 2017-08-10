/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.task.filter;

import java.io.StringWriter;

/**
 *
 * @author sluzba
 */
public enum ReportTimeIntervalType {
  StatusChangedTime,
  AddedTime;

  public static String listOfNames() {
    StringWriter sw = new StringWriter();
    boolean bFirst = true;
    for (ReportTimeIntervalType rt : values()) {
      if (bFirst) {
        bFirst = false;
      } else {
        sw.append(",");
      }

      sw.append(rt.name());

    }
     return sw.toString();
  }
}
