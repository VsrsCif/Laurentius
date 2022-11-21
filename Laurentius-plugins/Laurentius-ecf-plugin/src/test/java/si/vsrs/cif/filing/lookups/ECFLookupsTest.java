package si.vsrs.cif.filing.lookups;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import si.laurentius.commons.SEDSystemProperties;
import si.vsrs.cif.filing.lookups.data.CourtType;
import si.vsrs.cif.filing.lookups.data.FieldOfLawType;
import si.vsrs.cif.filing.lookups.data.RegisterType;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

public class ECFLookupsTest {
    ECFLookups testInstance = new ECFLookups();

    @Before
    public void initTest() throws IOException {
        testInstance.initLookups();
    }
    @Test
    public void initLookupsTest() {

        // test court lookup
        assertFalse(testInstance.getCourtCodes().isEmpty());
        assertNotNull(testInstance.getCourtCodes().get(0).getName());
        assertNotNull(testInstance.getCourtCodes().get(0).getCode());
        // test register lookup
        assertFalse(testInstance.getRegisterTypes().isEmpty());
        assertNotNull(testInstance.getRegisterTypes().get(0).getId());
        assertNotNull(testInstance.getRegisterTypes().get(0).getCode());
        assertNotNull(testInstance.getRegisterTypes().get(0).getName());
        assertNotNull(testInstance.getRegisterTypes().get(0).getApplikCode());
        assertFalse(testInstance.getRegisterTypes().get(0).getCourts().isEmpty());
        assertNotNull(testInstance.getRegisterTypes().get(0).getCourts().get(0));
        // test fieldOfLaw
        assertFalse(testInstance.getFieldOfLawTypes().isEmpty());
        assertNotNull(testInstance.getFieldOfLawTypes().get(0).getCode());
        assertNotNull(testInstance.getFieldOfLawTypes().get(0).getName());
        assertNotNull(testInstance.getFieldOfLawTypes().get(0).getDefRegisterType());
    }

    @Test
    public void testGetCourtByCode() {
        // given
        String testcode = "S01";
        // when
        Optional<CourtType> result = testInstance.getCourtByCode(testcode);
        //then
        assertTrue(result.isPresent());
        assertEquals(testcode, result.get().getCode());
    }

    @Test
    public void testGetRegisterByCode() {
        // given
        String testcode = "St";
        // when
        Optional<RegisterType> result = testInstance.getRegisterByCode(testcode);
        //then
        assertTrue(result.isPresent());
        assertEquals(testcode, result.get().getCode());
    }

    @Test
    public void testGetRegisterById() {
        // given
        String testid = "4";
        // when
        Optional<RegisterType> result = testInstance.getRegisterById(testid);
        //then
        assertTrue(result.isPresent());
        assertEquals(testid, result.get().getId());
    }

    @Test
    public void testGetFieldOfLawByCode() {
        // given
        String testcode = "ZK";
        // when
        Optional<FieldOfLawType> result = testInstance.getFieldOfLawByCode(testcode);
        //then
        assertTrue(result.isPresent());
        assertEquals(testcode, result.get().getCode());
    }
}