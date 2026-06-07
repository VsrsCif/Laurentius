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
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.junit.Before;
import org.junit.Test;

import si.laurentius.commons.cxf.EBMSConstants;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.property.MSHOutProperties;
import si.laurentius.msh.outbox.property.MSHOutProperty;
import si.laurentius.msh.pmode.Action;
import si.laurentius.msh.pmode.Service;

/**
 * Tests that ZPPOutInterceptor adds the DeliveredByFiction property
 * to FictionNotification mails during the outbound interceptor phase.
 * This is the actual code path that produces the SOAP MessageProperties.
 */
public class ZPPOutInterceptorFictionTest {

    private static final String ORIGINAL_MSG_ID = "original-msg-001@court-laurentius.si";
    private static final String FICTION_MSG_ID = "fiction-notif-001@court-laurentius.si";

    private ZPPOutInterceptor interceptor;
    private SEDDaoInterface mockDB;

    @Before
    public void setUp() {
        interceptor = new ZPPOutInterceptor();
        mockDB = mock(SEDDaoInterface.class);
        interceptor.mDB = mockDB;
        interceptor.mdbLookup = mock(si.laurentius.commons.interfaces.SEDLookupsInterface.class);
        interceptor.mCertBean = mock(si.laurentius.commons.interfaces.SEDCertStoreInterface.class);
    }

    /**
     * Simulates the outbound interceptor chain for a FictionNotification mail.
     * The original mail (DeliveryNotification) has been delivered by fiction
     * and its DeliveredDate is set. The interceptor should look up the original
     * mail and add the DeliveredByFiction property to the FictionNotification.
     */
    @Test
    public void testFictionNotification_addsDeliveredByFictionProperty() {
        Date fictionDate = createFixedDate(2026, Calendar.MAY, 29, 5, 5, 8);

        // Original mail — the DeliveryNotification that was delivered by fiction
        MSHOutMail originalMail = new MSHOutMail();
        originalMail.setId(BigInteger.valueOf(100));
        originalMail.setMessageId(ORIGINAL_MSG_ID);
        originalMail.setDeliveredDate(fictionDate);

        // Fiction notification mail — references the original
        MSHOutMail fictionNotification = createFictionNotificationMail();

        // Mock DB to return the original mail when looked up by MessageId
        when(mockDB.getMailByMessageId(eq(MSHOutMail.class), eq(ORIGINAL_MSG_ID)))
                .thenReturn(Collections.singletonList(originalMail));

        // Build mock SoapMessage with the fiction notification on the exchange
        SoapMessage soapMsg = createMockSoapMessage(fictionNotification);

        // Execute the interceptor
        boolean result = interceptor.handleMessage(soapMsg, new Properties());

        assertTrue("handleMessage should return true", result);

        // Verify the property was added to the in-memory fiction notification
        assertNotNull("FictionNotification should have properties after interceptor",
                fictionNotification.getMSHOutProperties());

        Optional<MSHOutProperty> prop = fictionNotification.getMSHOutProperties()
                .getMSHOutProperties().stream()
                .filter(p -> ZPPConstants.S_MAIL_PROPERTY_DELIVERED_BY_FICTION.equals(p.getName()))
                .findFirst();

        assertTrue("FictionNotification must carry DeliveredByFiction property",
                prop.isPresent());

        String propValue = prop.get().getValue();
        assertNotNull("Property value should not be null", propValue);

        // Format should be date+timezone like "2026-05-29+02:00"
        assertTrue("Property value should be in xs:date format (e.g. 2026-05-29+02:00)",
                propValue.matches("\\d{4}-\\d{2}-\\d{2}[+-]\\d{2}:\\d{2}"));

        Date parsedDate = DateAdapter.parseDate(propValue);
        assertNotNull("Property value should be a valid xs:date", parsedDate);

        // Compare year/month/day
        Calendar expected = Calendar.getInstance();
        expected.setTime(fictionDate);
        Calendar actual = Calendar.getInstance();
        actual.setTime(parsedDate);
        assertEquals("Year should match", expected.get(Calendar.YEAR), actual.get(Calendar.YEAR));
        assertEquals("Month should match", expected.get(Calendar.MONTH), actual.get(Calendar.MONTH));
        assertEquals("Day should match", expected.get(Calendar.DAY_OF_MONTH), actual.get(Calendar.DAY_OF_MONTH));

        // Verify DB was queried with the correct RefToMessageId
        verify(mockDB).getMailByMessageId(MSHOutMail.class, ORIGINAL_MSG_ID);
    }

