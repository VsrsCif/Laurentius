/*
 * Copyright 2018, Supreme Court Republic of Slovenia
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
package si.vsrs.cif.filing;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.pmode.enums.SecSignatureAlgorithm;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.lce.DigestMethodCode;
import si.laurentius.lce.DigestUtils;
import si.laurentius.lce.sign.xml.XMLSignatureUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interceptor.MailInterceptorPropertyDef;
import si.laurentius.plugin.interfaces.PropertyListType;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;
import si.sodisce.common.dokument.v2.DokumentType;
import si.sodisce.splosnavloga.v2.DokumentComplexType;
import si.sodisce.splosnavloga.v2.SplosnaVloga;
import si.vsrs.cif.filing.enums.ECFAction;
import si.vsrs.cif.filing.enums.ECFService;
import si.vsrs.cif.filing.enums.EFCError;
import si.vsrs.cif.filing.exception.ECFFault;
import si.vsrs.cif.filing.lookups.ECFLookups;
import si.vsrs.cif.filing.lookups.data.CourtType;
import si.vsrs.cif.filing.lookups.data.FieldOfLawType;
import si.vsrs.cif.filing.lookups.data.RegisterType;
import si.vsrs.cif.filing.services.PDFValidationService;
import si.vsrs.cif.filing.services.TimeStampServiceImpl;
import si.vsrs.cif.filing.utils.MimeTypeUtils;
import si.vsrs.cif.filing.utils.SchemaValidator;

import javax.ejb.*;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;
import static si.vsrs.cif.filing.enums.EFCError.*;
import static si.vsrs.cif.filing.utils.ExceptionUtils.throwFault;

/**
 * @author Joze Rihtarsic
 * @since 2.0
 * <p>
 * This is an example of the Slovenian Court e-filing plugin "eOdlozisce".
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ECFInInterceptor implements SoapInterceptorInterface {

    private static final SEDLogger LOG = new SEDLogger(ECFInInterceptor.class);
    protected static final String PLUGIN_NAME = "eOdlozisce prestreznik";
    protected static final String PLUGIN_DESCRIPTION = "eOdložišče prestreznik: Namen dodatka je preverjanje tehnične ustreznosti dohodne vloge.";

    protected static final String SIGNATURE_ALGORITHM = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    protected static final DigestMethodCode SIGNATURE_REFERENCE_DIGEST = DigestMethodCode.SHA256;
    protected static final String SIGNATURE_REASON = "Prispela vloga";

    public static final List<MimeValue> DEFAULT_ALLOWED_MIMETYPES = Arrays.asList(MimeValue.MIME_XML, MimeValue.MIME_XML1, MimeValue.MIME_PDF);
    public static final String DEFAULT_OPRST_REGEXP = "^(\\S*\\s+)?(?<vpisnik>\\S*)\\s+(\\d*\\/\\d{2,4})$";
    public static final String OPRST_REGEXP_VPISNIK = "vpisnik";

    public static final String KEY_PAYLOAD_METADATA_NAME = "ecf.payload.metadata.name";
    public static final String KEY_METADAT_OPRST_REGEXP = "ecf.payload.metadata.oprst.regexp";
    public static final String KEY_PAYLOAD_PDF_VALIDATION_URL = "ecf.service.pdf-validation.url";
    public static final String KEY_PAYLOAD_PDF_VALIDATION_APPL_ID = "ecf.service.pdf-validation.aplik-id";
    public static final String KEY_PAYLOAD_PDF_VALIDATION_REPORT = "ecf.service.pdf-validation.report.return";

    public static final String KEY_SIGNATURE_KEY_ALIAS = "ecf.sign.key.alias";
    public static final String KEY_SIGNATURE_MANDATORY = "ecf.signature.mandatory";

    public static final String KEY_TIMESTAMP_URL = "ecf.service.timestamp.url";
    public static final String KEY_TIMESTAMP_TIMEOUT = "ecf.service.timestamp.timeout_in_ms";


    public static final String KEY_PAYLOAD_MIMETYPES = "ecf.payload.mimetypes";
    public static final String KEY_PAYLOAD_DIGEST_ENCODING = "ecf.payload.digest.encoding";
    public static final String KEY_PAYLOAD_DIGEST_ALGORITHM = "ecf.payload.digest.algorithm";


    MailInterceptorDef mailIntcDef = null;
    ECFLookups lookups = new ECFLookups();
    SchemaValidator schemaValidator = new SchemaValidator();
    XMLSignatureUtils signatureUtils = new XMLSignatureUtils();


    @EJB
    PDFValidationService pdfValidationService;
    @EJB
    TimeStampServiceImpl timeStampService;
    @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
    SEDCertStoreInterface mCertBean;


    @Override
    public MailInterceptorDef getDefinition() {
        if (mailIntcDef != null) {
            LOG.log("ECFInInterceptor plugin description already generated!");
            return mailIntcDef;
        }
        mailIntcDef = new MailInterceptorDef();
        mailIntcDef.setType(ECFInInterceptor.class.getSimpleName());
        mailIntcDef.setName(PLUGIN_NAME);
        mailIntcDef.setDescription(PLUGIN_DESCRIPTION);


        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_PAYLOAD_METADATA_NAME, "SplosnaVloga", "Naziv priponke, ki vsebuje obvezne meta-podatke pošiljke. ", false,
                PropertyType.String.getType(), null, null));

        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_PAYLOAD_PDF_VALIDATION_URL, "http://localhost:9085/", "URL sevisa za preverjanje ustreznosti PDF vsebin.", false,
                PropertyType.String.getType(), null, null));
        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_PAYLOAD_PDF_VALIDATION_APPL_ID, "TEST", "Vhodni parameter 'applicationId' za preverjanje ustreznosti PDF vsebin.", false,
                PropertyType.String.getType(), null, null));

        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_PAYLOAD_PDF_VALIDATION_REPORT, "false", "Vhodni parameter 'includeReport' za preverjanje ustreznosti PDF vsebin. Porocilo se izpise log datoteko!", false,
                PropertyType.Boolean.getType(), null, null));

        String defaultMimeTypes = DEFAULT_ALLOWED_MIMETYPES.stream()
                .map(mimeValue -> mimeValue.getMimeType()).collect(Collectors.joining(","));
        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_PAYLOAD_MIMETYPES, defaultMimeTypes, "Seznam dovoljenih mime tipov priponk ločenih z vejico. Primer: [" + defaultMimeTypes + "])", false,
                PropertyType.String.getType(), null, null));

        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_METADAT_OPRST_REGEXP, DEFAULT_OPRST_REGEXP, "'RegExp' za preverjanje opravilne stevilke in lociranja podatka za vpisnik! ", false,
                PropertyType.String.getType(), null, null));
        // hash
        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_PAYLOAD_DIGEST_ENCODING, "base64", "Nacin kodiranja zgostivene vrednosti: moznosti: hex,base64", true,
                PropertyType.List.getType(), null, "hex,base64"));
        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_PAYLOAD_DIGEST_ALGORITHM, "SHA256", "Dovoljen algoritem za izracun zgostivene vrednosti: SHA-1,SHA-256,SHA-512", true,
                PropertyType.List.getType(), null, "SHA1,SHA256,SHA512"));

        //
        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_TIMESTAMP_URL, "", "URL naslov za casovno zigosanje.", false,
                PropertyType.String.getType(), null, null));
        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_TIMESTAMP_TIMEOUT, "10000", "Timeout za klic casovneg zigosanja v ms.", false,
                PropertyType.Integer.getType(), null, null));

        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_SIGNATURE_MANDATORY, "false", "ecf.signature.mandatory.", true,
                PropertyType.Boolean.getType(), null, null));

        mailIntcDef.getMailInterceptorPropertyDeves().add(createProperty(
                KEY_SIGNATURE_KEY_ALIAS, "", "Alias kljuca za podpis vloge.", false,
                PropertyType.List.getType(), null, PropertyListType.KeystoreCertKeys.getType()));

        return mailIntcDef;
    }

    protected MailInterceptorPropertyDef createProperty(String key, String defValue,
                                                        String desc, boolean mandatory, String type, String valFormat,
                                                        String valList) {
        MailInterceptorPropertyDef ttp = new MailInterceptorPropertyDef();
        ttp.setKey(key);
        ttp.setDefValue(defValue);
        ttp.setDescription(desc);
        ttp.setMandatory(mandatory);
        ttp.setType(type);
        ttp.setValueFormat(valFormat);
        ttp.setValueList(valList);
        return ttp;
    }


    /**
     * @param msg
     * @param contextProperties
     */
    @Override
    public boolean handleMessage(SoapMessage msg, Properties contextProperties) {
        long l = LOG.logStart();
        boolean isBackChannel = SoapUtils.isRequestMessage(msg);
        MSHInMail mInMail = SoapUtils.getMSHInMail(msg);

        if (isBackChannel || mInMail == null) {
            // ignore isBackChannel processing
            LOG.formatedDebug("Skip backChannel processing!");
            return true;
        }

        if (mInMail == null) {
            throwFault("", MISSING_USER_MESSAGE);
        }
        // get message data
        String conversationId = mInMail.getConversationId();
        String messageId = mInMail.getMessageId();
        String senderId = mInMail.getSenderEBox();
        // get message data
        basicMessageValidation(mInMail, conversationId, messageId, senderId);

        List<MSHInPart> payloads = mInMail.getMSHInPayload().getMSHInParts();
        // filter all non mail sources
        List<MSHInPart> mailPayloads = payloads.stream()
                .filter(mshInPart -> StringUtils.equals(SEDMailPartSource.MAIL.getValue(), mshInPart.getSource()))
                .collect(Collectors.toList());

        // extract metadata and "mail content"
        String payloadName = getPropertyValue(KEY_PAYLOAD_METADATA_NAME, contextProperties);
        MSHInPart metadataPart = getMetadataPayload(mailPayloads, payloadName, conversationId, messageId, senderId);

        // validate if payloads exits and if hashes between metadata and mail payloads matches
        List<MSHInPart> mailContentPayloads = validatePayloads(metadataPart, mailPayloads, conversationId, messageId, senderId, contextProperties);
        // validate pdf payloads
        validatePDFPayloads(mailContentPayloads, conversationId, messageId, senderId, contextProperties);
        //sign and timestamp
        signAndTimestampMetadata(mInMail, metadataPart,conversationId, messageId, senderId, contextProperties);

        return true;
    }

    public void signAndTimestampMetadata(MSHInMail inMail, MSHInPart metadataPart, String conversationId, String messageId, String senderId, Properties contextProperties) {
        String signatureAlias = getPropertyValue(KEY_SIGNATURE_KEY_ALIAS, contextProperties);
        boolean signatureMandatory = Boolean.parseBoolean(getPropertyValue(KEY_SIGNATURE_MANDATORY, contextProperties));
        String timestampURL = getPropertyValue(KEY_TIMESTAMP_URL, contextProperties);
        Integer timestampTimeout = new Integer(getPropertyValue(KEY_TIMESTAMP_TIMEOUT, contextProperties));

        File fileXML = StorageUtils.getFile(metadataPart.getFilepath());

        Document document = null;
        try {
            document = timeStampService.parseXMLFile(fileXML);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LOG.logError(e.getMessage(), e);
            throwFault(messageId, EFCError.TIMESTAMP_ERROR, conversationId, senderId, ExceptionUtils.getRootCauseMessage(e));
        }

        Node signature = timeStampService.getLastSignatureNode(document);
        boolean documentChanged = false;
        if (signature == null) {
            if (signatureMandatory) {
                throwFault(messageId, EFCError.MISSING_SIGNATURE, conversationId, senderId);
            }
            if (StringUtils.isBlank(signatureAlias)){
                LOG.logWarn("Can not sign usigned incoming mail SplosnaVloga, because alisa does not exists!", null);
                return;
            }

            List<String> signatureIds = new ArrayList<>();
            try {
                PrivateKey privateKey = mCertBean.getPrivateKeyForAlias(signatureAlias);
                X509Certificate certificate = mCertBean.getX509CertForAlias(signatureAlias);
                KeyStore.PrivateKeyEntry key = new KeyStore.PrivateKeyEntry(privateKey, new Certificate[]{certificate});
                signatureUtils.createXAdESEnvelopedSignature(key, document.getDocumentElement(), signatureIds, SIGNATURE_REFERENCE_DIGEST, SIGNATURE_ALGORITHM, SIGNATURE_REASON);
                documentChanged = true;
            } catch (SEDSecurityException exception) {
                throwFault(messageId, SERVER_ERROR, conversationId, senderId, ExceptionUtils.getRootCauseMessage(exception));
            }
        }

        LOG.log("Timestamp url: ["+timestampURL+"]");
        if (isNotEmpty(timestampURL) && !timeStampService.hasTimestampNode(document)) {
            LOG.log("Execute timestamp on url: ["+timestampURL+"]");
            timeStampService.timeStampXmlDocument(document, timestampURL, timestampTimeout, conversationId, messageId, senderId);
            documentChanged = true;
        }
        if (documentChanged) {
            try {
                createNewSplosnaVlogaPart(document, metadataPart, inMail);
            } catch (FileNotFoundException | StorageException exception) {
                throwFault(messageId, SERVER_ERROR, conversationId, senderId, ExceptionUtils.getRootCauseMessage(exception));
            }
        }
    }

    /**
     * Method creates new mail part with signed/timestamped SplosnaVloga
     *
     * @param document
     * @param metadataPart
     * @param inMail
     * @throws StorageException
     * @throws FileNotFoundException
     */
    public void createNewSplosnaVlogaPart(Document document, MSHInPart metadataPart, MSHInMail inMail) throws StorageException, FileNotFoundException {

        File newFileName = StorageUtils.getNewStorageFile("xml","ecf-vloga");
        XMLUtils.serialize(document, true, newFileName);

        MSHInPart miNewDoc = new MSHInPart();
        miNewDoc.setIsSent(Boolean.FALSE);
        miNewDoc.setIsReceived(Boolean.FALSE);
        miNewDoc.setGeneratedFromPartId(metadataPart.getId());

        miNewDoc.setSource(getDefinition().getType());
        miNewDoc.setDescription("New: " + metadataPart.getDescription());
        miNewDoc.setEbmsId(metadataPart.getEbmsId() + "-dec");
        miNewDoc.setEncoding(metadataPart.getEncoding());
        miNewDoc.setFilename("ecf-"+metadataPart.getFilename());
        miNewDoc.setMimeType(metadataPart.getMimeType());
        miNewDoc.setName("ecf-"+metadataPart.getName());
        miNewDoc.setType(metadataPart.getType());
        miNewDoc.setIsEncrypted(Boolean.FALSE);

        miNewDoc.setSha256Value(DigestUtils.getBase64Sha256Digest(newFileName));
        miNewDoc.setSize(BigInteger.valueOf(newFileName.length()));
        miNewDoc.setFilepath(StorageUtils.getRelativePath(newFileName));
        inMail.getMSHInPayload().getMSHInParts().add(miNewDoc);
    }

    /**
     * Validate service, action and payloads must not be empty!
     *
     * @param mInMail
     * @throws ECFFault -
     */
    public void basicMessageValidation(MSHInMail mInMail, String conversationId, String messageId, String senderId) throws ECFFault {

        // currently only CourtFiling is the only service
        if (!ECFService.CourtFiling.getService().equals(mInMail.getService())) {
            throwFault(messageId, INVALID_SERVICE, conversationId, senderId, mInMail.getService(), ECFService.CourtFiling.getService());
        }

        // currently only ServeFiling is the only action
        if (!ECFAction.ServeFiling.getValue().equals(mInMail.getAction())) {
            throwFault(messageId, INVALID_ACTION, conversationId, senderId, mInMail.getAction(), ECFAction.ServeFiling.getValue());
        }

        // message must have at least one payload!
        if (mInMail.getMSHInPayload() == null
                || mInMail.getMSHInPayload().getMSHInParts() == null
                || mInMail.getMSHInPayload().getMSHInParts().isEmpty()) {
            throwFault(messageId, MISSING_PAYLOAD, conversationId, senderId);
        }
    }

    /**
     * Method validates payloads The payloads must have one XML with name SplosnaVloga  and returns "mail" content parts list!
     *
     * @param metadataPart
     * @param mailPayloads
     * @param messageId
     * @param conversationId
     * @return mail content parts
     */
    public List<MSHInPart> validatePayloads(MSHInPart metadataPart, List<MSHInPart> mailPayloads, String conversationId, String messageId, String senderId, Properties contextProperties) {
        // settings
        String allowedMimeTypes = getPropertyValue(KEY_PAYLOAD_MIMETYPES, contextProperties);


        List<MSHInPart> contentPayloads = mailPayloads.stream().filter(mshInPart -> mshInPart != metadataPart).collect(Collectors.toList());

        // test message payload (skip the metadata!)
        if (StringUtils.isNotBlank(allowedMimeTypes)) {
            List<MimeValue> documentMimeTypes = MimeTypeUtils.convertStringListToMimeList(allowedMimeTypes);
            // check payload mimetypes
            Optional<MSHInPart> wrongMimeType = contentPayloads.stream().filter(mshInPart -> !MimeTypeUtils.matchMimeTypes(mshInPart, documentMimeTypes)).findFirst();
            if (wrongMimeType.isPresent()) {
                throwFault(messageId, INVALID_PAYLOAD_MIMETYPE, conversationId, senderId, wrongMimeType.get().getMimeType());
            }
        }
        validateMetadata(metadataPart, contentPayloads, conversationId, messageId, senderId, contextProperties);
        return contentPayloads;
    }

    public void validatePDFPayloads(List<MSHInPart> contentPayloads, String conversationId, String messageId, String senderId, Properties contextProperties) {
        String validationUrl = getPropertyValue(KEY_PAYLOAD_PDF_VALIDATION_URL, contextProperties);
        String validationApplicationID = getPropertyValue(KEY_PAYLOAD_PDF_VALIDATION_APPL_ID, contextProperties);
        boolean returnReport = Boolean.parseBoolean(getPropertyValue(KEY_PAYLOAD_PDF_VALIDATION_REPORT, contextProperties));
        LOG.log("Validate PDFS using the URL [" + validationUrl + "], AppId: [" + validationApplicationID + "]");
        // validate pdf parts
        List<MSHInPart> pdfParts = MimeTypeUtils.filterPayloadsByMimeTypes(contentPayloads, Arrays.asList(MimeValue.MIME_PDF));
        for (MSHInPart mip : pdfParts) {
            File fPDF = StorageUtils.getFile(mip.getFilepath());
            pdfValidationService.validatePDF(fPDF, mip.getName(), validationUrl, validationApplicationID, returnReport, conversationId, messageId, senderId);
        }
    }

    public String getPropertyValue(String key, Properties contextProperties) {
        return trim(contextProperties.getProperty(key, getDefaultPropertyValue(key)));
    }

    public String getDefaultPropertyValue(String key) {
        return getDefinition().getMailInterceptorPropertyDeves().stream()
                .filter(prop -> StringUtils.equalsIgnoreCase(key, prop.getKey()))
                .map(prop -> prop.getDefValue()).findAny().orElse(null);
    }

    /**
     * Method returns message metadata payload. If there is only one XML the payload name is not validated.
     * If there are more than one xml payloads, then it returns the one which has the given payload name.
     *
     * @param payloads
     * @param payloadName
     * @param conversationId
     * @param messageId
     * @param senderId
     * @return
     */
    public MSHInPart getMetadataPayload(List<MSHInPart> payloads, String payloadName, String conversationId, String messageId, String senderId) {
        List<MSHInPart> partMetaData = MimeTypeUtils.filterPayloadsByMimeTypesAndName(payloads, payloadName, false, Arrays.asList(MimeValue.MIME_XML, MimeValue.MIME_XML1));
        if (partMetaData.isEmpty()) {
            throwFault(messageId, MISSING_METADATA, conversationId, senderId, payloadName);
        }

        if (partMetaData.size() > 1) {
            throwFault(messageId, INVALID_METADATA_COUNT, conversationId, senderId, payloadName);
        }
        return partMetaData.get(0);
    }

    /**
     * Method validates metadata "SplosnaVloga":
     * - executes the schema validation
     *
     * @param conversationId
     * @param messageId
     * @param senderId
     * @param contextProperties
     */
    public void validateMetadata(MSHInPart metadataPart, List<MSHInPart> contentPayloads, String conversationId, String messageId, String senderId, Properties contextProperties) {
        File fVloga = StorageUtils.getFile(metadataPart.getFilepath());
        LOG.log("Validate XML : " + fVloga.getAbsolutePath());
        try {
            schemaValidator.validateXMLBySplosnaVloga(fVloga);
        } catch (IOException | SAXException e) {
            throwFault(messageId, INVALID_METADATA_SCHEMA, conversationId, senderId, ExceptionUtils.getRootCauseMessage(e));
        }
        SplosnaVloga splosnaVloga = null;
        try {
            splosnaVloga = (SplosnaVloga) XMLUtils.deserialize(fVloga, SplosnaVloga.class);
        } catch (JAXBException e) {
            throwFault(messageId, INVALID_METADATA_PARSE, conversationId, senderId, ExceptionUtils.getRootCauseMessage(e));
        }
        // validate sodiSif and OprSt/PravnoPodrocje
        validateSplosnaVlogaData(splosnaVloga, conversationId, messageId, senderId, contextProperties);

        //validate payloads hashes
        validateSplosnaVlogaPayloadHashes(splosnaVloga, contentPayloads, conversationId, messageId, senderId, contextProperties);
    }

    /**
     * Method validates hashes from the metadat and the message
     *
     * @param splosnaVloga
     * @param contentPayloads
     * @param conversationId
     * @param messageId
     * @param senderId
     * @param contextProperties
     */
    public void validateSplosnaVlogaPayloadHashes(SplosnaVloga splosnaVloga, List<MSHInPart> contentPayloads, String conversationId, String messageId, String senderId, Properties contextProperties) {
        String hashEncoding = getPropertyValue(KEY_PAYLOAD_DIGEST_ENCODING, contextProperties);
        String hashAlgorithm = getPropertyValue(KEY_PAYLOAD_DIGEST_ALGORITHM, contextProperties);
        DigestMethodCode digestMethod = DigestMethodCode.valueOf(hashAlgorithm);

        DokumentType mainDocument = null;
        List<String> vlogaContentHashes = new ArrayList<>();
        for (DokumentComplexType vlozeniDokument : splosnaVloga.getVloga().getVlozeniDokumentis()) {
            for (DokumentType dokumentType : vlozeniDokument.getSeznamDokumentovs()) {
                // test is vodilni
                if (dokumentType.isJeVodilni()) {
                    if (mainDocument != null) {
                        // only one vodilni is allowed
                        throwFault(messageId, INVALID_PAYLOAD_VODILNI_COUNT, conversationId, senderId);
                    }
                    mainDocument = dokumentType;
                }


                if (StringUtils.isBlank(dokumentType.getHashAlgoritem())) {
                    LOG.logWarn("Dokument hash algoritem ni podan za dokument: [" + dokumentType.getIme() + "]", null);
                } else {
                    if (!StringUtils.equalsIgnoreCase(digestMethod.getAlgorithmURI(), dokumentType.getDokumentHash())) {
                        throwFault(messageId, INVALID_PAYLOAD_HASH_ALGORITHM, conversationId, senderId, dokumentType.getDokumentHash(), digestMethod.getAlgorithmURI());
                    }
                }
                vlogaContentHashes.add(dokumentType.getDokumentHash());
            }
        }

        if (mainDocument == null) {
            // only one vodilni is allowed
            throwFault(messageId, MISSING_PAYLOAD_VODILNI_COUNT, conversationId, senderId);
        }

        // check if number of parts matches number of hashes
        if (contentPayloads.size() != vlogaContentHashes.size()) {
            throwFault(messageId, INVALID_PAYLOAD_COUNT, conversationId, senderId, contentPayloads.size() + "", vlogaContentHashes.size() + "");
        }
        // calculate hashes

        List<String> contentPayloadsHashes = new ArrayList<>();
        boolean hexEnc = StringUtils.equalsIgnoreCase("hex", hashEncoding);
        for (MSHInPart part : contentPayloads) {
            File fPart = StorageUtils.getFile(part.getFilepath());
            try {
                contentPayloadsHashes.add(hexEnc ? DigestUtils.getHexOldDigest(fPart, digestMethod.getJcaCode()) :
                        DigestUtils.getBase64Digest(fPart, digestMethod.getJcaCode()));
            } catch (IOException | NoSuchAlgorithmException e) {
                LOG.logError("Error occured while calculating diges", e);
                throwFault(messageId, SERVER_ERROR, conversationId, senderId, ExceptionUtils.getRootCauseMessage(e));
            }
        }

        // test digests values.
        for (String value : vlogaContentHashes) {
            if (!contentPayloadsHashes.contains(value)) {
                throwFault(messageId, INVALID_PAYLOAD_HASH, conversationId, senderId,
                        String.join(",", vlogaContentHashes), String.join(",", contentPayloadsHashes));
            }
        }

    }

    public void validateSplosnaVlogaData(SplosnaVloga splosnaVloga, String conversationId, String messageId, String senderId, Properties contextProperties) {
        String oprstRegExp = getPropertyValue(KEY_METADAT_OPRST_REGEXP, contextProperties);

        // validate sodisif
        Optional<CourtType> courtType = lookups.getCourtByCode(splosnaVloga.getVloga().getSodiSif());
        if (!courtType.isPresent()) {
            throwFault(messageId, INVALID_METADATA_COURT_TYPE, conversationId, senderId, splosnaVloga.getVloga().getSodiSif());
        }
        // validate oprst/pravno podrocje
        Optional<RegisterType> registerType;
        String opravilnaSt = splosnaVloga.getVloga().getOprSt();
        String pravnoPodrocjeSif = splosnaVloga.getVloga().getPravnoPodrocjeSif();
        if (StringUtils.isNotBlank(opravilnaSt)) {

            // parse vpisnik from oprSt and chek if is "valid"
            String vpisnik = getVpisnikFromOprst(opravilnaSt, oprstRegExp);

            if (StringUtils.isBlank(vpisnik)) {
                throwFault(messageId, INVALID_METADATA_OPRST, conversationId, senderId, "OprSt [" + opravilnaSt + "] does not match [" + oprstRegExp + "]!");
            }

            registerType = lookups.getRegisterByCode(vpisnik);
            if (!registerType.isPresent()) {
                throwFault(messageId, INVALID_METADATA_OPRST, conversationId, senderId, "Register part [" + vpisnik + "] from 'oprSt' [" + opravilnaSt + "] is invalid!");
            }
        } else {
            Optional<FieldOfLawType> flt = lookups.getFieldOfLawByCode(pravnoPodrocjeSif);
            if (!flt.isPresent()) {
                throwFault(messageId, INVALID_METADATA_FIELD_OF_LAW, conversationId, senderId, "Invalid data [" + pravnoPodrocjeSif + "]");
            }
            registerType = lookups.getRegisterById(flt.get().getDefRegisterType());
            if (!registerType.isPresent()) {
                throwFault(messageId, INVALID_METADATA_FIELD_OF_LAW, conversationId, senderId, "Miss-configuration [" + pravnoPodrocjeSif
                        + "] has not defined default register [" + flt.get().getDefRegisterType() + "]");
            }
        }
        // validate if register is allowed for the court
        List<String> registerCourts = registerType.get().getCourts();
        if (!registerCourts.isEmpty() && !registerCourts.contains(courtType.get().getCode())) {
            throwFault(messageId, INVALID_METADATA_OPRST, conversationId, senderId, "Wrong court code [" + courtType.get().getCode() + "] for 'oprSt' [" + opravilnaSt + "] or pravnoPodrocjeSif [" + pravnoPodrocjeSif + "]!");
        }
    }

    public String getVpisnikFromOprst(String opravilnaSt, String oprstRegExp) {
        if (StringUtils.isBlank(oprstRegExp)) {
            LOG.logWarn("Regular expression is not defined. Return [" + opravilnaSt + "]!", null);
            return opravilnaSt;
        }
        if (StringUtils.isBlank(opravilnaSt)) {
            LOG.logWarn("Null OprSt!", null);
            return opravilnaSt;
        }

        Pattern pattern = Pattern.compile(oprstRegExp);
        Matcher matcher = pattern.matcher(opravilnaSt);

        return matcher.matches() ? matcher.group(OPRST_REGEXP_VPISNIK) : null;
    }


    /**
     * @param t
     */
    @Override
    public void handleFault(SoapMessage t, Properties cp) {
        // ignore
    }


}
