/*
 * Copyright 2026, Supreme Court Republic of Slovenia
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
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import javax.xml.bind.DatatypeConverter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import si.jrc.msh.plugin.zpp.utils.XSLTExtensions;
import si.jrc.msh.plugin.zpp.utils.ZPPUtils;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.inbox.property.MSHInProperty;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.property.MSHOutProperty;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.PartyIdentitySetType;

/**
 * Fiction date for ZPP variant A (Laurentius creates the fiction itself),
 * Redmine #14612.
 *
 * Expected behaviour (7th paragraph of 141.a ZPP): mail is put into the
 * recipient's VEP on day D, the 15-day period starts on D+1, delivery is deemed
 * done on the last day of the period (D+15). Laurentius issues the fiction on
 * the first day after the period (D+16) and the PDF, the in-mail imported to
 * EVIP and the out mail must all state D+15.
 *
 * The scenario used: mail sent 16 days ago, fiction task runs today.
 */
public class ZPPFictionDateTest {

    private static final String SIGN_ALIAS = "test-alias";
    private static final String RECEIVER_BOX = "receiver@test.si";
    // property names read by laurentius-evip-plugin (ZPPConstants.S_PROPERTY_FICTION_DATE/2)
    private static final String EVIP_PROP_FICTION_DATE = "DeliverdByFiction";
    private static final String EVIP_PROP_FICTION_DATE2 = "DeliveredByFiction";

    private SEDDaoInterface mockDB;
    private SEDCertStoreInterface mockCertBean;
    private PModeInterface mockPMode;
    private ZPPUtils mockZPPUtils;

    @Before
    public void setUp() throws Throwable {
        mockDB = mock(SEDDaoInterface.class);
        mockCertBean = mock(SEDCertStoreInterface.class);
        mockPMode = mock(PModeInterface.class);
        mockZPPUtils = mock(ZPPUtils.class);

        PartyIdentitySet pis = new PartyIdentitySet();
        PartyIdentitySetType.ExchangePartySecurity eps
                = new PartyIdentitySetType.ExchangePartySecurity();
        eps.setSignatureCertAlias("receiver-cert");
        pis.setExchangePartySecurity(eps);
        when(mockPMode.getPartyIdentitySetForSEDAddress(RECEIVER_BOX)).thenReturn(pis);

        when(mockCertBean.getX509CertForAlias(anyString())).thenReturn(mock(X509Certificate.class));
        when(mockCertBean.getPrivateKeyForAlias(anyString())).thenReturn(mock(PrivateKey.class));

        MSHOutPart dummyPart = new MSHOutPart();
        when(mockZPPUtils.createSignedAdviceOfFictionNotification(
                any(MSHOutMail.class), any(PrivateKey.class), any(X509Certificate.class)))
                .thenReturn(dummyPart);
        when(mockZPPUtils.createSignedAdviceOfDeliveryFiction(
                any(MSHOutMail.class), any(PrivateKey.class), any(X509Certificate.class)))
                .thenReturn(new MSHInPart());
        when(mockZPPUtils.createMSHOutPart(
                any(), any(), any(), any(PrivateKey.class), any(X509Certificate.class)))
                .thenReturn(dummyPart);
        when(mockZPPUtils.createMSHInPart(
                any(), any(), any(), any(PrivateKey.class), any(X509Certificate.class)))
                .thenReturn(new MSHInPart());
        when(mockZPPUtils.getEncKeyFromOut(any(MSHOutMail.class)))
                .thenReturn(mock(java.security.Key.class));
        when(mockZPPUtils.createEncryptedKey(any(), any(), anyString(), anyString()))
                .thenReturn(dummyPart);
    }

    // ------------------------------------------------------------------
    // Reference behaviour that is already correct

    /**
     * The fiction task first selects a mail on day D+16 (filter: SentDate &lt;
     * midnight of today-15).
     */
    @Test
    public void fictionTask_selectsMailFirstOnDay16() throws Exception {
        ZPPTaskFiction task = newFictionTask();
        doReturn(Collections.emptyList()).when(mockDB).getDataList(
                eq(MSHOutMail.class), anyInt(), anyInt(), anyString(), anyString(), any());

        Properties p = new Properties();
        p.setProperty("zpp.sign.key.alias", SIGN_ALIAS);
        task.executeTask(p);

        ArgumentCaptor<Object> filter = ArgumentCaptor.forClass(Object.class);
        verify(mockDB).getDataList(eq(MSHOutMail.class), eq(-1), eq(100),
                eq("Id"), eq("ASC"), filter.capture());
        Date cutoff = ((ZPPMailFilter) filter.getValue()).getSentDateTo();

        // DAO applies "SentDateTo" as SentDate < cutoff
        Date sentOnDay15Start = startOfDay(daysAgo(15));
        Date sentLateOnDay16 = at(daysAgo(16), 23, 59, 59);
        assertFalse("Mail whose 15th day is today must not be selected yet",
                sentOnDay15Start.before(cutoff));
        assertTrue("Mail whose 15th day was yesterday must be selected",
                sentLateOnDay16.before(cutoff));
    }

