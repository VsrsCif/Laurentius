<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"   xmlns:S12="http://www.w3.org/2003/05/soap-envelope"  exclude-result-prefixes="S12 wsse wsa" xmlns:wsa="http://www.w3.org/2005/08/addressing"  xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:eb3="http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/" xmlns:ds="http://www.w3.org/2000/09/xmldsig#" xmlns:ebbp="http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0"    >
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="messageid">someuniqueid@receiver.example.com</xsl:param>
    <xsl:param name="timestamp">2010-10-10T10:10:10.000Z</xsl:param>
    <xsl:template match="S12:Envelope">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="S12:Header">
        <xsl:apply-templates select="eb3:Messaging"/>
    </xsl:template>
    <xsl:template match="S12:Envelope/S12:Body" />
   
    <xsl:template match="eb3:Messaging[not(@S12:role='http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/part2/200811/nextmsh')]">
		
        <xsl:apply-templates select="descendant-or-self::eb3:UserMessage"/>
		
    </xsl:template>
    <xsl:template match="eb3:UserMessage">
        <xsl:choose>
            <xsl:when test="/S12:Envelope/S12:Header/wsse:Security/ds:Signature">
                <ebbp:NonRepudiationInformation>
                    <xsl:apply-templates select="//ds:Reference"/>
                </ebbp:NonRepudiationInformation>
            </xsl:when>
            <xsl:otherwise>
                <xsl:namespace name="" select="string(@targetNamespace)"/>
                <xsl:copy-of select="//eb3:UserMessage/@* | node()"/>
                <!--xsl:copy-of select="//eb3:UserMessage"/ -->
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>
    <xsl:template match="ds:Reference">
        <ebbp:MessagePartNRInformation>
            <xsl:copy-of select="@* | node()"/>
            <!--xsl:copy-of select="current()"/-->
        </ebbp:MessagePartNRInformation>
    </xsl:template>
</xsl:stylesheet>
