/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.vsrs.cif.filing.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author sluzba
 */
public class PDFUtils {
  private static final SEDLogger LOG = new SEDLogger(PDFUtils.class);

  public String testPDFA(File pdfFile) {
    long l = LOG.logStart();
    
    
    ValidationResult result = null;
    PreflightParser parser;
    try {
      parser = new PreflightParser(pdfFile);
    } catch (IOException ex) {
      String msg = String.format("Error occured while parsing file %s. Message %s", pdfFile.getAbsolutePath(), ex.getMessage());
      LOG.logWarn(msg, ex);
      return msg;
    }

    try {
      parser.parse();

      try (PreflightDocument document = parser.getPreflightDocument()) {
        document.validate();
        
        // Get validation result
        result = document.getResult();
      }

    } catch (SyntaxValidationException e) {   
      String msg = String.format("SyntaxValidationException occured while validating file %s. Message %s", pdfFile.getAbsolutePath(), e.getMessage());
      LOG.logWarn(msg, e);
    } catch (IOException ex) {
      String msg = String.format("Error occured while validating file %s. Message %s", pdfFile.getAbsolutePath(), ex.getMessage());
      LOG.logWarn(msg, ex);
      return msg;
    }
    

// display validation result
    if (result!= null && !result.isValid()) {
      StringWriter sw = new StringWriter();
      sw .append("File is PDF/A invalid!");
      
      for (ValidationResult.ValidationError error : result.getErrorsList()) {
        sw.append(error.getErrorCode());
        sw.append(" : ");
        sw.append(error.getDetails());
      }
      return sw.toString();
     
    }

    return null;
  }
}
