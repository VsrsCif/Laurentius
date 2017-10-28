<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" 
                exclude-result-prefixes="xd xsi" version="2.0" 
                xmlns:S12="http://www.w3.org/2003/05/soap-envelope" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns:wsa="http://www.w3.org/2005/08/addressing" 
                xmlns:ebint="http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/multihop/200902/" 
                xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:eb3="http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/" xmlns:ds="http://www.w3.org/2000/09/xmldsig#" xmlns:ebbp="http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
	<xd:doc scope="stylesheet">
		<xd:desc>
			<xd:p>
				<xd:b>Created on:</xd:b>13.08.2016</xd:p>
			<xd:p>This XSLT stylesheet is a non-normative part of the OASIS AS4 specification. It is created from 
    			https://docs.oasis-open.org/ebxml-msg/ebms/v3.0/profiles/AS4-profile/v1.0/os/examples/as4receipt/as4receipt.xsl			
    				
    			</xd:p>
		</xd:desc>
	</xd:doc>
	<xsl:output method="xml" indent="yes"/>
	    <xsl:param name="messageid">someuniqueid@receiver.example.com</xsl:param>
    <xsl:param name="timestamp">2012-02-05T19:43:11.735Z</xsl:param>

	<xsl:template match="S12:Envelope">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="S12:Header">
		<xsl:apply-templates select="eb3:Messaging"/>
	</xsl:template>
	    <xd:doc>
		<xd:desc>This  template for the <xd:i>eb3:Messaging</xd:i> element covers AS4 
            point-to-point receipt. Exchanged over a multi-hop network IS NOT SUPPORTED - use original transformation..</xd:desc>
	</xd:doc>
	<xsl:template
        match="eb3:Messaging[not(
        @S12:role='http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/part2/200811/nextmsh')]">
        <eb3:Messaging S12:mustUnderstand="true" id="{concat('_ebmessaging_',generate-id())}">
            <xsl:apply-templates select="descendant-or-self::eb3:UserMessage"/>
        </eb3:Messaging>
    </xsl:template>
	<xd:doc>
		<xd:desc>
			<xd:p>The AS4 receipt is generated based on <xd:i>eb3:UserMessage</xd:i> and
                <xd:i>ds:Signature</xd:i>content.</xd:p>
			<xd:ul>
				<xd:li>A receipt for a signed AS4 message references the message parts using
                    <xd:i>ds:Reference</xd:i>s in the WS-Security header of that message</xd:li>
				<xd:li>A receipt for an unsigned AS4 message references the message using the
                    <xd:i>eb3:UserMessage</xd:i> structure of the AS4 message.
                </xd:li>
			</xd:ul>
		</xd:desc>
	</xd:doc>
	<xsl:template match="eb3:UserMessage">
		<eb3:SignalMessage>
			<eb3:MessageInfo>
				<eb3:Timestamp>
					<xsl:value-of select="$timestamp"/>
				</eb3:Timestamp>
				<eb3:MessageId>
					<xsl:value-of select="$messageid"/>
				</eb3:MessageId>
				<eb3:RefToMessageId>
					<xsl:value-of select="descendant::eb3:MessageId"/>
				</eb3:RefToMessageId>
			</eb3:MessageInfo>
			<eb3:Receipt>
				<xsl:choose>
					<xsl:when test="/S12:Envelope/S12:Header/wsse:Security/ds:Signature">
						<ebbp:NonRepudiationInformation>
							<xsl:apply-templates select="//ds:Reference"/>
						</ebbp:NonRepudiationInformation>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="//eb3:UserMessage"/>
					</xsl:otherwise>
				</xsl:choose>
			</eb3:Receipt>
		</eb3:SignalMessage>
	</xsl:template>
	<xsl:template match="ds:Reference">
		<ebbp:MessagePartNRInformation>
			<xsl:copy-of select="current()"/>
		</ebbp:MessagePartNRInformation>
	</xsl:template>
</xsl:stylesheet>
