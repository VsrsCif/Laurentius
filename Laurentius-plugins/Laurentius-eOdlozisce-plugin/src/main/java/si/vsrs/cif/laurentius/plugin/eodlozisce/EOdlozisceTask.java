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
public class EOdlozisceTask implements TaskExecutionInterface {

    private static final SEDLogger LOG = new SEDLogger(EOdlozisceTask.class);
    public static final String KEY_PAYLOAD_METADATA_NAME = "ecf.payload.metadata.name";
    private static final String SIGN_ALIAS = "zkp.sign.key.alias";
    public static final String KEY_SIGNATURE_KEY_ALIAS = "ecf.sign.key.alias";

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
        // set status to proccess
        inMailList.forEach((inMail) -> {
            try {
                List<MSHInPart> mshInParts = inMail.getMSHInPayload().getMSHInParts();
                ValidationResult validationResult = validateSchemas(metadataAttachmentName, mshInParts);

                if (validationResult.getValidationOutputs().stream().anyMatch((o) -> o.getSeverity().equals(ValidationOutput.Severity.ERROR))) {
                    Document signedDocument = this.signatureUtils.signXmlDocument(
                            getPrivateKeyEntry(properties),
                            getDocumentFromObject(
                                    createDefaultEPosiljkaAndAttachXml(metadataAttachmentName, inMail),
                                    ElektronskaOvojnica.class));
                    replaceWithSignedDocument(metadataAttachmentName, inMail, mshInParts, signedDocument);
                } else {

                    Optional<MSHInPart> mshInPart = mshInParts.stream()
                            .filter((part) -> metadataAttachmentName.equals(part.getName())).findFirst();
                    ValidationResult signatureValidationResultOptional = mshInPart
                            .map((part) -> this.xmlSignatureValidator.validate(Paths.get(part.getFilepath()))
                            ).orElse(new ValidationResult());
                    if (signatureValidationResultOptional.getValidationOutputs().stream().anyMatch(ValidationOutput::isError)) {
                        // TODO consolidate these two if / elseif blocks.
                        // On error -> signature validation failed, delete signature and sign again
                        ElektronskaOvojnica elektronskaOvojnica = getElektronskaOvojnica(Files.newInputStream(Paths.get(mshInPart.get().getFilepath())));
                        elektronskaOvojnica.getSignatures().removeIf((signature -> signature.getSignedInfo().getSignatureMethod().getAlgorithm().equalsIgnoreCase(XMLSignatureUtils.SHA256_WITH_RSA_URI)));
                        Document signedDocument = this.signatureUtils.signXmlDocument(
                                getPrivateKeyEntry(properties), getDocumentFromObject(elektronskaOvojnica, ElektronskaOvojnica.class));
                        replaceWithSignedDocument(metadataAttachmentName, inMail, mshInParts, signedDocument);
                    } else if (signatureValidationResultOptional.getValidationOutputs().stream().anyMatch(ValidationOutput::isWarning)) {
                        // On warning -> signature not present, sign again
                        if (mshInPart.isPresent()) {
                            ElektronskaOvojnica elektronskaOvojnica = getElektronskaOvojnica(Files.newInputStream(Paths.get(mshInPart.get().getFilepath())));
                            Document signedDocument = this.signatureUtils.signXmlDocument(
                                    getPrivateKeyEntry(properties), getDocumentFromObject(elektronskaOvojnica, ElektronskaOvojnica.class));
                            replaceWithSignedDocument(metadataAttachmentName, inMail, mshInParts, signedDocument);
                        } else {
                            throw new RuntimeException("No part to sign.");
                        }
                    }

                    // Schema validation passed without errors
                    ValidationResult filingValidationResults = validateFilings(metadataAttachmentName, mshInParts);
                    if (filingValidationResults.getValidationOutputs().stream().anyMatch((ValidationOutput::isError))) {
                        // filing errors
                        mDB.setStatusToInMail(inMail, SEDInboxMailStatus.ERROR, "Add message to zkp deliver proccess");
                    }
                    validationResult.chain(filingValidationResults);

                }

                // TODO: generate JSON report of validation errors file and attach
                // TODO: TSA

                mDB.setStatusToInMail(inMail, SEDInboxMailStatus.PROCESS,
                        "Add message to zkp deliver proccess");
            } catch (StorageException ex) {
                String msg = String.format(
                        "Error occurred processing mail: '%s'. Err: %s.", inMail.getId(),
                        ex.getMessage());
                LOG.logError(l, msg, ex);
                sw.append(msg);
            } catch (JAXBException | ParserConfigurationException | SEDSecurityException | FileNotFoundException |
                     TransformerException e) {
                throw new RuntimeException(e);
            } catch (TaskException e) {
                // no sign key alias
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        sw.append("End zkp plugin task");
        return sw.toString();
    }

    private ValidationResult validateFilings(String metadataAttachmentName, List<MSHInPart> mshInParts) {
        return mshInParts.stream().filter((part) -> metadataAttachmentName.equals(part.getName())).findFirst().map((part) -> {
                    try {
                        return this.filingValidationStage.validate(getElektronskaOvojnica(Files.newInputStream(Paths.get(part.getFilepath()))));
                    } catch (IOException | JAXBException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).orElse(new ValidationResult());
    }

    private ValidationResult validateSchemas(String metadataAttachmentName, List<MSHInPart> mshInParts) {
        return mshInParts.stream().filter((part) -> metadataAttachmentName.equals(part.getName())).findFirst().map((part) ->
                {
                    try {
                        return this.schemaValidator.validate(Files.newInputStream(Paths.get(part.getFilepath())));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).orElseGet(() -> {
            ValidationResult missingPart = new ValidationResult();
            missingPart.add(new ValidationOutput(ValidationOutput.Severity.ERROR, MISSING_METADATA_XML));
            return missingPart;
        });
    }


    // TODO move out of TASK file
    private <T> Document getDocumentFromObject(Object elektronskaOvojnica, Class<T> c) throws ParserConfigurationException, JAXBException {
        JAXBContext jc = JAXBContext.newInstance(c);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(elektronskaOvojnica, document);

        return document;
    }

    private ElektronskaOvojnica createDefaultEPosiljkaAndAttachXml(String metadataAttachmentName, MSHInMail inMail) throws StorageException {
        mDB.setStatusToInMail(inMail, SEDInboxMailStatus.ERROR, "Error validating metadata file");

        ElektronskaOvojnica elektronskaOvojnica = createElektronskaOvojnica();

        MimeValue soapPartMime = MimeValue.MIME_XML;
        File f = StorageUtils.getNewStorageFile(soapPartMime.getSuffix(), EBMSConstants.SOAP_PART_REQUEST_PREFIX);
        MSHInPart part = getMshInPart(
                metadataAttachmentName + "_invalid_" + UUID.randomUUID(), inMail.getMessageId(), soapPartMime, f);
        inMail.getMSHInPayload().getMSHInParts().add(part);

        // TODO: sign and timestamp the signature

        try {
            JAXBContext jc = JAXBContext.newInstance(ElektronskaOvojnica.class);
            TransformerFactory.newInstance().newTransformer().transform(
                    new JAXBSource(jc, elektronskaOvojnica),
                    new StreamResult(f));

            return elektronskaOvojnica;
        } catch (TransformerException | JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private MSHInPart getMshInPart(String metadataAttachmentName, String messageId, MimeValue soapPartMime, File f) throws StorageException {
        MSHInPart part = new MSHInPart();
        part.setName(metadataAttachmentName);
        part.setIsSent(Boolean.FALSE);
        part.setIsReceived(Boolean.TRUE);
        part.setEbmsId(messageId);
        part.setMimeType(soapPartMime.getMimeType());
        part.setDescription("XML Metadata");
        part.setSource(SEDMailPartSource.PLUGIN.getValue());
        part.setFilename(f.getName());
        part.setFilepath(StorageUtils.getRelativePath(f));
        return part;
    }

    private ElektronskaOvojnica createElektronskaOvojnica() {
        ElektronskaPosiljkaTip elektronskaPosiljkaTip = new ElektronskaPosiljkaTip();
        UdelezenecTip udelezenecTip = new UdelezenecTip();
        udelezenecTip.setId("GeneratedByValidation");
        elektronskaPosiljkaTip.setPrejemnik(udelezenecTip);
        ElektronskaOvojnica elektronskaOvojnica = new ElektronskaOvojnica();
        elektronskaOvojnica.setPosiljka(elektronskaPosiljkaTip);
        return elektronskaOvojnica;
    }

    private ElektronskaOvojnica getElektronskaOvojnica(InputStream elektronskaOvojnicaInputStream) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(ElektronskaOvojnica.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (ElektronskaOvojnica) unmarshaller.unmarshal(elektronskaOvojnicaInputStream);
    }

    @Override
    public CronTaskDef getDefinition() {

        CronTaskDef tt = new CronTaskDef();
        tt.setType("eodlozisce-validation");
        tt.setName("Validate eOdlozisce package");
        tt.setDescription("Validate and forward eVlozisce package.");
        return tt;
    }

    private CronTaskPropertyDef createTTProperty(String key, String desc,
                                                 boolean mandatory,
                                                 String type, String valFormat, String valList) {
        CronTaskPropertyDef ttp = new CronTaskPropertyDef();
        ttp.setKey(key);
        ttp.setDescription(desc);
        ttp.setMandatory(mandatory);
        ttp.setType(type);
        ttp.setValueFormat(valFormat);
        ttp.setValueList(valList);
        return ttp;
    }

    private String getSignKeyAlias(Properties properties) throws TaskException {
        String signKeyAlias = "";
        if (!properties.containsKey(SIGN_ALIAS)) {
            throw new TaskException(TaskException.TaskExceptionCode.InitException,
                    "Missing parameter:  '" + SIGN_ALIAS + "'!");
        } else {
            signKeyAlias = properties.getProperty(SIGN_ALIAS);
        }
        return signKeyAlias;
    }

    private KeyStore.PrivateKeyEntry getPrivateKeyEntry(Properties p) throws TaskException, SEDSecurityException {
        String signKeyAlias = getSignKeyAlias(p);
        PrivateKey pk = mCertBean.getPrivateKeyForAlias(signKeyAlias);
        X509Certificate xcert = mCertBean.getX509CertForAlias(signKeyAlias);
        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = xcert;
        return new KeyStore.PrivateKeyEntry(pk, chain);
    }

    private void writeXml(Document doc, OutputStream out) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(out);

        transformer.transform(source, result);
    }

    private void replaceWithSignedDocument(String metadataAttachmentName, MSHInMail inMail, List<MSHInPart> mshInParts, Document signedDocument) throws StorageException, TransformerException, FileNotFoundException {
        MimeValue soapPartMime = MimeValue.MIME_XML;
        File f = StorageUtils.getNewStorageFile(soapPartMime.getSuffix(), EBMSConstants.SOAP_PART_REQUEST_PREFIX);
        writeXml(signedDocument, new FileOutputStream(f));
        mshInParts.removeIf(inPart -> metadataAttachmentName.equals(inPart.getName()));
        MSHInPart part = getMshInPart(metadataAttachmentName, inMail.getMessageId(), soapPartMime, f);
        mshInParts.add(part);
    }
}
