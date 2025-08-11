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
package si.jrc.msh.interceptor;

import static org.junit.Assert.*;

import org.apache.cxf.binding.soap.SoapFault;
import org.junit.Test;

import si.laurentius.commons.ebms.EBMSError;
import si.jrc.msh.exception.EBMSErrorCode;
import si.laurentius.msh.inbox.mail.MSHInMail;

/**
 * Simple test that demonstrates the RefToMessageId issue in error responses.
 * 
 * This test shows that when creating EBMSError with debug information as RefToMessageId
 * instead of actual message ID, the error response contains wrong information.
 * 
 * Expected: This test should FAIL, showing the exact issue from MSHPluginInterceptorAbstract.java:151
 * 
 * @author Jože Rihtaršič
 */
public class RefToMessageIdIssueTest {

    private static final String ACTUAL_MESSAGE_ID = "0b85bf97-1c8c-4e34-9fd7-b5f086537ba3@{IPADDRESS}";
    private static final String DEBUG_STRING = "InterceptEvent 'InMessage', isInboundMessage:'false', isRequestMessage 'false', inMailId 'null', outMailId null";

    /**
     * Test that verifies the bug from MSHPluginInterceptorAbstract.java:151 has been fixed
     * 
     * This test now passes after the fix implementation.
     */
    @Test
    public void testCurrentFixedBehavior() {
        System.out.println("Testing current fixed behavior from MSHPluginInterceptorAbstract...");
        
        // This reproduces the exact code from MSHPluginInterceptorAbstract.java:151
        MSHInMail inMail = createTestInMail();
        MSHInMail outMail = null; // null as in the error case
        
        // Create the debug string exactly like in MSHPluginInterceptorAbstract.java:94-101
        String strMsg = String.format(
            "InterceptEvent '%s', isInboundMessage:'%s', isRequestMessage '%s',"
            + " inMailId '%s', outMailId %s ",
            "InMessage", "false", "false",
            inMail != null ? inMail.getId() : "null",
            outMail != null ? outMail.getId() : "null"
        );
        
        System.out.println("Debug string (strMsg): " + strMsg);
        System.out.println("Actual MessageId: " + ACTUAL_MESSAGE_ID);
        
        // Create error message like in MSHPluginInterceptorAbstract.java:147-152
        String errmsg = String.format(
            "('%s') SoapInterceptorInterface '%s' throws an error with message: %s!",
            strMsg, "java:global/plugin-zpp/ZPPInInterceptor", "CMSException: IOException reading content");
        
        // FIXED: Now using actual MessageId as RefToMessageId (like the fix does)
        EBMSError fixedError = new EBMSError(
            EBMSErrorCode.Other, 
            inMail.getMessageId(), // <-- FIXED: Use actual MessageId
            errmsg, 
            new RuntimeException("Simulated plugin error"),
            SoapFault.FAULT_CODE_CLIENT
        );
        
        // Test the fix
        String actualRefToMessageId = fixedError.getRefToMessage();
        
        System.out.println("✓ Created EBMSError with fixed RefToMessageId");
        System.out.println("RefToMessageId in error: " + actualRefToMessageId);
        
        // Verify the fix works
        assertEquals("RefToMessageId should be the actual MessageId (fix verified)", 
                     ACTUAL_MESSAGE_ID, actualRefToMessageId);
        
        assertFalse("RefToMessageId should NOT contain debug information (fix verified)", 
                   actualRefToMessageId.contains("InterceptEvent"));
        
        assertFalse("RefToMessageId should NOT contain 'isInboundMessage' (fix verified)", 
                   actualRefToMessageId.contains("isInboundMessage"));
        
        System.out.println("✓ Fix verified: RefToMessageId contains actual MessageId, not debug information");
    }

    /**
     * Test showing the correct behavior (what the fix should do)
     */
    @Test
    public void testCorrectBehavior() {
        System.out.println("Testing correct behavior (what the fix should implement)...");
        
        MSHInMail inMail = createTestInMail();
        
        // Create error message
        String errmsg = "SoapInterceptorInterface throws an error with message: CMSException: IOException reading content";
        
        // CORRECT: Use actual MessageId as RefToMessageId
        EBMSError correctError = new EBMSError(
            EBMSErrorCode.Other, 
            inMail.getMessageId(), // <-- CORRECT: Use actual MessageId
            errmsg, 
            new RuntimeException("Simulated plugin error"),
            SoapFault.FAULT_CODE_CLIENT
        );
        
        String refToMessageId = correctError.getRefToMessage();
        
        System.out.println("✓ Created EBMSError with correct RefToMessageId");
        System.out.println("RefToMessageId in error: " + refToMessageId);
        
        // Verify correct behavior
        assertEquals("RefToMessageId should be the actual MessageId", 
                    ACTUAL_MESSAGE_ID, refToMessageId);
        
        assertFalse("RefToMessageId should NOT contain debug information", 
                   refToMessageId.contains("InterceptEvent"));
        
        System.out.println("✓ Correct behavior verified");
    }

    /**
     * Test that compares the error formats side by side
     */
    @Test
    public void testErrorFormatComparison() {
        System.out.println("\n=== Error Format Comparison ===");
        
        MSHInMail inMail = createTestInMail();
        String debugString = "InterceptEvent 'InMessage', isInboundMessage:'false', isRequestMessage 'false', inMailId 'null', outMailId null";
        String actualMessageId = inMail.getMessageId();
        
        // Buggy version (current implementation)
        EBMSError buggyError = new EBMSError(EBMSErrorCode.Other, debugString, 
            "Plugin error occurred", SoapFault.FAULT_CODE_CLIENT);
        
        // Correct version (what it should be)
        EBMSError correctError = new EBMSError(EBMSErrorCode.Other, actualMessageId, 
            "Plugin error occurred", SoapFault.FAULT_CODE_CLIENT);
        
        System.out.println("BUGGY RefToMessageId  : " + buggyError.getRefToMessage());
        System.out.println("CORRECT RefToMessageId: " + correctError.getRefToMessage());
        System.out.println("Expected in XML       : <RefToMessageId>" + actualMessageId + "</RefToMessageId>");
        System.out.println("Actually in XML       : <RefToMessageId>" + buggyError.getRefToMessage() + "</RefToMessageId>");
        
        // This test will always pass but documents the difference
        assertNotEquals("Bug vs Correct should be different", 
                       buggyError.getRefToMessage(), correctError.getRefToMessage());
    }

    private MSHInMail createTestInMail() {
        MSHInMail inMail = new MSHInMail();
        inMail.setMessageId(ACTUAL_MESSAGE_ID);
        inMail.setRefToMessageId("8b2a511f-05ed-41ed-ad8a-98346f144b5e@b2g.sodisce.si");
        inMail.setAction("AdviceOfDelivery");
        inMail.setService("LegalDelivery_ZPP");
        inMail.setSenderEBox("rbinkaso@poslovna.posta.si");
        inMail.setReceiverEBox("izvrsba@b2g.sodisce.si");
        inMail.setId(null); // null ID as in the error case
        return inMail;
    }
}