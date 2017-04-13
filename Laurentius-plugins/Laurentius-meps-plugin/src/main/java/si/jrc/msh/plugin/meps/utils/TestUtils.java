/*
* Copyright 2016, Supreme Court Republic of Slovenia 
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved by 
* the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* https://joinup.ec.europa.eu/software/page/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis, WITHOUT 
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and  
* limitations under the Licence.
 */
package si.jrc.msh.plugin.meps.utils;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import static si.jrc.msh.plugin.meps.ejb.MEPSDataBean.BLOB_FOLDER;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.meps.envelope.EnvelopeData;
import si.laurentius.meps.envelope.PhysicalAddressType;
import si.laurentius.meps.envelope.PostalData;
import si.laurentius.meps.envelope.SenderMailData;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.outbox.property.MSHOutProperties;
import si.laurentius.plugin.meps.ServiceType;

/**
 *
 * @author Jože Rihtaršič
 */
public class TestUtils {

  public static final int RNUM_WEIGHTS[] = {8, 6, 4, 2, 3, 5, 9, 7};

  private static final AtomicInteger R_NUMBER = new AtomicInteger(1);

  private static final SEDLogger LOG = new SEDLogger(TestUtils.class);

  public static File[] mTstFiles = null;

  StorageUtils mstrgUtils = new StorageUtils();

  public String serialize(Object o)
          throws JAXBException {

    StringWriter sw = new StringWriter();

    JAXBContext carContext = JAXBContext.newInstance(o.getClass());
    Marshaller carMarshaller = carContext.createMarshaller();
    carMarshaller.marshal(o, sw);

    return sw.toString();
  }

  public List<File> getRandomFiles(int imin, int iMax, Random rnd) {

    int i = imin == iMax || imin > iMax ? imin : rnd.nextInt(iMax - imin)
            + imin;
    i = i > 0 ? i : 1;
    List<File> lst = new ArrayList<>();
    File[] testFiles = getTestFiles();
    while (i-- > 0) {
      lst.add(testFiles[rnd.nextInt(testFiles.length)]);
    }
    return lst;
  }

  public MSHOutMail createOutMail(
          int indx,
          String sndBox,
          String rcBox,
          String service,
          String action,
          ServiceType st) throws JAXBException, StorageException {
    Random rnd = new Random(Calendar.getInstance().getTimeInMillis());
    long l = LOG.logStart();

    String[] senderAddress = TCLookUp.TEST_COURT_ADDRESSES[rnd.nextInt(
            TCLookUp.TEST_COURT_ADDRESSES.length)];
    String[] receiverAddress = TCLookUp.TEST_COURT_ADDRESSES[rnd.nextInt(
            TCLookUp.TEST_COURT_ADDRESSES.length)];
    String contentDesc = TCLookUp.SUBJECTS[rnd.nextInt(TCLookUp.SUBJECTS.length)];
    List<File> fls = getRandomFiles(1, 5, rnd);

    MSHOutMail om = new MSHOutMail();

    om.setSenderMessageId("SM_ID-" + UUID.randomUUID().toString());
    om.setSubmittedDate(Calendar.getInstance().getTime());
    om.setAction(action);
    om.setService(service);
    om.setReceiverName(senderAddress[0]);
    om.setReceiverEBox(rcBox);
    om.setSenderName(receiverAddress[0]);
    om.setSenderEBox(sndBox);
    om.setSubject(contentDesc);
    om.setMSHOutProperties(new MSHOutProperties());

    om.setMSHOutPayload(new MSHOutPayload());
    
    addEnvelopeData(indx, om, senderAddress, senderAddress, st);

    int i = 0;
    for (File f : fls) {
      try {

        File fStorage = mstrgUtils.storeOutFile(MimeValue.getMimeTypeByFileName(
                f.getName()), f);
        MSHOutPart op = new MSHOutPart();
        op.setFilename(f.getName());

        op.setDescription(i++ == 0 ? "Sklep" : "Priloga");
        op.setFilepath(StorageUtils.getRelativePath(fStorage));
        op.setMimeType(MimeValue.getMimeTypeByFileName(f.getName()));
        om.getMSHOutPayload().getMSHOutParts().add(op);
      } catch (StorageException ex) {
        LOG.logError(l, ex);
      }
    }

    return om;

  }

