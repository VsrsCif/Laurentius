package si.vsrs.cif.filing.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gen.si.vsrs.cif.validator.model.PdfAModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import si.laurentius.commons.utils.SEDLogger;
import si.vsrs.cif.filing.enums.EFCError;

import javax.ejb.Stateless;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import static si.vsrs.cif.filing.utils.ExceptionUtils.throwFault;

@Stateless
public class PDFValidationService {
    public static SEDLogger LOG = new SEDLogger(PDFValidationService.class);

    private static final ThreadLocal<ObjectMapper> JSON_MAPPER = ThreadLocal.withInitial(() -> {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        JsonDeserializer<OffsetDateTime> deserializer = new PDFAValidationDateDeserializer();
        module.addDeserializer(OffsetDateTime.class, deserializer);
        mapper.registerModule(module);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    });


    public void validatePDF(File pdfFilePath, String name, String validationUrl, String validationAplId, boolean includeReport, String conversationId, String messageId, String senderId) {
        long startTime = Calendar.getInstance().getTimeInMillis();
        HttpResponse response = null;
        try {
            response = executePDFValidation(pdfFilePath, validationUrl, validationAplId, includeReport);
        } catch (IOException e) {
            LOG.logError("Connection error", e);
            throwFault(messageId, EFCError.SERVER_ERROR, conversationId, senderId, name, ExceptionUtils.getRootCauseMessage(e));
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            response.getEntity().writeTo(bos);
        } catch (IOException e) {
            LOG.logError("Read URL Connection error", e);
            throwFault(messageId, EFCError.SERVER_ERROR, conversationId, senderId, name, ExceptionUtils.getRootCauseMessage(e));
        }
        LOG.log("Validation response in [" + (Calendar.getInstance().getTimeInMillis() - startTime) + "] ms.");

        PDFAValidationResponse result = null;
        try {
            result = JSON_MAPPER.get().readValue(bos.toByteArray(), PDFAValidationResponse.class);
        } catch (IOException e) {
            throwFault(messageId, EFCError.SERVER_ERROR, conversationId, senderId, name, ExceptionUtils.getRootCauseMessage(e));
        }

        if (includeReport) {
            logResponse(bos, pdfFilePath);
        }

        if (result.getData().getValidationResult() != PdfAModel.ValidationResultEnum.VALID) {
            throwFault(messageId, EFCError.INVALID_PDF, conversationId, senderId, name, includeReport ? result.getData().getValidationReport() : "Invalid PDF");
        }
        LOG.log("Document: ["+name+"] is VALID!");
    }


    public HttpResponse executePDFValidation(File pdfFilePath, String validationUrl, String applicationId, boolean includeReport) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        String urlPath = StringUtils.appendIfMissing( validationUrl,"/") + "api/v1/pdfa/validator?applicationId=" + applicationId + "&includeReport=" + Boolean.toString(includeReport);
        System.out.println(urlPath);
        HttpPost httpPost = new HttpPost(validationUrl + "/api/v1/pdfa/validator?applicationId=" + applicationId + "&includeReport=" + Boolean.toString(includeReport));

        FileBody uploadFilePart = new FileBody(pdfFilePath);
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("file", uploadFilePart);
        httpPost.setEntity(reqEntity);
        return httpclient.execute(httpPost);

    }

    public void logResponse(ByteArrayOutputStream bos, File pdfFilePath) {
        LOG.log("---------------------------------------------------------------------------------------------");
        LOG.log("Log PDF Response for file: [" + pdfFilePath.getAbsolutePath() + "]");
        LOG.log("---------------------------------------------------------------------------------------------");
        LOG.log(bos.toString());
    }

    static class PDFAValidationDateDeserializer extends JsonDeserializer<OffsetDateTime> {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        @Override
        public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            OffsetDateTime dateTime;
            try {
                dateTime = LocalDateTime.parse(parser.getText(), this.formatter).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            } catch (java.time.format.DateTimeParseException exc) {
                dateTime = LocalDateTime.parse(parser.getText(), this.formatter1).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            }
            return dateTime;
        }
    }
}