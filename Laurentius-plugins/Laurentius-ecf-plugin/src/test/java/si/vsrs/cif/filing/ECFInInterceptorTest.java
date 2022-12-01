package si.vsrs.cif.filing;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.BeforeClass;
import org.junit.Test;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.sodisce.common.dokument.v2.DokumentType;
import si.sodisce.splosnavloga.v2.DokumentComplexType;
import si.sodisce.splosnavloga.v2.SplosnaVloga;
import si.sodisce.splosnavloga.v2.VlogaType;
import si.vsrs.cif.filing.enums.ECFAction;
import si.vsrs.cif.filing.enums.ECFService;
import si.vsrs.cif.filing.exception.ECFFault;
import si.vsrs.cif.filing.lookups.ECFLookups;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.*;

public class ECFInInterceptorTest {

    ECFInInterceptor testInstance = new ECFInInterceptor();
    private static final String TEST_FILE_PDF_01="test-pdf-01.pdf";
    private static final String TEST_FILE_PDF_02="test-pdf-02.pdf";

    @BeforeClass
    public static void beforeClass() throws Exception {
        // create storage files
        File testPdf1 = new File(StorageUtils.getStorageFolder(), TEST_FILE_PDF_01);
        File testPdf2 = new File(StorageUtils.getStorageFolder(), TEST_FILE_PDF_02);

        Files.copy(ECFLookups.class.getResourceAsStream("/examples/" + TEST_FILE_PDF_01),
                testPdf1.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(ECFLookups.class.getResourceAsStream("/examples/" + TEST_FILE_PDF_02),
                testPdf2.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }



    @Test
    public void getDefinition() {
        // When
        MailInterceptorDef result = testInstance.getDefinition();
        // Test
        assertNotNull(result);
        assertEquals(ECFInInterceptor.PLUGIN_NAME, result.getName());
        assertEquals(ECFInInterceptor.PLUGIN_DESCRIPTION, result.getDescription());
        assertEquals(ECFInInterceptor.class.getSimpleName(), result.getType());

        assertEquals(12, result.getMailInterceptorPropertyDeves().size());
    }

    @Test
    public void basicMessageValidationWrongService() {
        MSHInMail mInMail = createInMail("WrongService", ECFAction.ServeFiling.getValue());

        ECFFault result = assertThrows(ECFFault.class,
                () -> testInstance.basicMessageValidation(mInMail, mInMail.getConversationId(), mInMail.getMessageId(), mInMail.getSenderEBox()));
        MatcherAssert.assertThat(result.getSubMessage(), CoreMatchers.containsString("has invalid service: [WrongService] (expected:[CourtFiling])"));
        assertEquals(mInMail.getMessageId(), result.getRefToMessage());
    }

    @Test
    public void basicMessageValidationWrongAction() {
        MSHInMail mInMail = createInMail(ECFService.CourtFiling.getService(), "WrongAction");

        ECFFault result = assertThrows(ECFFault.class,
                () -> testInstance.basicMessageValidation(mInMail, mInMail.getConversationId(), mInMail.getMessageId(), mInMail.getSenderEBox()));
        MatcherAssert.assertThat(result.getSubMessage(), CoreMatchers.containsString("has invalid action: [WrongAction] (expected:[ServeFiling])"));
        assertEquals(mInMail.getMessageId(), result.getRefToMessage());
    }

    @Test
    public void basicMessageValidationMissingPayloads() {
        MSHInMail mInMail = createInMail(ECFService.CourtFiling.getService(), ECFAction.ServeFiling.getValue());
        mInMail.setMSHInPayload(null);

        ECFFault result = assertThrows(ECFFault.class,
                () -> testInstance.basicMessageValidation(mInMail, mInMail.getConversationId(), mInMail.getMessageId(), mInMail.getSenderEBox()));
        MatcherAssert.assertThat(result.getSubMessage(), CoreMatchers.containsString("does not have payload!"));
        assertEquals(mInMail.getMessageId(), result.getRefToMessage());
    }

    @Test
    public void basicMessageValidationOK() {
        MSHInMail mInMail = createInMail(ECFService.CourtFiling.getService(), ECFAction.ServeFiling.getValue());

        testInstance.basicMessageValidation(mInMail, mInMail.getConversationId(), mInMail.getMessageId(), mInMail.getSenderEBox());
        // no error should be thrown
    }

    @Test
    public void testDefaultRegExpVpisnikFromOprst() {
        String[][] testData = new String[][]{
                {"I PR 22/2022", ECFInInterceptor.DEFAULT_OPRST_REGEXP,  "PR", "22","2022"},
                {"PR 02/2022", ECFInInterceptor.DEFAULT_OPRST_REGEXP, "PR", "02","2022"},
                {"PR 22/22", ECFInInterceptor.DEFAULT_OPRST_REGEXP, "PR", "22","2022"},
                {"  PR 22/20", ECFInInterceptor.DEFAULT_OPRST_REGEXP, "PR", "22","2020"}
        };

        for (String[] test : testData) {
            CaseNumber result = testInstance.parseCaseNumber(test[0], test[1]);
            assertEquals(test[2], result.getRegisterCode());
            assertEquals(test[3], result.getNumber());
            assertEquals(test[4], result.getYear());
        }

    }

    @Test
    public void validateSplosnaVlogaDataOKByOprSt() {

        MSHInMail mshInMail = createMSHInMail();
        // vpisnik St and SodiSif must be in ECFLookup!
        Properties properties = getDefaultProperties();
        SplosnaVloga splosnaVloga = createSplosnaVloga("S03", "I St 2/2022", null);
        testInstance.validateSplosnaVlogaData(splosnaVloga, mshInMail, properties);
    }

    @Test
    public void validateSplosnaVlogaDataOKByPravnoPodrocje() {
        MSHInMail mshInMail = createMSHInMail();
        // vpisnik St and SodiSif must be in ECFLookup!
        Properties properties = getDefaultProperties();
        SplosnaVloga splosnaVloga = createSplosnaVloga("S03", null, "St");
        testInstance.validateSplosnaVlogaData(splosnaVloga, mshInMail, properties);
    }


    @Test
    public void validateSplosnaVlogaDataWrongSodiSif() {
        MSHInMail mshInMail = createMSHInMail();
        // vpisnik St and SodiSif must be in ECFLookup!
        Properties properties = getDefaultProperties();
        SplosnaVloga splosnaVloga = createSplosnaVloga("S01", "I St 2/2022", null);
        ECFFault result = assertThrows(ECFFault.class, () -> testInstance.validateSplosnaVlogaData(splosnaVloga, mshInMail, properties));

        MatcherAssert.assertThat(result.getSubMessage(), CoreMatchers.containsString("Wrong court code"));
    }

    @Test
    public void validateSplosnaVlogaDataWrongOprSt() {
        MSHInMail mshInMail = createMSHInMail();
        // vpisnik St and SodiSif must be in ECFLookup!
        Properties properties = getDefaultProperties();
        SplosnaVloga splosnaVloga = createSplosnaVloga("S01", "I NotExists 2/2022", null);
        ECFFault result = assertThrows(ECFFault.class, () -> testInstance.validateSplosnaVlogaData(splosnaVloga, mshInMail, properties));

        MatcherAssert.assertThat(result.getSubMessage(), CoreMatchers.containsString("is invalid"));
    }


    @Test
    public void validateSplosnaVlogaPayloadHashes() {
        String messageId = "messageId";
        String conversationId = "conversationId";
        String senderId = "senderId";
        // vpisnik St and SodiSif must be in ECFLookup!
        Properties properties = getDefaultProperties();
        List<MSHInPart> mshPartTypeList = Arrays.asList(createPart(MimeValue.MIME_PDF.getMimeType(), "pdf-part-001", TEST_FILE_PDF_01),
                createPart(MimeValue.MIME_PDF.getMimeType(), "pdf-part-002", TEST_FILE_PDF_02));

        SplosnaVloga splosnaVloga = createSplosnaVloga("S03", null, "St", mshPartTypeList);

        testInstance.validateSplosnaVlogaPayloadHashes(splosnaVloga, mshPartTypeList, conversationId, messageId, senderId, properties);
    }

    private static MSHInMail createInMail(String service, String action) {
        MSHInMail mInMail = new MSHInMail();
        mInMail.setService(service);
        mInMail.setAction(action);
        mInMail.setMessageId(UUID.randomUUID().toString());
        mInMail.setConversationId(UUID.randomUUID().toString());
        mInMail.setSenderEBox(UUID.randomUUID().toString());
        mInMail.setMSHInPayload(new MSHInPayload());
        mInMail.getMSHInPayload().getMSHInParts().add(createPart(MimeValue.MIME_XML.getMimeType(), "xml-part"));
        mInMail.getMSHInPayload().getMSHInParts().add(createPart(MimeValue.MIME_PDF.getMimeType(), "pdf-part"));
        return mInMail;
    }

    private static MSHInPart createPart(String mimeType, String name, String filename) {
        MSHInPart part = new MSHInPart();
        part.setMimeType(mimeType);
        part.setName(name);
        if (StringUtils.isBlank(filename)){
            return part;
        }
        // add part if filename exits
        part.setFilepath(filename);
        File filePath = StorageUtils.getFile(filename);
        if (filePath.exists()){
            part.setSha256Value(DigestUtils.getBase64Sha256Digest(filePath));
        }
        return part;
    }

    private static MSHInPart createPart(String mimeType, String name) {
        return createPart(mimeType, name, null);
    }

    private static SplosnaVloga createSplosnaVloga(String sodiSif, String opravilnaSt, String pravnoPodrocje) {
        return createSplosnaVloga(sodiSif, opravilnaSt, pravnoPodrocje,  null);
    }

    private static SplosnaVloga createSplosnaVloga(String sodiSif, String opravilnaSt, String pravnoPodrocje,  List<MSHInPart> mshPartTypeList) {
        SplosnaVloga splosnaVloga = new SplosnaVloga();
        splosnaVloga.setVloga(new VlogaType());
        splosnaVloga.getVloga().setSodiSif(sodiSif);
        splosnaVloga.getVloga().setOprSt(opravilnaSt);
        splosnaVloga.getVloga().setPravnoPodrocjeSif(pravnoPodrocje);

        if (mshPartTypeList == null){
            return splosnaVloga;
        }
        DokumentComplexType dokumentComplexType = new DokumentComplexType();
        for (int i=0; i< mshPartTypeList.size(); i++){
            MSHInPart part = mshPartTypeList.get(i);
            DokumentType doc = new DokumentType();
            // first is always "vodilni" :)
            doc.setJeVodilni(i==0);
            doc.setIme(part.getName());
            doc.setDokumentHash(part.getSha256Value());
            dokumentComplexType.getSeznamDokumentovs().add(doc);
        }
        splosnaVloga.getVloga().getVlozeniDokumentis().add(dokumentComplexType);


        return splosnaVloga;
    }

    public Properties getDefaultProperties() {
        Properties properties = new Properties();
        testInstance.getDefinition().getMailInterceptorPropertyDeves().stream().filter(propDef ->
                StringUtils.isNotBlank(propDef.getDefValue())).forEach(propDef -> properties.setProperty(propDef.getKey(), propDef.getDefValue()));
        return properties;
    }

    public MSHInMail createMSHInMail(){
        return createMSHInMail("messageId","conversationId","senderEBox");
    }

    public MSHInMail createMSHInMail(String messageId, String conversationId, String senderEBox){
        MSHInMail mshInMail= new MSHInMail();
        mshInMail.setMessageId(messageId);
        mshInMail.setConversationId(conversationId);
        mshInMail.setSenderEBox(senderEBox);
        return mshInMail;
    }
}