    /**
     * When the original mail is not found in DB, the interceptor should
     * not fail — it should just skip adding the property.
     */
    @Test
    public void testFictionNotification_originalMailNotFound_noPropertyAdded() {
        MSHOutMail fictionNotification = createFictionNotificationMail();

        when(mockDB.getMailByMessageId(eq(MSHOutMail.class), eq(ORIGINAL_MSG_ID)))
                .thenReturn(Collections.emptyList());

        SoapMessage soapMsg = createMockSoapMessage(fictionNotification);

        boolean result = interceptor.handleMessage(soapMsg, new Properties());

        assertTrue("handleMessage should return true even if original not found", result);

        // Property should NOT be present
        if (fictionNotification.getMSHOutProperties() != null) {
            Optional<MSHOutProperty> prop = fictionNotification.getMSHOutProperties()
                    .getMSHOutProperties().stream()
                    .filter(p -> ZPPConstants.S_MAIL_PROPERTY_DELIVERED_BY_FICTION.equals(p.getName()))
                    .findFirst();
            assertFalse("No DeliveredByFiction property when original mail not found",
                    prop.isPresent());
        }
    }

    /**
     * When the original mail has no DeliveredDate (edge case), the property
     * should not be added.
     */
    @Test
    public void testFictionNotification_originalMailNoDeliveredDate_noPropertyAdded() {
        MSHOutMail originalMail = new MSHOutMail();
        originalMail.setId(BigInteger.valueOf(100));
        originalMail.setMessageId(ORIGINAL_MSG_ID);
        originalMail.setDeliveredDate(null);

        MSHOutMail fictionNotification = createFictionNotificationMail();

        when(mockDB.getMailByMessageId(eq(MSHOutMail.class), eq(ORIGINAL_MSG_ID)))
                .thenReturn(Collections.singletonList(originalMail));

        SoapMessage soapMsg = createMockSoapMessage(fictionNotification);

        boolean result = interceptor.handleMessage(soapMsg, new Properties());

        assertTrue("handleMessage should return true", result);

        if (fictionNotification.getMSHOutProperties() != null) {
            Optional<MSHOutProperty> prop = fictionNotification.getMSHOutProperties()
                    .getMSHOutProperties().stream()
                    .filter(p -> ZPPConstants.S_MAIL_PROPERTY_DELIVERED_BY_FICTION.equals(p.getName()))
                    .findFirst();
            assertFalse("No DeliveredByFiction property when original has no DeliveredDate",
                    prop.isPresent());
        }
    }

    /**
     * When the fiction notification already has the DeliverdByFiction property
     * (set by the task at creation time and persisted to DB), the interceptor
     * should NOT overwrite it — this is the primary source.
     */
    @Test
    public void testFictionNotification_existingPropertyNotOverwritten() {
        String existingValue = "2026-05-20+02:00";

        MSHOutMail fictionNotification = createFictionNotificationMail();
        // Pre-set the property (simulating it was persisted from the task)
        MSHOutProperties mop = new MSHOutProperties();
        MSHOutProperty existingProp = new MSHOutProperty();
        existingProp.setName(ZPPConstants.S_MAIL_PROPERTY_DELIVERED_BY_FICTION);
        existingProp.setValue(existingValue);
        mop.getMSHOutProperties().add(existingProp);
        fictionNotification.setMSHOutProperties(mop);

        // Original mail has a different date
        Date differentDate = createFixedDate(2026, Calendar.MAY, 29, 5, 5, 8);
        MSHOutMail originalMail = new MSHOutMail();
        originalMail.setId(BigInteger.valueOf(100));
        originalMail.setMessageId(ORIGINAL_MSG_ID);
        originalMail.setDeliveredDate(differentDate);

        when(mockDB.getMailByMessageId(eq(MSHOutMail.class), eq(ORIGINAL_MSG_ID)))
                .thenReturn(Collections.singletonList(originalMail));

        SoapMessage soapMsg = createMockSoapMessage(fictionNotification);

        interceptor.handleMessage(soapMsg, new Properties());

        // The existing property value should be preserved, NOT overwritten
        Optional<MSHOutProperty> prop = fictionNotification.getMSHOutProperties()
                .getMSHOutProperties().stream()
                .filter(p -> ZPPConstants.S_MAIL_PROPERTY_DELIVERED_BY_FICTION.equals(p.getName()))
                .findFirst();

        assertTrue("Property should still be present", prop.isPresent());
        assertEquals("Existing property value should NOT be overwritten",
                existingValue, prop.get().getValue());
    }

