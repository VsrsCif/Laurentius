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

@RunWith(Parameterized.class)
public class MimeTypeUtilsTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"application/pdf", new MimeValue[]{MimeValue.MIME_PDF}, "application/pdf", true},
                {"appliCation/PDF", new MimeValue[]{MimeValue.MIME_PDF}, " apPlication/PDF ", true},
                {" application/pdf  ", new MimeValue[]{MimeValue.MIME_PDF}, "application/xml", false},
                {"application/pdf,application/xml,application/octet-stream", new MimeValue[]{MimeValue.MIME_PDF, MimeValue.MIME_XML, MimeValue.MIME_BIN}, "application/PDF ", true},
                {" application/pdf, application/xml ,   application/octet-stream  ", new MimeValue[]{MimeValue.MIME_PDF, MimeValue.MIME_XML, MimeValue.MIME_BIN}, "image/x-ms-bmp", false},

        });
    }

    // test parameters
    @Parameterized.Parameter
    public String testString;
    @Parameterized.Parameter(1)
    public MimeValue[] expectedList;
    @Parameterized.Parameter(2)
    public String partMimeType;
    @Parameterized.Parameter(3)
    public boolean expectedPartTestResult;


    @Test
    public void convertStringListToMimeListTest() {
        System.out.println("convertStringListToMimeListTest: [" + testString + "], expected list: [" + Stream.of(expectedList).map(Enum::name).collect(Collectors.joining(",")) + "]");
        List<MimeValue> result = MimeTypeUtils.convertStringListToMimeList(testString);
        assertEquals(expectedList.length, result.size());
        for (int i = 0; i < expectedList.length; i++) {
            assertEquals(expectedList[i], result.get(i));
        }

    }

    @Test
    public void matchMimeTypesTest() {
        System.out.println("matchMimeTypesTest: [" + partMimeType + "], Match list: [" + Stream.of(expectedList).map(Enum::name).collect(Collectors.joining(",")) + "]");
        MSHInPart part = createMSHInPartWithMimeType(partMimeType);
        boolean result = MimeTypeUtils.matchMimeTypes(part, Arrays.asList(expectedList));

        assertEquals(expectedPartTestResult, result);
    }


    private static MSHInPart createMSHInPartWithMimeType(String mimetype) {
        MSHInPart part = new MSHInPart();
        part.setMimeType(mimetype);
        return part;
    }
}