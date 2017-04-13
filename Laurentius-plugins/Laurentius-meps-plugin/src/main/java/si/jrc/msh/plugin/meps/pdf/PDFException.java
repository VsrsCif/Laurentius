/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package si.jrc.msh.plugin.meps.pdf;

/**
 *
 * @author logos
 */
public class PDFException extends Exception {

  

    public PDFException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
    
    public PDFException(Throwable arg0) {
        super(arg0);
    }
    
    public PDFException(String arg0) {
        super(arg0);
    }

    public PDFException() {
    }
    
}
