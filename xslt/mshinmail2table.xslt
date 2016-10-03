<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<xsl:output method="html" version="4.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
	<xsl:element name="table">
		<xsl:element name="tbody">
		<xsl:element name="tr">
					<xsl:element name="th">Lastnost</xsl:element>
					<xsl:element name="th">opis</xsl:element>
				</xsl:element>
			<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
			<xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'"/>
			<xsl:for-each select="/xs:schema/xs:complexType[@name='MSHInMailType']/xs:attribute">
				<xsl:element name="tr">
					<xsl:element name="td">
						<xsl:value-of select="concat(translate(substring(@name,1,1), $lowercase, $uppercase) , substring(@name, 2)) "/>
					</xsl:element>
					<xsl:element name="td">
						<xsl:value-of select="xs:annotation/xs:documentation"/>
					</xsl:element>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>
	</xsl:element>
	</xsl:template>
</xsl:stylesheet>
