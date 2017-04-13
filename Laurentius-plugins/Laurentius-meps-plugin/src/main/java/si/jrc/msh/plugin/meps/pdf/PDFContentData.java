/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.meps.pdf;

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

    private byte[] mbaData = null;
    private int miDocumentCount = -1;
    private int miPageCount = -1;
    private String mstrTempFileName =  null;

    public byte[] getData() {
        byte[] ba = mbaData;
        /*
        //System.out.println("MailContentData: getData tempFile: " + mstrTempFileName);
        if (ba == null && mstrTempFileName!= null){
            FileInputStream fis = null;
            try {
                //System.out.println("MailContentData: read from tempFile: " + mstrTempFileName);
                fis = new FileInputStream(mstrTempFileName);
                ba = new byte[fis.available()];
                fis.read(ba);
            } catch (IOException ex) {
                Logger.getLogger(PDFContentData.class.getName()).log(Level.SEVERE, null, ex);
            }  finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(PDFContentData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/
        return ba;
    }

    public void setData(byte[] mbaData) {
        this.mbaData = mbaData;
    }

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

    public String getTempFileName() {
        return mstrTempFileName;
    }

    public void setTempFileName(String mstrTempFileName) {
        this.mstrTempFileName = mstrTempFileName;
        if (this.mstrTempFileName!= null){
            FileInputStream fis = null;
            try {
                //System.out.println("MailContentData: read from tempFile: " + mstrTempFileName);
                fis = new FileInputStream(mstrTempFileName);
                mbaData = new byte[fis.available()];
                fis.read(mbaData);
            } catch (IOException ex) {
                Logger.getLogger(PDFContentData.class.getName()).log(Level.SEVERE, null, ex);
            }  finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(PDFContentData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    public void deleteTempFile(){
        if(mstrTempFileName!=null){
            File f = new File(mstrTempFileName);
            if (f.exists()) f.delete();
        }
    }

}
