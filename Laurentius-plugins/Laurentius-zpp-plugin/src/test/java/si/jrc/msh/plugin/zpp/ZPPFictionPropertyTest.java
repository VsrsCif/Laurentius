/*
 * Copyright 2025, Supreme Court Republic of Slovenia
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
package si.jrc.msh.plugin.zpp;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jrc.xml.DateAdapter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import si.jrc.msh.plugin.zpp.exception.ZPPException;
import si.jrc.msh.plugin.zpp.utils.ZPPUtils;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.property.MSHOutProperty;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.PartyIdentitySetType;

/**
 * Tests that the FictionNotification outbound message carries the
 * DeliveredByFiction property with the correct date value.
 * Exercises the actual processZPPFictionDelivery code path.
 */
public class ZPPFictionPropertyTest {

    private static final String TEST_SIGN_ALIAS = "test-alias";
    private static final String TEST_RECEIVER_BOX = "receiver@test.si";
    private static final String TEST_CERT_ALIAS = "receiver-cert";

    private SEDDaoInterface mockDB;
    private SEDCertStoreInterface mockCertBean;
    private PModeInterface mockPMode;
    private ZPPUtils mockZPPUtils;

    private PartyIdentitySet testPIS;

    @Before
    public void setUp() throws Exception {
        mockDB = mock(SEDDaoInterface.class);
        mockCertBean = mock(SEDCertStoreInterface.class);
        mockPMode = mock(PModeInterface.class);
        mockZPPUtils = mock(ZPPUtils.class);

        // Set up PartyIdentitySet with ExchangePartySecurity
        testPIS = new PartyIdentitySet();
        PartyIdentitySetType.ExchangePartySecurity eps =
                new PartyIdentitySetType.ExchangePartySecurity();
        eps.setSignatureCertAlias(TEST_CERT_ALIAS);
        testPIS.setExchangePartySecurity(eps);

        try {
            when(mockPMode.getPartyIdentitySetForSEDAddress(TEST_RECEIVER_BOX))
                    .thenReturn(testPIS);
        } catch (PModeException e) {
            // won't happen on a mock
        }

        // Mock cert store — return a dummy self-signed cert
        X509Certificate dummyCert = mock(X509Certificate.class);
        PrivateKey dummyPk = mock(PrivateKey.class);
        when(mockCertBean.getX509CertForAlias(anyString())).thenReturn(dummyCert);
        when(mockCertBean.getPrivateKeyForAlias(anyString())).thenReturn(dummyPk);

        // Mock ZPPUtils — return dummy parts
        MSHOutPart dummyPart = new MSHOutPart();
        when(mockZPPUtils.createSignedAdviceOfFictionNotification(
                any(MSHOutMail.class), any(PrivateKey.class), any(X509Certificate.class)))
                .thenReturn(dummyPart);
        when(mockZPPUtils.createSignedAdviceOfDeliveryFiction(
                any(MSHOutMail.class), any(PrivateKey.class), any(X509Certificate.class)))
                .thenReturn(mock(si.laurentius.msh.inbox.payload.MSHInPart.class));
        when(mockZPPUtils.createMSHOutPart(
                any(), any(), any(), any(PrivateKey.class), any(X509Certificate.class)))
                .thenReturn(dummyPart);
        when(mockZPPUtils.createMSHInPart(
                any(), any(), any(), any(PrivateKey.class), any(X509Certificate.class)))
                .thenReturn(mock(si.laurentius.msh.inbox.payload.MSHInPart.class));
        when(mockZPPUtils.getEncKeyFromOut(any(MSHOutMail.class)))
                .thenReturn(mock(java.security.Key.class));
        when(mockZPPUtils.createEncryptedKey(any(), any(), anyString(), anyString()))
                .thenReturn(dummyPart);
    }

