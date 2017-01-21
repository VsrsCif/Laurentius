/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.plg.db;

import java.io.File;
import java.util.List;
import javax.ejb.Local;
import si.laurentius.plugin.imp.IMPExecute;
import si.laurentius.plugin.imp.IMPExport;
import si.laurentius.plugin.imp.IMPXslt;

/**
 *
 * @author sluzba
 */
@Local
public interface IMPDBInterface {


  void exportInitData(File f);
  
  IMPExport getExport(String instance);
  boolean addExport(IMPExport sb);
  List<IMPExport> getExports();
  boolean removeExport(IMPExport sb);
  boolean updateExport(IMPExport sb);
  
  IMPExecute getExecute(String instance);
  boolean addExecute(IMPExecute sb);
  List<IMPExecute> getExecutes();
  boolean removeExecute(IMPExecute sb);
  boolean updateExecute(IMPExecute sb);
  
  
  IMPXslt getXSLT(String instance);
  boolean addXSLT(IMPXslt sb);
  List<IMPXslt> getXSLTs();
  boolean removeXSLT(IMPXslt sb);
  boolean updateXSLT(IMPXslt sb);


}
