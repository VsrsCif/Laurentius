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
package si.laurentius.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;

import si.laurentius.plg.db.IMPDBInterface;
import si.laurentius.plg.web.PlgSystemProperties;
import si.laurentius.plugin.imp.IMPXslt;
import si.laurentius.plugin.imp.XSLTRule;
import si.laurentius.plugin.interfaces.InMailProcessorInterface;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.exception.InMailProcessException;
import si.laurentius.plugin.processor.InMailProcessorDef;
import si.laurentius.process.xslt.XPathUtils;
import si.laurentius.process.xslt.XSLTNamespaceContext;
import si.laurentius.process.xslt.XSLTUtils;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(InMailProcessorInterface.class)
public class ProcessXSLT extends AbstractMailProcessor {

  private static final SEDLogger LOG = new SEDLogger(ProcessXSLT.class);
  public static final String KEY_XSLT_INSTANCE = "imp.xslt.instance";
  private static final String FILE_PREFIX = "xslt";
  private static final String FILE_SOURCE = "imp_xslt";

  XPathUtils mxsltUtils = new XPathUtils();

  @EJB
  private IMPDBInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDBDao;

  @Override
  public InMailProcessorDef getDefinition() {
    InMailProcessorDef impd = new InMailProcessorDef();
    impd.setType("xslt");
    impd.setName("XSLT processor");
    impd.setDescription("XSLT transform processor");

    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_XSLT_INSTANCE, null, "XSLT instance.", true,
            PropertyType.String.getType(), null, null));

    return impd;

  }

  @Override
  public List<String> getInstanceIds() {
    List<String> lst = new ArrayList<>();
    for (IMPXslt im : mDB.getXSLTs()) {
      lst.add(im.getInstance());
    }
    return lst;
  }

  @Override
  public boolean proccess(MSHInMail mi, Map<String, Object> map) throws InMailProcessException {
    long l = LOG.logStart(mi.getId());
    String instance = (String) map.get(KEY_XSLT_INSTANCE);
    boolean suc = false;
    IMPXslt ix = mDB.getXSLT(instance);

    tranformPayloads(ix, mi);
    suc = true;

    LOG.logEnd(l, instance, mi.getId());
    return suc;

  }

  public void tranformPayloads(IMPXslt xslt, MSHInMail mim)
          throws InMailProcessException {

    // if empty message  - skip transformation
    if (mim.getMSHInPayload() == null
            || mim.getMSHInPayload().getMSHInParts().isEmpty()) {
      LOG.formatedWarning(
              "Could not execute XSLT transformation '%s' on mail '%d' - no payloads",
              xslt.getInstance(), mim.getId());
      return;
    }

    for (XSLTRule xpr : xslt.getXSLTRules()) {

      List<MSHInPart> miplst = mim.getMSHInPayload().getMSHInParts();
      // create xpath context
      XPath xpath = mxsltUtils.createXPathFromNSContext(
              new XSLTNamespaceContext(
                      xpr.getXPath().getNamespaces()));
      //  get xslt transformation
      File fXSLT = getXSLTFile(xpr.getTransformation());
      // new payloads  holder
      List<MSHInPart> mipNewPayload = new ArrayList<>();

      for (MSHInPart mip : miplst) {
        // check if file is mimetype
        if (!Objects.equals(mip.getMimeType(), MimeValue.MIME_XML.
                getMimeType())) {
          continue;
        }

        try {
          Document doc = XMLUtils.deserializeToDom(StorageUtils.getFile(
                  mip.getFilepath()));

          // assert xpath rule  or there is no rule
          if (Utils.isEmptyString(xpr.getXPath().getXpath())
                  || !mxsltUtils.doesRuleApply(doc, xpath,
                          xpr.getXPath().getXpath(), xpr.getXPathValue())) {
            continue;
          }

          try {

            File fRes = StorageUtils.getNewStorageFile(
                    MimeValue.MIME_XML.getSuffix(), FILE_PREFIX);
            // transform
            XSLTUtils.transform(doc, fXSLT, fRes);

            String resultName = xpr.getResultFilename();
            resultName = Utils.isEmptyString(resultName) ? "Transformation" : resultName;

            MSHInPart mipt = new MSHInPart();
            mipt.setDescription(String.format(
                    "Transform of '%s' with xslt: '%s'", mip.
                            getFilename(), xpr.getTransformation()));

            mipt.setMimeType(MimeValue.MIME_XML.getMimeType());
            mipt.setFilename(resultName.toLowerCase().endsWith(
                    MimeValue.MIME_XML.getSuffix()) ? resultName
                    : resultName + "." +MimeValue.MIME_XML.getSuffix());
            mipt.setSource(FILE_SOURCE);
            mipt.setFilepath(StorageUtils.getRelativePath(fRes));
            mipt.setName(resultName);
            mipNewPayload.add(mipt);
          } catch (JAXBException | TransformerException | StorageException ex) {
            String errMsg = String.format(
                    "XSLT transformation failed %s!",
                    ex.getMessage());
            throw new InMailProcessException(
                    InMailProcessException.ProcessExceptionCode.ProcessException,
                    errMsg, ex, false, true);
          }

        } catch (IOException | ParserConfigurationException | SAXException
                | XPathExpressionException ex) {

          String errMsg = String.format(
                  "XSLT transformation failed %s!",
                  ex.getMessage());
          throw new InMailProcessException(
                  InMailProcessException.ProcessExceptionCode.ProcessException,
                  errMsg, ex, false, true);
        }

      }

      // add new payloads
      if (!mipNewPayload.isEmpty()) {
        mim.getMSHInPayload().getMSHInParts().addAll(mipNewPayload);
        try {
          mDBDao.updateInMail(mim, "transform", "");
        } catch (StorageException ex) {
          String errMsg = String.format(
                  "XSLT transformation failed %s!",
                  ex.getMessage());
          throw new InMailProcessException(
                  InMailProcessException.ProcessExceptionCode.ProcessException,
                  errMsg, ex, false, true);
        }

      }
    }
  }

  private File getXSLTFile(String xsltFileName) throws InMailProcessException {
    File fXSLT = new File(PlgSystemProperties.getXSLTFolder(), xsltFileName);
    if (!fXSLT.exists()) {
      String errMsg = String.format(
              "Transformation file %s not exist!", fXSLT.
                      getAbsolutePath());
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.InitException,
              errMsg, null, false, true);
    }
    return fXSLT;
  }

}
