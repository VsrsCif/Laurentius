<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<TargetXML>
		<xsl:attribute name="version">1.0</xsl:attribute>
		<xsl:attribute name="transformation">transformation_v1.0.xslt</xsl:attribute>
			<Name>
				<xsl:value-of select="/SourceXML/Entity/@name"/>
			</Name>
			<Address>
				<xsl:value-of select="/SourceXML/Entity/@address"/>
			</Address>
		</TargetXML>
	</xsl:template>
</xsl:stylesheet>