    /**
     * The fiction PDF (vročilnica) states sentDate + 15. Customer example:
     * notice created 18.07.2025, EVIP dat_vrocitve 02.08.2025.
     */
    @Test
    public void pdfFictionDate_customerExample_is02082025() {
        Date sent = at(date(2025, Calendar.JULY, 18), 14, 26, 42);
        assertEquals("02. 08. 2025",
                XSLTExtensions.getZPPFictionDate(DateAdapter.printDateTime(sent)));
    }

    @Test
    public void pdfFictionDate_isDay15() {
        MSHOutMail outMail = createSentMail(at(daysAgo(16), 14, 26, 42));
        assertEquals(new SimpleDateFormat("dd. MM. yyyy").format(daysAgo(1)),
                XSLTExtensions.getZPPFictionDate(
                        DateAdapter.printDateTime(outMail.getSentDate())));
    }

    // ------------------------------------------------------------------
    // 7th paragraph (ZPPTaskFiction) - currently failing

    /**
     * EVIP reads the delivery date from the AdviceOfDeliveryFiction in-mail
     * property; the in-mail created by the fiction task must carry it.
     */
    @Test
    public void fictionTask_adviceOfDeliveryFictionInMail_carriesDay15() throws Exception {
        MSHOutMail outMail = createSentMail(at(daysAgo(16), 14, 26, 42));

        MSHInMail inMail = runFictionTask(outMail);

        Optional<String> prop = getInProperty(inMail, EVIP_PROP_FICTION_DATE);
        assertTrue("AdviceOfDeliveryFiction in-mail must carry DeliverdByFiction",
                prop.isPresent());
        assertSameDay(daysAgo(1), DateAdapter.parseDate(prop.get()));
    }

    /**
     * End-to-end view of what EVIP stores in vl_posiljke.dat_vrocitve. The
     * fiction PDF is signed when the task runs, so the signature date is now.
     */
    @Test
    public void fictionTask_dateImportedToEVIP_isDay15() throws Exception {
        MSHOutMail outMail = createSentMail(at(daysAgo(16), 14, 26, 42));

        MSHInMail inMail = runFictionTask(outMail);
        Date pdfSignatureDate = new Date();

        assertSameDay(daysAgo(1), evipDeliveryDate(inMail, pdfSignatureDate));
    }

    @Test
    public void fictionTask_fictionNotificationProperty_isDay15() throws Exception {
        MSHOutMail outMail = createSentMail(at(daysAgo(16), 14, 26, 42));

        ArgumentCaptor<MSHOutMail> outCaptor = ArgumentCaptor.forClass(MSHOutMail.class);
        newFictionTask().processZPPFictionDelivery(outMail, SIGN_ALIAS);
        verify(mockDB).serializeInOutMail(any(MSHInMail.class), outCaptor.capture(),
                anyString(), any());

        Optional<String> prop = getOutProperty(outCaptor.getValue(),
                ZPPConstants.S_MAIL_PROPERTY_DELIVERED_BY_FICTION);
        assertTrue(prop.isPresent());
        assertSameDay(daysAgo(1), DateAdapter.parseDate(prop.get()));
    }

    @Test
    public void fictionTask_originalMailDeliveredDate_isDay15() throws Exception {
        MSHOutMail outMail = createSentMail(at(daysAgo(16), 14, 26, 42));

        runFictionTask(outMail);

        assertSameDay(daysAgo(1), outMail.getDeliveredDate());
    }

    // ------------------------------------------------------------------
    // 6th paragraph (ZPPTaskFictionByLastDelivery) - currently failing

