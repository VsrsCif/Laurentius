<?xml version="1.0" encoding="UTF-8"?>
<!-- edited with XMLSpy v2012 sp1 (http://www.altova.com) by M.Kos (Nova KBM d.d.) -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:dSignature="http://www.w3.org/2000/09/xmldsig#" 
  xmlns:iZbs_DokVseb="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_DokumentiVsebinski.xsd" 
  xmlns:iZbs_AS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_AkcijeSklepov.xsd" 
  xmlns:iZbs_Body="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_Body.xsd" 
  xmlns:iZbs_DocAS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_DokumentAkcijSklepov.xsd" 
  xmlns:iZbs_ElDoc="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ElementiDokumenta.xsd" 
  xmlns:iZbs_ElS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ElementiSklepa.xsd" 
  xmlns:iZbs_Spl="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_SplosniTipi.xsd" 
  xmlns:iZbs_Zaglavje="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ZaglavjeSporocil.xsd">

	<xsl:output method="html" indent="yes"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>Grafični prikaz dokumenta XML</title>
				<link rel="stylesheet" type="text/css" href="IzvrsbeXML_VizualniStili.css"/>
			</head>
			<body>
				<h1>Dokument zahtevkov, za izvedbo akcij nad sklepi</h1>
				<br/>
				<!-- *************************** POSILJATELJ / AVTOR SPOROCILA *************************** -->
				<xsl:for-each select="iZbs_DokVseb:PrevzemiDokumentXmlResponse/iZbs_DokVseb:Header">
					<h2>Podatki o sporočilu, avtorju in naslovniku:</h2>
					<div></div>
					<table class="table" width="800px">
						<tbody>
							<tr>
								<td>Datum sporočila:</td>
								<td><xsl:value-of select="iZbs_Zaglavje:DatumSporocila"/></td>
							</tr>
							<tr>
								<td>ID sporočila:</td>
								<td><xsl:value-of select="iZbs_Zaglavje:IdSporocila/iZbs_Spl:Id"/>; <xsl:value-of select="iZbs_Zaglavje:IdSporocila/iZbs_Spl:DavcnaStevilka"/></td>
							</tr>
							<tr>
								<td>Pošiljatelj sporočila:</td>
								<td>
									<xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naziv"/>
									<br/>
									Davčna št.&#160;=&#160;<xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:DavcnaStevilka"/>
									<br/>
									<xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:Ulica"/>&#160;<xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:HisnaStevilka"/>
									<br/>
									<xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:Posta"/>&#160;<xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:Kraj"/>
									<br/>
									<xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:Drzava"/>
									<br/>
									<xsl:if test="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:PostniPredal != ''" >
										P.p.&#160;=&#160;<xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:PostniPredal"/>
										<br/>
									</xsl:if>
									<xsl:if test="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:ElektronskiPostniNaslov != ''" >
										E-pošta = <xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:ElektronskiPostniNaslov"/>
										<br/>
									</xsl:if>
									<xsl:if test="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:Telefon != ''" >
										Telefon = <xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:Telefon"/>
										<br/>
									</xsl:if>
								</td>
							</tr>
							<tr>
								<td>Prejemnik sporočila:</td>
								<td>
									<xsl:value-of select="iZbs_Zaglavje:PrejemnikSporocila/iZbs_Spl:Naziv"/>
									<br/>
									Davčna št.&#160;=&#160;<xsl:value-of select="iZbs_Zaglavje:PrejemnikSporocila/iZbs_Spl:DavcnaStevilka"/>
									<br/>
									<xsl:value-of select="iZbs_Zaglavje:PrejemnikSporocila/iZbs_Spl:Naslov/iZbs_Spl:Ulica"/>&#160;<xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:HisnaStevilka"/>
									<br/>
									<xsl:value-of select="iZbs_Zaglavje:PrejemnikSporocila/iZbs_Spl:Naslov/iZbs_Spl:Posta"/>&#160;<xsl:value-of select="iZbs_Zaglavje:PosiljateljSporocila/iZbs_Spl:Naslov/iZbs_Spl:Kraj"/>
									<br/>
									<xsl:value-of select="iZbs_Zaglavje:PrejemnikSporocila/iZbs_Spl:Naslov/iZbs_Spl:Drzava"/>
									<br/>
									<xsl:if test="iZbs_Zaglavje:PrejemnikSporocila/iZbs_Spl:Naslov/iZbs_Spl:PostniPredal != ''" >
										P.p.&#160;=&#160;<xsl:value-of select="iZbs_Zaglavje:PrejemnikSporocila/iZbs_Spl:Naslov/iZbs_Spl:PostniPredal"/>
										<br/>
									</xsl:if>
									<xsl:if test="iZbs_Zaglavje:PrejemnikSporocila/iZbs_Spl:Naslov/iZbs_Spl:ElektronskiPostniNaslov != ''" >
										E-pošta = <xsl:value-of select="iZbs_Zaglavje:PrejemnikSporocila/iZbs_Spl:Naslov/iZbs_Spl:ElektronskiPostniNaslov"/>
										<br/>
									</xsl:if>
									<xsl:if test="iZbs_Zaglavje:PrejemnikSporocila/iZbs_Spl:Naslov/iZbs_Spl:Telefon != ''" >
										Telefon = <xsl:value-of select="iZbs_Zaglavje:PrejemnikSporocila/iZbs_Spl:Naslov/iZbs_Spl:Telefon"/>
										<br/>
									</xsl:if>
								</td>
							</tr>
							<tr>
								<td>Test ali produkcija:</td>
								<td><xsl:value-of select="iZbs_Zaglavje:TestAliProdukcija"/></td>
							</tr>
							<tr>
								<td>ID izvornega sistema:</td>
								<td><xsl:value-of select="iZbs_Zaglavje:IdIzvornegaSistema"/></td>
							</tr>
						</tbody>
					</table>
				</xsl:for-each>
				
<!--
				<xsl:for-each select="iZbs_DokVseb:PrevzemiDokumentXmlResponse/iZbs_DokVseb:Body/iZbs_Body:DokumentAkcijSklepov/iZbs_DocAS:PaketAkcijSklepov/iZbs_DocAS:AkcijeSklepov">
					<h2>Nasel BODY ELEMENT 1111!!!</h2>
				</xsl:for-each>
-->				
				<xsl:for-each select="iZbs_DokVseb:PrevzemiDokumentXmlResponse/iZbs_DokVseb:Body/iZbs_Body:DokumentAkcijSklepov/iZbs_DocAS:PaketAkcijSklepov/iZbs_DocAS:AkcijeSklepov">
					<xsl:for-each select="iZbs_DocAS:AkcijaSklepa">
						<hr/>
						<h2>Zahtevek za sklep</h2>
						<table class="table" width="800px">
							<tbody>
								<tr>
									<td>Tip zahtevka:</td>
									<td><xsl:value-of select="@xsi:type"/></td>
								</tr>
								<tr>
									<td>ID zahtevka:</td>
									<td><xsl:value-of select="iZbs_AS:Sklep/iZbs_ElS:IdSklepa"/></td>
								</tr>
								<tr>
									<td>Opravilna številka sklepa:</td>
									<td><xsl:value-of select="iZbs_AS:Sklep/iZbs_ElS:IdSklepa"/></td>
								</tr>
								<tr>
									<td>Datumi sklepa:</td>
									<td>
										izdaja = <xsl:value-of select="iZbs_AS:Sklep/iZbs_ElS:DatumiSklepa/iZbs_ElS:DatumIzdaje"/><br/>
										izvršljivost = <xsl:value-of select="iZbs_AS:Sklep/iZbs_ElS:DatumiSklepa/iZbs_ElS:DatumIzvrsljivosti"/><br/>
										vročitev = <xsl:value-of select="iZbs_AS:Sklep/iZbs_ElS:DatumiSklepa/iZbs_ElS:DatumVrocitve"/><br/>
									</td>
								</tr>
								<tr>
									<td>Opravilna številka:</td>
									<td><xsl:value-of select="iZbs_AS:Sklep/iZbs_ElS:IdSklepa"/></td>
								</tr>
							</tbody>
						</table>
						<br/>
						<xsl:for-each select="iZbs_AS:Sklep/iZbs_ElS:NestrukturiraniPodatki">
							<table class="table" width="800px"><tbody><tr><td>

								<xsl:if test="iZbs_ElS:NazivDokumenta != ''" >
									<h3><xsl:value-of select="iZbs_ElS:NazivDokumenta"/></h3>
								</xsl:if>
									
								<xsl:if test="iZbs_ElS:VsebinaNadNazivomDokumenta != ''" >
									<p><xsl:value-of select="iZbs_ElS:VsebinaNadNazivomDokumenta"/></p>
								</xsl:if>
									
								<xsl:if test="iZbs_ElS:VsebinaPodNazivomDokumenta != ''" >
									<p><xsl:value-of select="iZbs_ElS:VsebinaPodNazivomDokumenta"/></p>
								</xsl:if>
								
								<xsl:if test="iZbs_ElS:VsebinaIzrek != ''" >
									<h4>Izrek</h4>
									<p><xsl:value-of select="iZbs_ElS:VsebinaIzrek"/></p>
								</xsl:if>
								
								<xsl:if test="iZbs_ElS:VsebinaObrazlozitev != ''" >
									<h4>Obrazložitev</h4>
									<p><xsl:value-of select="iZbs_ElS:VsebinaObrazlozitev"/></p>
								</xsl:if>
								
								<xsl:if test="iZbs_ElS:VsebinaPravniPouk != ''" >
									<h4>Pravni pouk</h4>
									<p><xsl:value-of select="iZbs_ElS:VsebinaPravniPouk"/></p>
								</xsl:if>
								
								<xsl:if test="iZbs_ElS:VsebinaNadOdgovornimiOsebami != ''" >
									<p><xsl:value-of select="iZbs_ElS:VsebinaNadOdgovornimiOsebami"/></p>
								</xsl:if>
	
								<xsl:if test="iZbs_ElS:OdgovorneOsebe != ''" >
									<table align="center" width="680px">
										<tbody>
											<tr>
												<td width="300px" align="center">Postopek vodil/-a:</td>
												<td width="80px"></td>
												<td width="300px" align="center">Odgovorne osebe:</td>
											</tr>
											<tr>
												<td align="center"><div><xsl:value-of select="iZbs_ElS:OdgovorneOsebe/iZbs_ElS:VodjaPostopkaImeInPriimek"/></div></td>
												<td></td>
												<td align="center"><xsl:value-of select="iZbs_ElS:OdgovorneOsebe/iZbs_ElS:OdgovornaOsebaImeInPriimek"/></td>
											</tr>
											<tr>
												<td align="center"><div><xsl:value-of select="iZbs_ElS:OdgovorneOsebe/iZbs_ElS:VodjaPostopkaDodatenOpis"/></div></td>
												<td></td>
												<td align="center"><xsl:value-of select="iZbs_ElS:OdgovorneOsebe/iZbs_ElS:OdgovornaOsebaDodatenOpis"/></td>
											</tr>
										</tbody>
									</table>
									
								</xsl:if>
								
								<xsl:if test="iZbs_ElS:VsebinaPodOdgovornimiOsebami != ''" >
									<p><xsl:value-of select="iZbs_ElS:VsebinaPodOdgovornimiOsebami"/></p>
								</xsl:if>
							</td></tr></tbody></table>
						</xsl:for-each>

					</xsl:for-each>
					<!-- konec Predlog oznake -->
				</xsl:for-each>
				<!-- konec Predlogi oznake -->
				<hr/>
				<p>KONEC_VSEBINE</p>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
