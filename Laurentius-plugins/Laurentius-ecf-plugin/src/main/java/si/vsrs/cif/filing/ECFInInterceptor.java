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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.msh.inbox.payload.IMPartProperty;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;
import si.vsrs.cif.filing.enums.ECFAction;
import si.vsrs.cif.filing.enums.ECFService;
import si.vsrs.cif.filing.exception.ECFFault;
import si.vsrs.cif.filing.exception.ECFFaultCode;
import si.vsrs.cif.filing.utils.PDFUtils;
import si.vsrs.cif.filing.utils.SchemaValidator;

import static java.lang.String.format;

/**
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ECFInInterceptor implements SoapInterceptorInterface {

    /**
     *
     */
    protected final SEDLogger LOG = new SEDLogger(ECFInInterceptor.class);

    StorageUtils storageUtils = new StorageUtils();
    PDFUtils mPDFUtils = new PDFUtils();
    SchemaValidator schemaValidator = new SchemaValidator();

    /**
     *
     */
    @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
    SEDDaoInterface mdao;

    @Override
    public MailInterceptorDef getDefinition() {
        MailInterceptorDef def = new MailInterceptorDef();
        def.setType(ECFInInterceptor.class.getSimpleName());
        def.setName(ECFInInterceptor.class.getSimpleName());
        def.setDescription(
                "eOdlozisce interceptor");
        return def;
    }

    /**
     * @param msg
     * @param cp
     */
    @Override
    public boolean handleMessage(SoapMessage msg, Properties cp) {
        long l = LOG.logStart();
        boolean isBackChannel = SoapUtils.isRequestMessage(msg);
        MSHInMail mInMail = SoapUtils.getMSHInMail(msg);

        if (isBackChannel || mInMail == null) {
            // ignore processing
            LOG.formatedDebug("Skip processing if the mail");
            return true;
        }
        // validate msg
        validateMessage(mInMail);

        String conversationId = mInMail.getConversationId();
        String messageId = mInMail.getMessageId();
        String senderId = mInMail.getSenderEBox();


        List<MSHInPart> payloads = mInMail.getMSHInPayload().getMSHInParts();
        validatePayloads(payloads, conversationId, messageId, senderId);
        // test if pdf is signed
        // test if payload is signed PDF
        // test if mail is PDF/a
        return true;
    }

    /**
     * Validate service, action and payload must not be empty
     *
     * @param mInMail
     * @throws ECFFault
     */
    public void validateMessage(MSHInMail mInMail) throws ECFFault {
        String conversationId = mInMail.getConversationId();
        String messageId = mInMail.getMessageId();
        String senderId = mInMail.getSenderEBox();
        if (!ECFService.CourtFiling.getService().equals(mInMail.getService())) {
            String msgError = format("Message ConversationId: %s from %s has wrong service: '%s' (expected: %s) for this plugin!",
                    conversationId, senderId, mInMail.getService(), ECFService.CourtFiling.getService());
            LOG.logWarn(msgError, null);
            throw new ECFFault(ECFFaultCode.Other, messageId,
                    msgError,
                    SoapFault.FAULT_CODE_SERVER);

        }

        if (!ECFAction.ServeFiling.getValue().equals(mInMail.getAction())) {
            String msgError = format("Message ConversationId: %s from %s has wrong action: '%s' (expected: %s) for this plugin!",
                    conversationId, senderId, mInMail.getAction(), ECFAction.ServeFiling.getValue());
            LOG.logWarn(msgError, null);
            throw new ECFFault(ECFFaultCode.Other, messageId,
                    msgError,
                    SoapFault.FAULT_CODE_SERVER);
        }

        if (mInMail.getMSHInPayload() == null || mInMail.getMSHInPayload().
                getMSHInParts().isEmpty()) {
            String msgError = format("Message ConversationId: %s from %s does not have payload!",
                    conversationId, senderId);
            LOG.logWarn(msgError, null);
            throw new ECFFault(ECFFaultCode.Other, messageId,
                    msgError,
                    SoapFault.FAULT_CODE_SERVER);
        }
    }

    /**
     * Method validates payloads The payloads must have one XML with name SplosnaVloga and pdfs must be an pdf/a
     *
     * @param payloads
     * @param messageId
     * @param conversationId
     */
    public void validatePayloads(List<MSHInPart> payloads, String conversationId, String messageId, String senderId) {
        List<MSHInPart> partMetaData = getPayloadsWithNameAndType(payloads, "SplosnaVloga", Arrays.asList(MimeValue.MIME_XML, MimeValue.MIME_XML1) );
        if (partMetaData.isEmpty()) {
            String msgError = format(
                    "Message ConversationId: %s from %s has missing XML payload with name SplosnaVloga!",
                    conversationId, senderId);
            throw new ECFFault(ECFFaultCode.Other, messageId,
                    msgError,
                    SoapFault.FAULT_CODE_SERVER);
        }

        if (partMetaData.size() > 1) {
            String msgError = format(
                    "Message ConversationId: %s from %s has more than one XML payload with name SplosnaVloga!",
                    conversationId, senderId);
            throw new ECFFault(ECFFaultCode.Other, messageId,
                    msgError,
                    SoapFault.FAULT_CODE_SERVER);
        }
        MSHInPart partVloga = partMetaData.get(0);


        File fVloga = StorageUtils.getFile(partVloga.getFilepath());
        boolean isSchemaValid = schemaValidator.isValid(fVloga);
        if (!isSchemaValid) {
            String msgError = format(
                    "Message ConversationId: %s from %s has invalid SplosnaVloga!",
                    conversationId, senderId);
            LOG.logWarn(msgError, null);
            throw new ECFFault(ECFFaultCode.Other, messageId,
                    msgError,
                    SoapFault.FAULT_CODE_SERVER);
        }
        // validate pdf parts
        List<MSHInPart> pdfParts = getPayloadsWithNameAndType(payloads, null, Arrays.asList(MimeValue.MIME_PDF) );
        for (MSHInPart mip : pdfParts) {
            if (SEDMailPartSource.MAIL.getValue().equals(mip.getSource())) {

                //--------------------------------------------------------
                // validate for pdf/a
                File fPDF = StorageUtils.getFile(mip.getFilepath());
                String res = mPDFUtils.testPDFA(fPDF);
                if (res != null) {
                    String msgError = format(
                            "Message ConversationId: %s from %s payload is not pdf/a compliant: %s!",
                            conversationId, senderId, res);
                    LOG.logWarn(msgError, null);
                    throw new ECFFault(ECFFaultCode.Other, messageId,
                            msgError,
                            SoapFault.FAULT_CODE_SERVER);
                }
            }
        }
    }


    public List<MSHInPart> getPayloadsWithNameAndType(List<MSHInPart> payloads, String name, List<MimeValue> mimeValues) {
        return payloads.stream().filter(part -> hasPartNameAndMimeTypes(part, name, mimeValues))
                .collect(Collectors.toList());
    }

    public boolean hasPartNameAndMimeTypes(MSHInPart part, String name, List<MimeValue> mimeValues) {
        if (StringUtils.isNotBlank(name) && !StringUtils.equalsIgnoreCase(name, part.getName())) {
            LOG.formatedDebug("Part [%s] with name [%s], does not have name [%s]", part.getEbmsId(), part.getName(), name);
            return false;
        }
        if (mimeValues != null && !mimeValues.isEmpty()) {
            MimeValue partMimeType = getMimeType(part.getMimeType());
            return mimeValues.contains(partMimeType);
        }
        return true;
    }


    public String getPartProperty(MSHInPart part, String propertytype) {
        if (part == null || part.getIMPartProperties().isEmpty()) {
            return null;
        }
        for (IMPartProperty p : part.getIMPartProperties()) {
            if (Objects.equals(p.getName(), propertytype)) {
                return p.getValue();
            }
        }
        return null;
    }

    /**
     * @param t
     */
    @Override
    public void handleFault(SoapMessage t, Properties cp) {
        // ignore
    }

    /**
     * Method returns MimeType Object for mimetype. If no mimetype is found MIME_BIN is returned-
     *
     * @param strMimeType
     * @return suffix
     */
    public static MimeValue getMimeType(String strMimeType) {
        MimeValue defaultValue = MimeValue.MIME_BIN;

        if (Utils.isEmptyString(strMimeType)) {
            return defaultValue;
        }
        Optional<MimeValue> value = Stream.of(MimeValue.values())
                .filter(vm -> vm.getDefForMime() && vm.getMimeType().equalsIgnoreCase(strMimeType)).findAny();
        return value.isPresent()?value.get():defaultValue;
    }


}
