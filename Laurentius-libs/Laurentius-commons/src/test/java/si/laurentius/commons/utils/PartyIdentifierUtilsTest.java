package si.laurentius.commons.utils;

import org.junit.Assert;
import org.junit.Test;
import si.laurentius.commons.SEDSystemProperties;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PartyIdentifierUtilsTest {
    @Test
    public void isDomainMatchingPartyDomains_ReturnsTrue_WhenDomainMatches() {
        String[] pModePartyDomains = {"example.com", "test.com"};
        String messagePartyDomain = "test.com";
        assertTrue(PartyIdentifierUtils.isDomainMatchingPartyDomains(pModePartyDomains, messagePartyDomain));
    }

    @Test
    public void isDomainMatchingPartyDomains_ReturnsFalse_WhenDomainDoesNotMatch() {
        String[] pModePartyDomains = {"example.com", "test.com"};
        String messagePartyDomain = "nonexistent.com";
        assertFalse(PartyIdentifierUtils.isDomainMatchingPartyDomains(pModePartyDomains, messagePartyDomain));
    }

    @Test
    public void isDomainMatchingPartyDomains_ReturnsFalse_WhenMessageDomainIsNull() {
        String[] pModePartyDomains = {"example.com", "test.com"};
        assertFalse(PartyIdentifierUtils.isDomainMatchingPartyDomains(pModePartyDomains, null));
    }

    @Test
    public void isDomainMatchingPartyDomains_IgnoresCase_WhenMatchingDomains() {
        String[] pModePartyDomains = {"Example.com", "Test.com"};
        String messagePartyDomain = "test.COM";
        assertTrue(PartyIdentifierUtils.isDomainMatchingPartyDomains(pModePartyDomains, messagePartyDomain));
    }

    @Test
    public void getLocalDomains_ReturnsEmptyArray_WhenSystemPropertyIsNotSet() {
        System.clearProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN);
        String[] result = PartyIdentifierUtils.getLocalDomains();
        Assert.assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void getLocalDomains_ReturnsArrayOfDomains_WhenSystemPropertyIsSet() {
        System.setProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN, " domain1.com , domain2.com ");
        String[] result = PartyIdentifierUtils.getLocalDomains();
        assertArrayEquals(new String[]{"domain1.com", "domain2.com"}, result);
    }

    @Test
    public void convertToArray_ReturnsEmptyArray_WhenInputIsNull() {
        String[] result = PartyIdentifierUtils.convertToArray(null);
        Assert.assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void convertToArray_ReturnsArrayOfTrimmedDomains_WhenInputIsValid() {
        String[] result = PartyIdentifierUtils.convertToArray(" domain1.com , domain2.com ");
        assertArrayEquals(new String[]{"domain1.com", "domain2.com"}, result);
    }

    @Test
    public void getFirstDomain_ReturnsNull_WhenInputIsNull() {
        String result = PartyIdentifierUtils.getFirstDomain(null);
        Assert.assertNull(result);
    }

    @Test
    public void getFirstDomain_ReturnsFirstDomain_WhenInputIsValidWithSpaces() {
        String result = PartyIdentifierUtils.getFirstDomain( " domain1.com , domain2.com");
        assertEquals("domain1.com", result);
    }
}