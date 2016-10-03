<?xml version="1.0" encoding="UTF-8"?>
<html xsl:version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">
<head>
	<meta charset="UTF-8" />
</head>
	<body style="font-family:Arial;font-size:12pt;background-color:#EEEEEE">
		<table>
			<tbody>
				<xsl:for-each select="/xs:schema/xs:complexType[@name='MSHInMailType']/xs:attribute">
					<tr>
						<td>
							<xsl:value-of select="concat(upper-case(substring(@name,1,1)) , substring(@name, 2)) "/>
						</td>
						<td>
							<xsl:value-of select="xs:annotation/xs:documentation"/>
						</td>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</body>
</html>