    /**
     * Fiction by pickup of another mail: the in-mail imported to EVIP must
     * carry the pickup date.
     */
    @Test
    public void fictionByLastDelivery_inMail_carriesPickupDate() throws Exception {
        ZPPTaskFictionByLastDelivery task = new ZPPTaskFictionByLastDelivery();
        task.mDB = mockDB;
        task.mCertBean = mockCertBean;
        task.mpModeManager = mockPMode;
        task.mzppZPPUtils = mockZPPUtils;

        Date pickup = at(daysAgo(1), 14, 0, 0);
        MSHOutMail outMail = createSentMail(at(daysAgo(5), 10, 0, 0));
        outMail.setDeliveredDate(pickup);

        ArgumentCaptor<MSHInMail> inCaptor = ArgumentCaptor.forClass(MSHInMail.class);
        task.processZPPFictionDelivery(outMail, SIGN_ALIAS);
        verify(mockDB).serializeInOutMail(inCaptor.capture(), any(MSHOutMail.class),
                anyString(), any());

        Optional<String> prop = getInProperty(inCaptor.getValue(), EVIP_PROP_FICTION_DATE);
        assertTrue("AdviceOfDeliveryFiction in-mail must carry DeliverdByFiction",
                prop.isPresent());
        assertSameDay(pickup, DateAdapter.parseDate(prop.get()));
    }

    // ------------------------------------------------------------------

    private ZPPTaskFiction newFictionTask() {
        ZPPTaskFiction task = new ZPPTaskFiction();
        task.mDB = mockDB;
        task.mCertBean = mockCertBean;
        task.mpModeManager = mockPMode;
        task.mzppZPPUtils = mockZPPUtils;
        return task;
    }

    private MSHInMail runFictionTask(MSHOutMail outMail) throws Exception {
        ArgumentCaptor<MSHInMail> inCaptor = ArgumentCaptor.forClass(MSHInMail.class);
        newFictionTask().processZPPFictionDelivery(outMail, SIGN_ALIAS);
        verify(mockDB).serializeInOutMail(inCaptor.capture(), any(MSHOutMail.class),
                anyString(), any());
        return inCaptor.getValue();
    }

    /**
     * Mirrors laurentius-evip-plugin
     * TaskImportDeliveryAdviceToEVIP.createDeliveryAdvice: fiction property on
     * the in-mail, otherwise the earliest PDF signature date.
     */
    private static Date evipDeliveryDate(MSHInMail inMail, Date pdfSignatureDate) {
        Optional<String> val = getInProperty(inMail, EVIP_PROP_FICTION_DATE);
        if (!val.isPresent()) {
            val = getInProperty(inMail, EVIP_PROP_FICTION_DATE2);
        }
        return val.isPresent()
                ? DatatypeConverter.parseDate(val.get()).getTime()
                : pdfSignatureDate;
    }

    private static Optional<String> getInProperty(MSHInMail mail, String name) {
        if (mail.getMSHInProperties() == null) {
            return Optional.empty();
        }
        return mail.getMSHInProperties().getMSHInProperties().stream()
                .filter(p -> name.equals(p.getName()))
                .map(MSHInProperty::getValue)
                .findFirst();
    }

    private static Optional<String> getOutProperty(MSHOutMail mail, String name) {
        if (mail.getMSHOutProperties() == null) {
            return Optional.empty();
        }
        return mail.getMSHOutProperties().getMSHOutProperties().stream()
                .filter(p -> name.equals(p.getName()))
                .map(MSHOutProperty::getValue)
                .findFirst();
    }

    private static MSHOutMail createSentMail(Date sentDate) {
        MSHOutMail mail = new MSHOutMail();
        mail.setService(ZPPConstants.S_ZPP_SERVICE);
        mail.setAction(ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION);
        mail.setConversationId("TEST-CONV@test.si");
        mail.setMessageId("TEST-MSG@test.si");
        mail.setSenderEBox("sender@test.si");
        mail.setSenderName("Test Sender");
        mail.setReceiverEBox(RECEIVER_BOX);
        mail.setReceiverName("Test Receiver");
        mail.setSubject("Test subject");
        mail.setSentDate(sentDate);
        mail.setReceivedDate(sentDate);
        return mail;
    }

    private static void assertSameDay(Date expected, Date actual) {
        assertNotNull("date is null", actual);
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(f.format(expected), f.format(actual));
    }

    private static Date daysAgo(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -days);
        return c.getTime();
    }

    private static Date date(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month, day);
        return c.getTime();
    }

    private static Date startOfDay(Date d) {
        return at(d, 0, 0, 0);
    }

    private static Date at(Date d, int hour, int minute, int second) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}
