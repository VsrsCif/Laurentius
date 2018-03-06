/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.plg.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author logos
 */
public class PDFContentData {


    private int miDocumentCount = -1;
    private int miPageCount = -1;
    private String mstrTempFileName =  null;



    public int getDocumentCount() {
        return miDocumentCount;
    }

    public void setDocumentCount(int miDocumentCount) {
        this.miDocumentCount = miDocumentCount;
    }

    public int getPageCount() {
        return miPageCount;
    }

    public void setPageCount(int miPageCount) {
        this.miPageCount = miPageCount;
    }

    public void setTempFileName(String file) {
        mstrTempFileName = file;
    }
    public String getTempFileName() {
        return mstrTempFileName;
    }

   
    public void deleteTempFile(){
        if(mstrTempFileName!=null && !mstrTempFileName.isEmpty()){
            File f = new File(mstrTempFileName);
            if (f.exists()) f.delete();
        }
    }

}
