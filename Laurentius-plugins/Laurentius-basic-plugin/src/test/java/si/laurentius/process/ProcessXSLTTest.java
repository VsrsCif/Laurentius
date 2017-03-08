/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.process;

import java.util.List;
import java.util.Map;
import javax.ejb.embeddable.EJBContainer;
import org.junit.Test;
import static org.junit.Assert.*;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.plugin.imp.IMPXslt;
import si.laurentius.plugin.imp.Namespace;
import si.laurentius.plugin.processor.InMailProcessorDef;

/**
 *
 * @author sluzba
 */
public class ProcessXSLTTest {

  public ProcessXSLTTest() {
  }

  @Test
  public void testTranformPayloads() throws Exception {
   /* System.out.println("tranformPayloads");
    IMPXslt xslt = null;
    MSHInMail mim = createInMail(null);

    ProcessXSLT instance = new ProcessXSLT();
    instance.tranformPayloads(xslt, mim);
    fail("The test case is a prototype.");*/
  }
  
  public IMPXslt createXSLT(String strXslt){
    IMPXslt xslt = new IMPXslt();
    xslt.setInstance(strXslt);
    Namespace ns1 = new Namespace();
    xslt.getNamespaces().add(ns1);
    
    return xslt;
  }

  private MSHInMail createInMail(String xmlPart) {
    MSHInMail mi = new MSHInMail();
    mi.setService("testService");
    mi.setAction("testAction");
    mi.setSenderEBox("sender@box.com");
    mi.setReceiverEBox("sender@box.com");
    mi.setConversationId("12345@box.com");
    mi.setMSHInPayload(new MSHInPayload());
    // create mime pdf
    MSHInPart mipPDF = new MSHInPart();
    mipPDF.setMimeType(MimeValue.MIME_PDF.getMimeType());
    mipPDF.setFilename("test.pdf");
    mipPDF.setDescription("test pdf");
    mi.getMSHInPayload().getMSHInParts().add(mipPDF);

    if (!Utils.isEmptyString(xmlPart)) {
      MSHInPart mipXML = new MSHInPart();
      mipXML.setMimeType(MimeValue.MIME_XML.getMimeType());
      mipXML.setFilename("test.xml");
      mipXML.setDescription("test xml");
      mi.getMSHInPayload().getMSHInParts().add(mipXML);
    }

    return mi;
  }
 
}
