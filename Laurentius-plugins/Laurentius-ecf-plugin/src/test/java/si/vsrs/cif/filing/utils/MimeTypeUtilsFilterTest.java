package si.vsrs.cif.filing.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.msh.inbox.payload.MSHInPart;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static si.laurentius.commons.enums.MimeValue.*;

@RunWith(Parameterized.class)
public class MimeTypeUtilsFilterTest {
    private static final String METADATA_NAME= "metadata";

    private static final MSHInPart[] TEST_LIST_MULTIPLE_XML = new MSHInPart[]{
            createMSHInPartWithMimeType(MIME_PDF.getMimeType(), "name-pdf-001"),
            createMSHInPartWithMimeType(MIME_PDF.getMimeType(), "name-pdf-002"),
            createMSHInPartWithMimeType(MIME_PDF.getMimeType(), "name-pdf-003"),
            createMSHInPartWithMimeType(MIME_XML.getMimeType(), "name-xml-001"),
            createMSHInPartWithMimeType(MIME_XML1.getMimeType(), "name-xml-002"),
            createMSHInPartWithMimeType(MIME_ZIP.getMimeType(), "name-zip-001"),
            createMSHInPartWithMimeType(MIME_ZIP.getMimeType(), "name-zip-002"),
            createMSHInPartWithMimeType(MIME_XML1.getMimeType(), METADATA_NAME)
    };

    private static final MSHInPart[] TEST_LIST_SINGLE_XML =  new MSHInPart[]{
            createMSHInPartWithMimeType(MIME_PDF.getMimeType(), "name-pdf-001"),
            createMSHInPartWithMimeType(MIME_PDF.getMimeType(), "name-pdf-002"),
            createMSHInPartWithMimeType(MIME_PDF.getMimeType(), "name-pdf-003"),
            createMSHInPartWithMimeType(MIME_PDF.getMimeType(), "name-pdf-004"),
            createMSHInPartWithMimeType(MIME_XML.getMimeType(), "name-xml-001"),
            createMSHInPartWithMimeType(MIME_ZIP.getMimeType(), "name-zip-001"),
            createMSHInPartWithMimeType(MIME_ZIP.getMimeType(), "name-zip-002")
    };

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Get all pdf parts", new MimeValue[]{MIME_PDF}, TEST_LIST_SINGLE_XML, null, false,"name-pdf-001,name-pdf-002,name-pdf-003,name-pdf-004" },
                {"Get all PDF and XML parts", new MimeValue[]{MIME_PDF,MIME_XML,MIME_XML1}, TEST_LIST_MULTIPLE_XML, null, false,"name-pdf-001,name-pdf-002,name-pdf-003,name-pdf-004name-xml-001,name-xml-002,"+METADATA_NAME },
                {"Get only metadata", new MimeValue[]{MIME_PDF,MIME_XML,MIME_XML1}, TEST_LIST_MULTIPLE_XML, METADATA_NAME, true, METADATA_NAME },
                {"Get metadata: only one XML", new MimeValue[]{MIME_XML,MIME_XML1}, TEST_LIST_SINGLE_XML, METADATA_NAME, false, "name-xml-001" },
        });
    }

    // test parameters
    @Parameterized.Parameter
    public String testName;
    @Parameterized.Parameter(1)
    public MimeValue[] filterMimeTypes;
    @Parameterized.Parameter(2)
    public MSHInPart[] testParts;
    @Parameterized.Parameter(3)
    public String fileName;
    @Parameterized.Parameter(4)
    public boolean strictValidation;
    @Parameterized.Parameter(5)
    public String expectedList;

    @Test
    public void filterPayloadsByMimeTypesAndNameTest() {
        System.out.println("filterPayloadsByMimeTypesAndNameTest: [" + testName + "]");
        String[] expectedPartList = expectedList.split(",");
        List<MSHInPart> result = MimeTypeUtils.filterPayloadsByMimeTypesAndName(Arrays.asList(testParts),fileName, strictValidation, Arrays.asList(filterMimeTypes ));
        assertEquals(expectedPartList.length, result.size());

    }


    private static MSHInPart createMSHInPartWithMimeType(String mimetype, String name) {
        MSHInPart part = new MSHInPart();
        part.setMimeType(mimetype);
        part.setName(name);
        return part;
    }
}