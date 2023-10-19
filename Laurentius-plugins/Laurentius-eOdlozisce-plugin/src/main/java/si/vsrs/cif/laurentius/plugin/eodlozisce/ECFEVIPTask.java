/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.cxf.EBMSConstants;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskaOvojnica;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskaPosiljkaTip;
import si.sodisce.sheme.skupno.skupno.v1.UdelezenecTip;
import si.vsrs.cif.laurentius.plugin.eodlozisce.sig.XMLSignatureUtils;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.FilingValidationStage;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.SchemaValidationStage;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationOutput;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationResult;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.XmlSignatureValidationStage;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import static si.vsrs.cif.laurentius.plugin.eodlozisce.validation.SchemaValidationStage.ErrorCodes.MISSING_METADATA_XML;

@Stateless
@Local(TaskExecutionInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ECFEVIPTask implements TaskExecutionInterface {

    private static final SEDLogger LOG = new SEDLogger(ECFEVIPTask.class);
    public static final String KEY_PAYLOAD_METADATA_NAME = "ecf.payload.metadata.name";
    private static final String SIGN_ALIAS = "ecf.sign.key.alias";

    @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
    SEDDaoInterface mDB;

    @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
    SEDCertStoreInterface mCertBean;

    @EJB(mappedName = SEDJNDI.JNDI_PMODE)
    PModeInterface mpModeManager;

    SchemaValidationStage schemaValidator;
    XmlSignatureValidationStage xmlSignatureValidator;
    FilingValidationStage filingValidationStage;
    XMLSignatureUtils signatureUtils;

    @PostConstruct
    public void init() {
        try {
            this.signatureUtils = new XMLSignatureUtils();
            this.schemaValidator = new SchemaValidationStage();
            this.filingValidationStage = new FilingValidationStage();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String executeTask(Properties properties) throws TaskException {

        long l = LOG.logStart();
        StringWriter sw = new StringWriter();
        sw.append("Start eOdlozisce plugin task: \n");

        MSHInMail mi = new MSHInMail();
        mi.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
        mi.setService(EOdlozisceConstants.EODLOZISCE_SERVICE);
        mi.setAction(EOdlozisceConstants.EODLOZISCE_ACTION);
        // mi.setReceiverEBox(sedBox + "@" + SEDSystemProperties.getLocalDomain());

        List<MSHInMail> inMailList = mDB.getDataList(MSHInMail.class, -1, 100, "Id", "ASC", mi);

        String metadataAttachmentName = properties.getProperty(KEY_PAYLOAD_METADATA_NAME);

        sw.append("End ecf plugin task");
        return sw.toString();
    }

    @Override
    public CronTaskDef getDefinition() {

        CronTaskDef tt = new CronTaskDef();
        tt.setType("ecf-evip-task");
        tt.setName("ECF EVIP task");
        tt.setDescription("Task for the EVIP integration");
        return tt;
    }
}