    /**
     * Non-FictionNotification ZPP actions (e.g. AdviceOfDelivery) should NOT
     * trigger the DeliveredByFiction property logic.
     */
    @Test
    public void testNonFictionAction_doesNotAddDeliveredByFictionProperty() {
        MSHOutMail otherMail = new MSHOutMail();
        otherMail.setId(BigInteger.valueOf(200));
        otherMail.setMessageId("other-001@court-laurentius.si");
        otherMail.setService("SomeOtherService");
        otherMail.setAction("SomeOtherAction");
        otherMail.setRefToMessageId(ORIGINAL_MSG_ID);
        otherMail.setSenderEBox("sender@test.si");
        otherMail.setReceiverEBox("receiver@test.si");

        SoapMessage soapMsg = createMockSoapMessage(otherMail);

        interceptor.handleMessage(soapMsg, new Properties());

        // DB should NOT be queried for the original mail
        verify(mockDB, never()).getMailByMessageId(any(), anyString());
    }

    // --- helpers ---

    private MSHOutMail createFictionNotificationMail() {
        MSHOutMail mail = new MSHOutMail();
        mail.setId(BigInteger.valueOf(200));
        mail.setMessageId(FICTION_MSG_ID);
        mail.setService(ZPPConstants.S_ZPP_SERVICE);
        mail.setAction(ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION);
        mail.setConversationId("180705@court-laurentius.si");
        mail.setRefToMessageId(ORIGINAL_MSG_ID);
        mail.setSenderEBox("sender@court-laurentius.si");
        mail.setSenderName("Test Sender");
        mail.setReceiverEBox("receiver@postar.eu");
        mail.setReceiverName("Test Receiver");
        mail.setSubject(ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION);
        return mail;
    }

    private SoapMessage createMockSoapMessage(MSHOutMail outMail) {
        // Build EBMSMessageContext with ZPP service and the mail's action
        Service service = new Service();
        service.setServiceName(outMail.getService());

        Action action = new Action();
        action.setName(outMail.getAction());

        EBMSMessageContext ectx = new EBMSMessageContext();
        ectx.setService(service);
        ectx.setAction(action);

        // Mock the CXF Exchange to return the right objects
        Exchange exchange = mock(Exchange.class);
        when(exchange.get(EBMSConstants.EBMS_CP_OUT_CONTEXT)).thenReturn(ectx);
        when(exchange.get(EBMSConstants.EBMS_CP_OUTMAIL)).thenReturn(outMail);
        when(exchange.get(EBMSConstants.EBMS_CP_INMAIL)).thenReturn(null);
        when(exchange.get(EBMSConstants.EBMS_CP_IN_CONTEXT)).thenReturn(null);

        // Mock SoapMessage
        SoapMessage soapMsg = mock(SoapMessage.class);
        when(soapMsg.getExchange()).thenReturn(exchange);
        // MessageUtils.isRequestor checks message property
        when(soapMsg.get(Message.REQUESTOR_ROLE)).thenReturn(Boolean.TRUE);

        return soapMsg;
    }

    private Date createFixedDate(int year, int month, int day, int hour, int min, int sec) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, hour, min, sec);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