    /**
     * Test that ZPPTaskFiction.processZPPFictionDelivery attaches the
     * DeliveredByFiction property to the FictionNotification mail.
     */
    @Test
    public void testZPPTaskFiction_addsDeliveredByFictionProperty() throws Exception {
        ZPPTaskFiction task = new ZPPTaskFiction();
        task.mDB = mockDB;
        task.mCertBean = mockCertBean;
        task.mpModeManager = mockPMode;
        task.mzppZPPUtils = mockZPPUtils;

        MSHOutMail outMail = createTestOutMail();

        // Capture the FictionNotification passed to serializeInOutMail
        ArgumentCaptor<MSHOutMail> outCaptor = ArgumentCaptor.forClass(MSHOutMail.class);
        ArgumentCaptor<MSHInMail> inCaptor = ArgumentCaptor.forClass(MSHInMail.class);

        task.processZPPFictionDelivery(outMail, TEST_SIGN_ALIAS);

        verify(mockDB).serializeInOutMail(inCaptor.capture(), outCaptor.capture(),
                eq(ZPPConstants.S_ZPP_PLUGIN_TYPE), isNull(si.laurentius.msh.pmode.PMode.class));

        MSHOutMail fictionNotification = outCaptor.getValue();

        // Verify the property exists
        assertNotNull("FictionNotification should have properties",
                fictionNotification.getMSHOutProperties());

        Optional<MSHOutProperty> prop = fictionNotification.getMSHOutProperties()
                .getMSHOutProperties().stream()
                .filter(p -> ZPPConstants.S_MAIL_PROPERTY_DELIVERED_BY_FICTION.equals(p.getName()))
                .findFirst();

        assertTrue("FictionNotification must carry DeliveredByFiction property",
                prop.isPresent());

        // Verify the value is a valid date (format: 2024-05-14+02:00)
        String propValue = prop.get().getValue();
        assertNotNull("Property value should not be null", propValue);
        Date parsedDate = DateAdapter.parseDate(propValue);
        assertNotNull("Property value should be a valid xs:date", parsedDate);

        // Verify it matches the date portion of deliveredDate
        Calendar expected = Calendar.getInstance();
        expected.setTime(outMail.getDeliveredDate());
        Calendar actual = Calendar.getInstance();
        actual.setTime(parsedDate);
        assertEquals("Property date should match the mail's deliveredDate",
                expected.get(Calendar.YEAR), actual.get(Calendar.YEAR));
        assertEquals("Property date should match the mail's deliveredDate",
                expected.get(Calendar.DAY_OF_YEAR), actual.get(Calendar.DAY_OF_YEAR));
    }

    /**
     * Test that ZPPTaskFictionByLastDelivery.processZPPFictionDelivery attaches the
     * DeliveredByFiction property with the lastDate (pickup date of other mail).
     */
    @Test
    public void testZPPTaskFictionByLastDelivery_addsDeliveredByFictionProperty() throws Exception {
        ZPPTaskFictionByLastDelivery task = new ZPPTaskFictionByLastDelivery();
        task.mDB = mockDB;
        task.mCertBean = mockCertBean;
        task.mpModeManager = mockPMode;
        task.mzppZPPUtils = mockZPPUtils;

        // Simulate: recipient picked up another mail yesterday at 14:00
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date lastDate = cal.getTime();

        MSHOutMail outMail = createTestOutMail();
        outMail.setDeliveredDate(lastDate);

        ArgumentCaptor<MSHOutMail> outCaptor = ArgumentCaptor.forClass(MSHOutMail.class);
        ArgumentCaptor<MSHInMail> inCaptor = ArgumentCaptor.forClass(MSHInMail.class);

        task.processZPPFictionDelivery(outMail, TEST_SIGN_ALIAS);

        verify(mockDB).serializeInOutMail(inCaptor.capture(), outCaptor.capture(),
                eq(ZPPConstants.S_ZPP_PLUGIN_TYPE), isNull(si.laurentius.msh.pmode.PMode.class));

        MSHOutMail fictionNotification = outCaptor.getValue();

        Optional<MSHOutProperty> prop = fictionNotification.getMSHOutProperties()
                .getMSHOutProperties().stream()
                .filter(p -> ZPPConstants.S_MAIL_PROPERTY_DELIVERED_BY_FICTION.equals(p.getName()))
                .findFirst();

        assertTrue("FictionNotification must carry DeliveredByFiction property",
                prop.isPresent());

        // For 6th paragraph, the property value should be the lastDate (date portion)
        Date parsedDate = DateAdapter.parseDate(prop.get().getValue());
        Calendar expected = Calendar.getInstance();
        expected.setTime(lastDate);
        Calendar actual = Calendar.getInstance();
        actual.setTime(parsedDate);
        assertEquals("Property date should match lastDate (pickup date of other mail)",
                expected.get(Calendar.YEAR), actual.get(Calendar.YEAR));
        assertEquals("Property date should match lastDate (pickup date of other mail)",
                expected.get(Calendar.DAY_OF_YEAR), actual.get(Calendar.DAY_OF_YEAR));
    }

    private MSHOutMail createTestOutMail() {
        MSHOutMail mail = new MSHOutMail();
        mail.setService(ZPPConstants.S_ZPP_SERVICE);
        mail.setAction(ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION);
        mail.setConversationId("TEST-CONV@test.si");
        mail.setMessageId("TEST-MSG@test.si");
        mail.setSenderEBox("sender@test.si");
        mail.setSenderName("Test Sender");
        mail.setReceiverEBox(TEST_RECEIVER_BOX);
        mail.setReceiverName("Test Receiver");
        mail.setSubject("Test subject");
        mail.setSentDate(Calendar.getInstance().getTime());
        return mail;
    }
}