  public void addEnvelopeData(int indx, MSHOutMail mo, String[] sa, String[] ra,
          ServiceType st) throws JAXBException, StorageException {
    EnvelopeData envelopeData = new EnvelopeData();
    envelopeData.setExecutorContractId("SP1");
    envelopeData.setExecutorId("ServiceProvider MEPS");
    envelopeData.setPostalData(new PostalData());
    envelopeData.setSenderMailData(new SenderMailData());
    
    envelopeData.getSenderMailData().setContentDescription(mo.getSubject());
    envelopeData.getSenderMailData().setCaseCode(String.format("VL %d/2017", indx));
    
    

    envelopeData.getPostalData().setMepsService(st.getName());
    envelopeData.getPostalData().setEnvelopeType(st.getEnvelopeName());
    if (st.isUseUPN()) {
      int irNumb = nextRNumber();
      int iCN = calculateControlNumber(irNumb);
      envelopeData.getPostalData().setUPNCode(new PostalData.UPNCode());
      envelopeData.getPostalData().getUPNCode().setPrefix(st.getUPNPrefix());
      envelopeData.getPostalData().getUPNCode().setCode(irNumb);
      envelopeData.getPostalData().getUPNCode().setControl(iCN);
      envelopeData.getPostalData().getUPNCode().setSuffix("SI");
    }
    
    

    envelopeData.getPostalData().setPostalContractName("Contract");
    envelopeData.getPostalData().setPostalContractId("");
    envelopeData.getPostalData().setSubmitPostalCode("1102");
    envelopeData.getPostalData().setSubmitPostalName("Ljublana");

    envelopeData.setSenderAddress(new PhysicalAddressType());
    envelopeData.getSenderAddress().setAddress(sa[2]);
    envelopeData.getSenderAddress().setCountry("Slovenija");
    envelopeData.getSenderAddress().setCountryCode("SVN");
    envelopeData.getSenderAddress().setName(sa[0]);
    envelopeData.getSenderAddress().setName2(sa[1]);
    envelopeData.getSenderAddress().setPostalCode(sa[3]);
    envelopeData.getSenderAddress().setPostalName(sa[4]);
    envelopeData.getSenderAddress().setTown(sa[4]);

    envelopeData.setReceiverAddress(new PhysicalAddressType());
    envelopeData.getReceiverAddress().setAddress(sa[2]);
    envelopeData.getReceiverAddress().setCountry("Slovenija");
    envelopeData.getReceiverAddress().setCountryCode("SVN");
    envelopeData.getReceiverAddress().setName(sa[0]);
    envelopeData.getReceiverAddress().setName2(sa[1]);
    envelopeData.getReceiverAddress().setPostalCode(sa[3]);
    envelopeData.getReceiverAddress().setPostalName(sa[4]);
    envelopeData.getReceiverAddress().setTown(sa[4]);

    byte buff[] = XMLUtils.serialize(envelopeData);
    File fStorage = mstrgUtils.storeOutFile(MimeValue.MIME_XML.getMimeType(), buff);
    
    
    MSHOutPart op = new MSHOutPart();
    op.setFilename("EnvelopeData.xml");
    op.setName("EnvelopeData");
    op.setDescription("EnvelopeData");
    op.setFilepath(StorageUtils.getRelativePath(fStorage));
    op.setMimeType(MimeValue.MIME_XML.getMimeType());
    mo.getMSHOutPayload().getMSHOutParts().add(op);
  }

  public static File[] getTestFiles() {
    if (mTstFiles == null) {

      File f = new File(SEDSystemProperties.getPluginsFolder(), StringFormater.
              replaceProperties(BLOB_FOLDER));
      mTstFiles = f.listFiles((File pathname) -> {
        return pathname.isFile()
                && pathname.getName().toLowerCase().endsWith(".pdf");
      });
    }
    return mTstFiles;
  }

  public static synchronized int nextRNumber() {
    int ir = R_NUMBER.getAndIncrement();
    if (ir > 99999999) {
      ir = 1;
      R_NUMBER.set(ir);
    }
    return ir;
  }

  public static int calculateControlNumber(int rNumber) {

    String rNum = rNumber + "";
    int iRNumLen = 8;
    while (rNum.length() < 8) {
      rNum = '0' + rNum;
    }
    int cs = 0;

    int r[] = {0, 0, 0, 0, 0, 0, 0, 0};
    for (int i = 0; i < iRNumLen; i++) {
      r[i] = rNum.charAt(i) * RNUM_WEIGHTS[i];
    }

    int t = 0;
    for (int i = 0; i < 8; i++) {
      t += r[i];
    }
    t %= 11;
    t = 11 - t;
    if (t >= 1 && t <= 9) {
      cs = t;
    } else if (t == 10) {
      cs = 0;
    } else if (t == 11) {
      cs = 5;
    }
    return cs;
  }
}
