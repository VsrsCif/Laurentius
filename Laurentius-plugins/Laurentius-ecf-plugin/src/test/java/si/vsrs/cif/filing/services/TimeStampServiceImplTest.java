package si.vsrs.cif.filing.services;

import org.junit.Test;
import si.laurentius.commons.cxf.SoapUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class TimeStampServiceImplTest {

    @Test
    public void timeStampXml() throws IOException {
        System.out.println("Manual timestamp test");
/*
        TimeStampServiceImpl testInstance= new TimeStampServiceImpl();
        File file = new File("/waso/test-eOdlozisce/SplosnaVloga-Signed.xml");
        String timestampService = "http://ts.si-tsa.sigov.si:80/verificationserver/timestamp";
        byte[] timestampedXML =  testInstance.timeStampXmlFile(file,timestampService, 2000, "aaa","bbb","ccc" );
        System.out.println(new String(timestampedXML));
*/
    }
}