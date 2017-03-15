<!-- 
Podrocje: e-izvrsbe
Opis:     Pretvornik XML datotek sodisca v format ZBS IzvrsbeXML.
Verzija:  0.01
Datum zadnje spremembe: 2.12.2016
-->
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:izv="http://sodisce.si/izvrsba"

	xmlns:dSignature="http://www.w3.org/2000/09/xmldsig#"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:iZbs_AS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_AkcijeSklepov.xsd"
	xmlns:iZbs_Body="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_Body.xsd"
	xmlns:iZbs_DocAS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_DokumentAkcijSklepov.xsd" xmlns:iZbs_ElDoc="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ElementiDokumenta.xsd" xmlns:iZbs_ElS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ElementiSklepa.xsd" xmlns:iZbs_Spl="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_SplosniTipi.xsd" xmlns:iZbs_Zaglavje="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ZaglavjeSporocil.xsd"
	xmlns:iZbs_DokVseb="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_DokumentiVsebinski.xsd"
>
<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

<!-- ************************************************************************
     TEST predpogojev za ustrezno transformacijo v format ZBS IzvrsbeXML
     ************************************************************************ -->
<xsl:template match="/">
	<xsl:choose>
		<xsl:when test="not(/izv:OpisPosiljke)">
			<xsl:call-template name="VHODNI_DOKUMENT_NAPACEN">
				 <xsl:with-param name="opisNapake">OPIS NAPAKE: Vhodni XML dokument ne vsebuje elementa 'izv:OpisPosiljke'!</xsl:with-param>
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="not(/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti)">
			<xsl:call-template name="VHODNI_DOKUMENT_NAPACEN">
				 <xsl:with-param name="opisNapake">OPIS NAPAKE: Vhodni XML dokument ne vsebuje elementa '... izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti'!</xsl:with-param>
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="not(/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje[@sifProcesnoDejanje='568'])">  <!-- procesno dejanje 568=VL predlog za izvrsbo; transformira lahko samo izvrsbe z opravilno stevilko VL% (VrstaListine=VERODOSTOJNA_LISTINA), NE transformira pa zapisov z opravilnimi stevilkami npr. I IG IN (VrstaListine=IZVRSILNI_NASLOV) -->
			<xsl:call-template name="VHODNI_DOKUMENT_NAPACEN">
				 <xsl:with-param name="opisNapake">OPIS NAPAKE: Vhodni XML dokument ne vsebuje procesnega dejanja tipa 568 (=VL predlog za izvrsbo)! Izvrsbe, pri katerih se opravilne stevilke zacnejo z I (npr.: I, IG, IN; vrstaListine=IZVRSILNI_NASLOV) niso podprte za transformacijo.</xsl:with-param>
			</xsl:call-template>
		</xsl:when>		
		
		<xsl:otherwise>
			<xsl:call-template name="VHODNI_DOKUMENT_USTREZEN"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>


<!-- ************************************************************************
     Standardna vsebina
     ************************************************************************ -->
<xsl:template name="VHODNI_DOKUMENT_USTREZEN">
	<iZbs_DokVseb:PrevzemiDokumentXmlResponse xsi:schemaLocation="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_DokumentiVsebinski.xsd IzvrsbeXml_DokumentiVsebinski.xsd" xmlns:dSignature="http://www.w3.org/2000/09/xmldsig#" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:iZbs_AS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_AkcijeSklepov.xsd" xmlns:iZbs_Body="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_Body.xsd" xmlns:iZbs_DocAS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_DokumentAkcijSklepov.xsd" xmlns:iZbs_ElDoc="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ElementiDokumenta.xsd" xmlns:iZbs_ElS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ElementiSklepa.xsd" xmlns:iZbs_Spl="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_SplosniTipi.xsd" xmlns:iZbs_Zaglavje="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ZaglavjeSporocil.xsd" xmlns:iZbs_DokVseb="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_DokumentiVsebinski.xsd" >

		<!-- **************** HEADER *************** -->
		<iZbs_DokVseb:Header>
			<iZbs_Zaglavje:DatumSporocila><xsl:value-of select="izv:pretvoriDatumSodisca2DatumInUra(/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje[@sifProcesnoDejanje='566']/izv:PotrdiloOPravnomocnosti/izv:datumPravnomocnosti)"/></iZbs_Zaglavje:DatumSporocila>
			<iZbs_Zaglavje:IdSporocila>
				<iZbs_Spl:Id><xsl:call-template name="ID_DOKUMENTA"/></iZbs_Spl:Id>
				<iZbs_Spl:DavcnaStevilka>
					<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
					<iZbs_Spl:Vrednost><xsl:call-template name="DAVCNA_STEVILKA_SODISCA"/></iZbs_Spl:Vrednost>
				</iZbs_Spl:DavcnaStevilka>
			</iZbs_Zaglavje:IdSporocila>
			
			<xsl:for-each select="izv:OpisPosiljke/izv:Glava/izv:Prejemnik">
				<iZbs_Zaglavje:PrejemnikSporocila>
					<iZbs_Spl:DavcnaStevilka>
						<iZbs_Spl:Drzava><xsl:value-of select="izv:pretvoriDrzavo(izv:Naslov/@drzava)"/></iZbs_Spl:Drzava>
						<iZbs_Spl:Vrednost><xsl:value-of select="@davcna" /></iZbs_Spl:Vrednost>
					</iZbs_Spl:DavcnaStevilka>
					<iZbs_Spl:Naziv><xsl:value-of select="@priimek-naziv"/></iZbs_Spl:Naziv>
					<xsl:for-each select="izv:Naslov">
						<iZbs_Spl:Naslov>
							<iZbs_Spl:Ulica><xsl:value-of select="@ulica"/></iZbs_Spl:Ulica>
							<iZbs_Spl:HisnaStevilka><xsl:value-of select="@hisnaStevilka"/></iZbs_Spl:HisnaStevilka>
							<iZbs_Spl:Kraj><xsl:value-of select="@kraj"/></iZbs_Spl:Kraj>
							<iZbs_Spl:Posta><xsl:value-of select="@sifPosta"/></iZbs_Spl:Posta>
							<iZbs_Spl:PostniPredal/>
							<iZbs_Spl:Drzava><xsl:value-of select="izv:pretvoriDrzavo(@drzava)"/></iZbs_Spl:Drzava>
							<iZbs_Spl:ElektronskiPostniNaslov></iZbs_Spl:ElektronskiPostniNaslov>
							<iZbs_Spl:Telefon></iZbs_Spl:Telefon>
						</iZbs_Spl:Naslov>
					</xsl:for-each>
				</iZbs_Zaglavje:PrejemnikSporocila>
			</xsl:for-each>
			
			<xsl:for-each select="izv:OpisPosiljke/izv:Glava/izv:Posiljatelj">
				<iZbs_Zaglavje:PosiljateljSporocila>
					<iZbs_Spl:DavcnaStevilka>
						<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
						<iZbs_Spl:Vrednost><xsl:call-template name="DAVCNA_STEVILKA_SODISCA"/></iZbs_Spl:Vrednost>
					</iZbs_Spl:DavcnaStevilka>
					<iZbs_Spl:Naziv><xsl:value-of select="@ime-naziv"/></iZbs_Spl:Naziv>
					<xsl:for-each select="izv:Naslov">
						<iZbs_Spl:Naslov>
							<iZbs_Spl:Ulica><xsl:value-of select="@ulica"/></iZbs_Spl:Ulica>
							<iZbs_Spl:HisnaStevilka><xsl:value-of select="@hisnaStevilka"/></iZbs_Spl:HisnaStevilka>
							<iZbs_Spl:Kraj><xsl:value-of select="@kraj"/></iZbs_Spl:Kraj>
							<iZbs_Spl:Posta><xsl:value-of select="@sifPosta"/></iZbs_Spl:Posta>
							<iZbs_Spl:PostniPredal/>
							<iZbs_Spl:Drzava><xsl:value-of select="izv:pretvoriDrzavo(@drzava)"/></iZbs_Spl:Drzava>
							<iZbs_Spl:ElektronskiPostniNaslov>info@sodisce.si????</iZbs_Spl:ElektronskiPostniNaslov>
							<iZbs_Spl:Telefon>01 4747600</iZbs_Spl:Telefon>
						</iZbs_Spl:Naslov>
					</xsl:for-each>
					<iZbs_Spl:Enota>
						<iZbs_Spl:MaticnaStevilkaEnoteId><xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje[1]/izv:Predlog/izv:Sodisce/izv:maticnaSt"/></iZbs_Spl:MaticnaStevilkaEnoteId>
						<iZbs_Spl:NazivEnote><xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje[1]/izv:Predlog/izv:Sodisce/izv:nazivSodisce"/></iZbs_Spl:NazivEnote>
					</iZbs_Spl:Enota>
				</iZbs_Zaglavje:PosiljateljSporocila>
			</xsl:for-each>		
			
			<iZbs_Zaglavje:TestAliProdukcija>PRODUKCIJA</iZbs_Zaglavje:TestAliProdukcija>
			<iZbs_Zaglavje:IdIzvornegaSistema>SODISCE-IS01</iZbs_Zaglavje:IdIzvornegaSistema>
			<iZbs_Zaglavje:Verzija>
				<iZbs_Zaglavje:VerzijaStoritve>1.10</iZbs_Zaglavje:VerzijaStoritve>
				<iZbs_Zaglavje:VerzijaXsdShemeSporocila>1.10</iZbs_Zaglavje:VerzijaXsdShemeSporocila>
			</iZbs_Zaglavje:Verzija>
		</iZbs_DokVseb:Header>
		
		<iZbs_DokVseb:ResponseHeader>
			<iZbs_Zaglavje:ResponseStatus>OK</iZbs_Zaglavje:ResponseStatus>
		</iZbs_DokVseb:ResponseHeader>
		
		<!-- **************** BODY *************** -->
		<iZbs_DokVseb:Body>
			<iZbs_Body:DokumentAkcijSklepov>
			
				<iZbs_DocAS:OsnovniPodatkiDokumenta>
					<iZbs_ElDoc:IdDokumenta>
						<iZbs_Spl:Id><xsl:call-template name="ID_DOKUMENTA"/></iZbs_Spl:Id>
						<iZbs_Spl:DavcnaStevilka>
							<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
							<iZbs_Spl:Vrednost><xsl:call-template name="DAVCNA_STEVILKA_SODISCA"/></iZbs_Spl:Vrednost>
						</iZbs_Spl:DavcnaStevilka>
					</iZbs_ElDoc:IdDokumenta>
					<iZbs_ElDoc:VerzijaSheme>1.10</iZbs_ElDoc:VerzijaSheme>
					<iZbs_ElDoc:VerzijaSifrantov>1.10</iZbs_ElDoc:VerzijaSifrantov>
					<iZbs_ElDoc:DatumInUraDokumenta><xsl:value-of select="izv:pretvoriDatumSodisca2DatumInUra(/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje[@sifProcesnoDejanje='566']/izv:PotrdiloOPravnomocnosti/izv:trenutniDatum)"/></iZbs_ElDoc:DatumInUraDokumenta>
					<iZbs_ElDoc:OpombeDokumenta>
						<iZbs_Spl:Opomba>
							<iZbs_Spl:KategorijaOpombe>OBVEZNO_PREBERI</iZbs_Spl:KategorijaOpombe>
							<iZbs_Spl:OpombaTekst>Dokument je kreiran s pomočjo Sodisce2ZbsIzvrsbeXml_v1.00.xslt</iZbs_Spl:OpombaTekst>
						</iZbs_Spl:Opomba>
					</iZbs_ElDoc:OpombeDokumenta>
				</iZbs_DocAS:OsnovniPodatkiDokumenta>
				
				<iZbs_DocAS:PaketAkcijSklepov>
					<iZbs_DocAS:SplosniPodatkiOPaketu>
						<iZbs_ElDoc:IdPaketa>
							<iZbs_Spl:Id><xsl:call-template name="ID_DOKUMENTA"/></iZbs_Spl:Id>
							<iZbs_Spl:DavcnaStevilka>
								<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
								<iZbs_Spl:Vrednost><xsl:call-template name="DAVCNA_STEVILKA_SODISCA"/></iZbs_Spl:Vrednost>
							</iZbs_Spl:DavcnaStevilka>
						</iZbs_ElDoc:IdPaketa>
						<iZbs_ElDoc:DatumInUraIzdelave><xsl:value-of select="izv:pretvoriDatumSodisca2DatumInUra(/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje[@sifProcesnoDejanje='566']/izv:PotrdiloOPravnomocnosti/izv:trenutniDatum)"/></iZbs_ElDoc:DatumInUraIzdelave>
						<iZbs_ElDoc:SteviloZapisov>
							<xsl:value-of select="count(//izv:ProcesnoDejanje)"/>
						</iZbs_ElDoc:SteviloZapisov>
					</iZbs_DocAS:SplosniPodatkiOPaketu>
					
					<!-- *** AKCIJE SKLEPA *** -->
					<iZbs_DocAS:AkcijeSklepov>
					
						<!-- *** AKCIJA SKLEPA *** -->
						
						<xsl:for-each select="//izv:ProcesnoDejanje[@sifProcesnoDejanje=566]">
						
							<iZbs_DocAS:AkcijaSklepa xsi:type="iZbs_AS:NOV_SKLEP_Type">
								<iZbs_AS:IdAkcije>
									<iZbs_Spl:Id><xsl:call-template name="ID_DOKUMENTA"/></iZbs_Spl:Id>
									<iZbs_Spl:DavcnaStevilka>
										<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
										<iZbs_Spl:Vrednost><xsl:call-template name="DAVCNA_STEVILKA_SODISCA"/></iZbs_Spl:Vrednost>
									</iZbs_Spl:DavcnaStevilka>
								</iZbs_AS:IdAkcije>
								<iZbs_AS:DatumiAkcije>
									<iZbs_AS:DatumNastanka><xsl:value-of select="izv:pretvoriDatumSodisca2DatumInUra(/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje[@sifProcesnoDejanje='566']/izv:PotrdiloOPravnomocnosti/izv:trenutniDatum)"/></iZbs_AS:DatumNastanka>
								</iZbs_AS:DatumiAkcije>
								<iZbs_AS:OpombeAkcije>
									<iZbs_Spl:Opomba>
										<iZbs_Spl:KategorijaOpombe>INFORMACIJE</iZbs_Spl:KategorijaOpombe>
										<iZbs_Spl:OpombaTekst><xsl:value-of select="@nazivProcesnoDejanje"/></iZbs_Spl:OpombaTekst>
									</iZbs_Spl:Opomba>
								</iZbs_AS:OpombeAkcije>
								<iZbs_AS:Sklep>
									
									<!-- *********** vstavi odsek XML SKLEP_OSNOVNI_PODATKI *********** -->
									<xsl:call-template name="SKLEP_OSNOVNI_PODATKI">
									</xsl:call-template>
									<!-- *********** konec odseka o SKLEP_OSNOVNI_PODATKI *********** -->
									
									<!-- *********** vstavi odsek XML o DOLZNIKIH *********** -->
									<xsl:call-template name="DOLZNIKI">
									</xsl:call-template>
									<!-- *********** konec odseka o DOLZNIKIH *********** -->
									
									<!-- *********** vstavi odsek XML o UPNIKIH *********** -->
									<xsl:call-template name="UPNIKI">
									</xsl:call-template>
									<!-- *********** konec odseka o UPNIKIH *********** -->

									<!-- *********** vstavi odsek za TERJATVE *********** -->
									<xsl:call-template name="TERJATVE">
									</xsl:call-template>
									<!-- *********** konec odseka za TERJATVE *********** -->									
									
									<!-- *********** vstavi odsek za SKLEP_NESTRUKTURIRANI_PODATKI *********** -->
									<!-- <xsl:call-template name="SKLEP_NESTRUKTURIRANI_PODATKI"/> -->
									<!-- *********** konec odseka za SKLEP_NESTRUKTURIRANI_PODATKI *********** -->									
									
								</iZbs_AS:Sklep>
							</iZbs_DocAS:AkcijaSklepa>
							
						</xsl:for-each>  <!-- konec zanke za 'izv:ProcesnoDejanje[@sifProcesnoDejanje=566]' v XML dokumentu sodisca -->
						
						<!-- paket lahko vsebuje vec akcij, do 500 -->
					</iZbs_DocAS:AkcijeSklepov>
				</iZbs_DocAS:PaketAkcijSklepov>
			</iZbs_Body:DokumentAkcijSklepov>
		</iZbs_DokVseb:Body>

	</iZbs_DokVseb:PrevzemiDokumentXmlResponse>
</xsl:template>


<!-- ************************************************************************
     Napacen vhodni dokument ...
     ************************************************************************  -->

<xsl:template name="VHODNI_DOKUMENT_NAPACEN">
	<xsl:param name="opisNapake"/>

	<iZbs_DokVseb:PrevzemiDokumentXmlResponse xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:iZbs_AS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_AkcijeSklepov.xsd" xmlns:iZbs_Body="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_Body.xsd" xmlns:iZbs_DocAS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_DokumentAkcijSklepov.xsd" xmlns:iZbs_ElDoc="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ElementiDokumenta.xsd" xmlns:iZbs_ElS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ElementiSklepa.xsd" xmlns:iZbs_Spl="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_SplosniTipi.xsd" xmlns:iZbs_Zaglavje="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ZaglavjeSporocil.xsd" xmlns:iZbs_DokVseb="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_DokumentiVsebinski.xsd" xsi:schemaLocation="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_DokumentiVsebinski.xsd IzvrsbeXml_DokumentiVsebinski.xsd">
		<iZbs_DokVseb:Header>
			<iZbs_Zaglavje:DatumSporocila><xsl:value-of select="current-dateTime()"/></iZbs_Zaglavje:DatumSporocila>
			<iZbs_Zaglavje:IdSporocila>
				<iZbs_Spl:Id>ERROR</iZbs_Spl:Id>
				<iZbs_Spl:DavcnaStevilka >
					<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
					<iZbs_Spl:Vrednost>66807107</iZbs_Spl:Vrednost>
				</iZbs_Spl:DavcnaStevilka>
			</iZbs_Zaglavje:IdSporocila>
			<iZbs_Zaglavje:PosiljateljSporocila>
				<iZbs_Spl:DavcnaStevilka>
					<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
					<iZbs_Spl:Vrednost>66807107</iZbs_Spl:Vrednost>
				</iZbs_Spl:DavcnaStevilka>
				<iZbs_Spl:Naziv>Okrajno sodišče v Ljubljani, Izvršilni oddelek</iZbs_Spl:Naziv>
				<iZbs_Spl:Naslov>
					<iZbs_Spl:Ulica>Miklošičeva ul. 10</iZbs_Spl:Ulica>
					<iZbs_Spl:HisnaStevilka/>
					<iZbs_Spl:Kraj>Ljubljana</iZbs_Spl:Kraj>
					<iZbs_Spl:Posta>1000</iZbs_Spl:Posta>
					<iZbs_Spl:PostniPredal/>
					<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
					<iZbs_Spl:ElektronskiPostniNaslov>info@sodisce.si????</iZbs_Spl:ElektronskiPostniNaslov>
					<iZbs_Spl:Telefon>01 4747600</iZbs_Spl:Telefon>
				</iZbs_Spl:Naslov>
			</iZbs_Zaglavje:PosiljateljSporocila>
			<iZbs_Zaglavje:TestAliProdukcija>PRODUKCIJA</iZbs_Zaglavje:TestAliProdukcija>
			<iZbs_Zaglavje:IdIzvornegaSistema>SODISCE-IS01</iZbs_Zaglavje:IdIzvornegaSistema>
			<iZbs_Zaglavje:Verzija>
				<iZbs_Zaglavje:VerzijaStoritve>1.10</iZbs_Zaglavje:VerzijaStoritve>
				<iZbs_Zaglavje:VerzijaXsdShemeSporocila>1.10</iZbs_Zaglavje:VerzijaXsdShemeSporocila>
			</iZbs_Zaglavje:Verzija>
		</iZbs_DokVseb:Header>
		<iZbs_DokVseb:ResponseHeader>
			<iZbs_Zaglavje:ResponseStatus>ERROR</iZbs_Zaglavje:ResponseStatus>
			<iZbs_Zaglavje:Napake>
				<iZbs_Spl:Napaka>
					<iZbs_Spl:NapakaTip>KRITICNA_NAPAKA</iZbs_Spl:NapakaTip>
					<iZbs_Spl:NapakaKoda>20000-NAPAKA_XML</iZbs_Spl:NapakaKoda>
					<iZbs_Spl:NapakaOpis>Vhodni dokument ni pravilen. <xsl:value-of select="$opisNapake"/></iZbs_Spl:NapakaOpis>
				</iZbs_Spl:Napaka>
			</iZbs_Zaglavje:Napake>
		</iZbs_DokVseb:ResponseHeader>
		<iZbs_DokVseb:Body>
			<iZbs_Body:DokumentPrazenObNapaki/>
		</iZbs_DokVseb:Body>
	</iZbs_DokVseb:PrevzemiDokumentXmlResponse>

</xsl:template>  <!-- konec predloge VHODNI_DOKUMENT_NAPACEN -->


<!-- **********************************************
      PODSKLOPI XML DOKUMENTA 
*************************************************** -->
<!--
KODE PROCESNIH DEJANJ na sodiscu:
* 568 = VL predlog za izvršbo
* 566 = Potrdilo o pravnomočnosti in izvršljivosti
* 536 = Sklep o izvršbi
-->

<!-- **********************************************
      PODSKLOP SKLEPA - OSNOVNI PODATKI O SKLEPU
*************************************************** -->
<xsl:template name="SKLEP_OSNOVNI_PODATKI">
	<xsl:for-each select="//izv:ProcesnoDejanje[@sifProcesnoDejanje=566]/izv:PotrdiloOPravnomocnosti">   <!-- nabere podatke iz sklepa o izvrsbi -->

		<iZbs_ElS:IdSklepa>
			<iZbs_ElS:OpravilnaStevilka><xsl:value-of select="izv:OpravilnaStevilka/@vpisnik"/><xsl:value-of select="' '"/><xsl:value-of select="izv:OpravilnaStevilka/@zadevaStevilka"/>/<xsl:value-of select="izv:OpravilnaStevilka/@zadevaLeto"/> </iZbs_ElS:OpravilnaStevilka>
			<iZbs_ElS:IzdajateljDavcnaStevilka>
				<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
				<iZbs_Spl:Vrednost><xsl:call-template name="DAVCNA_STEVILKA_SODISCA"/></iZbs_Spl:Vrednost>
			</iZbs_ElS:IzdajateljDavcnaStevilka>
		</iZbs_ElS:IdSklepa>
		<iZbs_ElS:DatumiSklepa>
			<iZbs_ElS:DatumIzdaje><xsl:value-of select="izv:pretvoriDatumSodisca2Datum(izv:datumSklepaOIzvrsbi)"/></iZbs_ElS:DatumIzdaje>
			<iZbs_ElS:DatumIzvrsljivosti><xsl:value-of select="izv:pretvoriDatumSodisca2Datum(izv:datumPravnomocnosti)"/></iZbs_ElS:DatumIzvrsljivosti>
			<iZbs_ElS:DatumVrocitve xsi:nil="true" />
		</iZbs_ElS:DatumiSklepa>
		
		<iZbs_ElS:StatusSklepaPriSubjektu>
			<xsl:choose>
				<xsl:when test="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:SklepOIzvrsbi/izv:datumSklepaOIzvrsbi">
					<iZbs_ElS:StatusSklepa>30000-IZVRSLJIV</iZbs_ElS:StatusSklepa>
				</xsl:when>
				<xsl:otherwise>
					<iZbs_ElS:StatusSklepa>30001-NEIZVRSLJIV</iZbs_ElS:StatusSklepa>
				</xsl:otherwise>
			</xsl:choose>
			<iZbs_ElS:SubjektDavcnaStevilka>
				<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
				<iZbs_Spl:Vrednost><xsl:call-template name="DAVCNA_STEVILKA_SODISCA"/></iZbs_Spl:Vrednost>
			</iZbs_ElS:SubjektDavcnaStevilka>
			<iZbs_ElS:DatumZapisaStatusa><xsl:value-of select="izv:pretvoriDatumSodisca2DatumInUra(izv:datumSklepaOIzvrsbi)"/></iZbs_ElS:DatumZapisaStatusa>
		</iZbs_ElS:StatusSklepaPriSubjektu>
		<iZbs_ElS:KlasifikatorjiSklepa>
			<iZbs_ElS:VrstaDolga>SODNI</iZbs_ElS:VrstaDolga>
			<iZbs_ElS:VrstaListine>VERODOSTOJNA_LISTINA</iZbs_ElS:VrstaListine>
			<iZbs_ElS:OznakaListine>????</iZbs_ElS:OznakaListine>
		</iZbs_ElS:KlasifikatorjiSklepa>

	</xsl:for-each>

</xsl:template>  <!-- konec sklopa SKLEP_OSNOVNI_PODATKI -->

<!-- **********************************************
      PODSKLOP SKLEPA - OPRAVILNA STEVILKA
*************************************************** -->
<xsl:template name="SKLEP_OPRAVILNA_STEVILKA">
	<xsl:for-each select="//izv:ProcesnoDejanje[@sifProcesnoDejanje=566]/izv:PotrdiloOPravnomocnosti/izv:OpravilnaStevilka">   <!-- nabere podatke iz sklepa o izvrsbi -->
		<iZbs_ElS:IdSklepa>
			<iZbs_ElS:OpravilnaStevilka><xsl:value-of select="@vpisnik"/> <xsl:value-of select="@zadevaStevilka"/>  <xsl:value-of select="@zadevaLeto"/></iZbs_ElS:OpravilnaStevilka>
			<iZbs_ElS:IzdajateljDavcnaStevilka>
				<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
				<iZbs_Spl:Vrednost><xsl:call-template name="DAVCNA_STEVILKA_SODISCA"/></iZbs_Spl:Vrednost>
			</iZbs_ElS:IzdajateljDavcnaStevilka>
		</iZbs_ElS:IdSklepa>
	</xsl:for-each>

</xsl:template>  <!-- konec sklopa SKLEP_OPRAVILNA_STEVILKA -->

<!-- **********************************************
      PODSKLOP SKLEPA - OSNOVNI PODATKI O SKLEPU
*************************************************** -->
<xsl:template name="SKLEP_NESTRUKTURIRANI_PODATKI">

	<iZbs_ElS:NestrukturiraniPodatki>
		<iZbs_ElS:NazivDokumenta>SKLEP SODIŠČA</iZbs_ElS:NazivDokumenta>
		<iZbs_ElS:VsebinaNadNazivomDokumenta></iZbs_ElS:VsebinaNadNazivomDokumenta>
		<iZbs_ElS:VsebinaPodNazivomDokumenta/>
		<iZbs_ElS:VsebinaIzrek></iZbs_ElS:VsebinaIzrek>
		<iZbs_ElS:VsebinaObrazlozitev></iZbs_ElS:VsebinaObrazlozitev>
		<iZbs_ElS:VsebinaPravniPouk></iZbs_ElS:VsebinaPravniPouk>
		<iZbs_ElS:VsebinaNadOdgovornimiOsebami/>
		<iZbs_ElS:VsebinaPodOdgovornimiOsebami/>
		<iZbs_ElS:OdgovorneOsebe>
			<iZbs_ElS:VodjaPostopkaImeInPriimek></iZbs_ElS:VodjaPostopkaImeInPriimek>
			<iZbs_ElS:VodjaPostopkaDodatenOpis></iZbs_ElS:VodjaPostopkaDodatenOpis>
			<iZbs_ElS:OdgovornaOsebaImeInPriimek></iZbs_ElS:OdgovornaOsebaImeInPriimek>
			<iZbs_ElS:OdgovornaOsebaDodatenOpis></iZbs_ElS:OdgovornaOsebaDodatenOpis>
		</iZbs_ElS:OdgovorneOsebe>
	</iZbs_ElS:NestrukturiraniPodatki>
	
</xsl:template>  <!-- konec sklopa SKLEP_OSNOVNI_PODATKI -->


<!-- **********************************************
      PODATKI O DOLZNIKIH
*************************************************** -->
<xsl:template name="DOLZNIKI">

	<xsl:variable name="steviloDolznikov" select="count(//izv:ProcesnoDejanje[@sifProcesnoDejanje=566]/izv:PotrdiloOPravnomocnosti/izv:Dolzniki/izv:Dolznik/izv:Oseba)" /> 

	<iZbs_ElS:Dolzniki>
		<xsl:for-each select="//izv:ProcesnoDejanje[@sifProcesnoDejanje=566]/izv:PotrdiloOPravnomocnosti/izv:Dolzniki/izv:Dolznik/izv:Oseba">   <!-- nabere podatke iz sklepa o izvrsbi -->
			<iZbs_ElS:Dolznik>
		
				<!-- *********** vstavi odsek izv:Oseba; podatki o SUBJEKTU (fizična oseba | pravna oseba | s.p.) *********** -->
				<xsl:call-template name="SUBJEKT">
					<xsl:with-param name="objekt_oseba" select="izv:Oseba"/>
				</xsl:call-template>
				<!-- *********** konec odseka izv:Oseba *********** -->
		
				<iZbs_ElS:TipDolznika><xsl:value-of select="izv:ediniDolznikAliSodolznik($steviloDolznikov)"/></iZbs_ElS:TipDolznika>   <!-- EDINI_DOLZNIK ali SODOLZNIK -->
				
				<!-- *********** vstavi odsek RACUNI_DOLZNIKA *********** -->
				<xsl:call-template name="RACUNI_DOLZNIKA"></xsl:call-template>
				<!-- *********** konec odseka RACUNI_DOLZNIKA *********** -->
		
			</iZbs_ElS:Dolznik>
		</xsl:for-each>
	</iZbs_ElS:Dolzniki>

</xsl:template>  <!-- konec sklopa DOLZNIKI -->


<!-- **********************************************
      PODATKI O SUBEKTU oz. osebi (uporablja se za UPNIKA, DOLŽNIKA itd.)
*************************************************** -->

<xsl:template name="SUBJEKT">

    <xsl:param name="objekt_oseba"/>
    
    <xsl:choose>

		<!-- FIZICNA OSEBA -->
		<xsl:when test="@vrsta='fizicna'">
			<iZbs_ElS:Subjekt xsi:type="iZbs_Spl:FizicnaOsebaType">
				<iZbs_Spl:Ime><xsl:value-of select="@ime-naziv"/></iZbs_Spl:Ime>
				<iZbs_Spl:Priimek><xsl:value-of select="@priimek-naziv"/></iZbs_Spl:Priimek>
				<xsl:choose>
					<xsl:when test="not(@datumRojstva)"><iZbs_Spl:DatumRojstva xsi:nil="true"/></xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@datumRojstva"/>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:choose>
					<xsl:when test="not(@davcna)"><iZbs_Spl:DavcnaStevilka xsi:nil="true"/></xsl:when>
					<xsl:otherwise>
						<iZbs_Spl:DavcnaStevilka>
							<iZbs_Spl:Drzava><xsl:value-of select="izv:pretvoriDrzavo(@drzava)"/></iZbs_Spl:Drzava>
							<iZbs_Spl:Vrednost><xsl:value-of select="@davcna"/></iZbs_Spl:Vrednost>
						</iZbs_Spl:DavcnaStevilka>
					</xsl:otherwise>
				</xsl:choose>
				<iZbs_Spl:Emso><xsl:value-of select="@maticna"/></iZbs_Spl:Emso>
				
				<!-- Vstavi podatke o naslovu -->
				<xsl:apply-templates select="izv:Naslov"/>
			</iZbs_ElS:Subjekt>
		</xsl:when>
					
		<!-- PRAVNA OSEBA -->
		<xsl:when test="@vrsta='pravna'">
			<iZbs_ElS:Subjekt xmlns:iZbs_Spl="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_SplosniTipi.xsd" xsi:type="iZbs_Spl:PravnaOsebaType">
				<iZbs_Spl:FirmaNaziv><xsl:value-of select="@priimek-naziv"/></iZbs_Spl:FirmaNaziv>
				<xsl:choose>
					<xsl:when test="not(@davcna)"><iZbs_Spl:DavcnaStevilka xsi:nil="true"/></xsl:when>
					<xsl:otherwise>
						<iZbs_Spl:DavcnaStevilka>
							<iZbs_Spl:Drzava><xsl:value-of select="izv:pretvoriDrzavo(@drzava)"/></iZbs_Spl:Drzava>
							<iZbs_Spl:Vrednost><xsl:value-of select="@davcna"/></iZbs_Spl:Vrednost>
						</iZbs_Spl:DavcnaStevilka>
					</xsl:otherwise>
				</xsl:choose>
				<iZbs_Spl:MaticnaStevilka><xsl:value-of select="@maticna"/></iZbs_Spl:MaticnaStevilka>
				
				<!-- Vstavi podatke o naslovu -->
				<xsl:apply-templates select="izv:Naslov"/>
			</iZbs_ElS:Subjekt>
		</xsl:when>
			
		<!-- S.P.-->
		<xsl:when test="@vrsta='s.p.'">
			<iZbs_ElS:Subjekt xmlns:iZbs_Spl="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_SplosniTipi.xsd" xsi:type="iZbs_Spl:PodjetnikType">
				<iZbs_Spl:Ime><xsl:value-of select="@ime-naziv"/></iZbs_Spl:Ime>
				<iZbs_Spl:Priimek><xsl:value-of select="@priimek-naziv"/></iZbs_Spl:Priimek>
				<iZbs_Spl:DatumRojstva xsi:nil="true"/>
				<xsl:choose>
					<xsl:when test="not(@davcna)"><iZbs_Spl:DavcnaStevilka xsi:nil="true"/></xsl:when>
					<xsl:otherwise>
						<iZbs_Spl:DavcnaStevilka>
							<iZbs_Spl:Drzava><xsl:value-of select="izv:pretvoriDrzavo(@drzava)"/></iZbs_Spl:Drzava>
							<iZbs_Spl:Vrednost><xsl:value-of select="@davcna"/></iZbs_Spl:Vrednost>
						</iZbs_Spl:DavcnaStevilka>
					</xsl:otherwise>
				</xsl:choose>
				<iZbs_Spl:Emso></iZbs_Spl:Emso>
				
				<!-- Vstavi podatke o naslovu -->
				<xsl:apply-templates select="izv:Naslov"/>
	
				<iZbs_Spl:FirmaNaziv><xsl:value-of select="@priimek-naziv"/></iZbs_Spl:FirmaNaziv>
				<iZbs_Spl:MaticnaStevilkaFirme><xsl:value-of select="@maticna"/></iZbs_Spl:MaticnaStevilkaFirme>
			</iZbs_ElS:Subjekt>
		</xsl:when>
	
		<xsl:otherwise><iZbs_ElS:Subjekt>NAPAKA - neznani tip subjekta (ni: fizicna | pravna | sp)!</iZbs_ElS:Subjekt></xsl:otherwise>

	</xsl:choose>

</xsl:template>    <!-- konec sklopa izv:Oseba -->


<!-- **********************************************
  PODATKI O NASLOVU (v okviru fizične, pravne osebe ali s.p.)
*************************************************** -->
<xsl:template match="izv:Naslov">
	<iZbs_Spl:Naslov>
		<iZbs_Spl:Ulica><xsl:value-of select="@ulica"/></iZbs_Spl:Ulica>
		<iZbs_Spl:HisnaStevilka><xsl:value-of select="@hisnaStevilka"/></iZbs_Spl:HisnaStevilka>
		<xsl:if test="izv:Naslov/@kraj">
			<iZbs_Spl:Kraj><xsl:value-of select="@kraj"/></iZbs_Spl:Kraj>
		</xsl:if>
		<xsl:if test="not(izv:Naslov/@kraj)">
			<iZbs_Spl:Kraj><xsl:value-of select="@nazivPosta"/></iZbs_Spl:Kraj>
		</xsl:if>
		<iZbs_Spl:Posta><xsl:value-of select="@sifPosta"/></iZbs_Spl:Posta>
		<iZbs_Spl:PostniPredal></iZbs_Spl:PostniPredal>
		<iZbs_Spl:Drzava><xsl:value-of select="izv:pretvoriDrzavo(@drzava)"/></iZbs_Spl:Drzava>
	</iZbs_Spl:Naslov>
</xsl:template>    <!-- konec sklopa izv:Naslov -->


<!-- **********************************************
      PODATKI O RACUNIH DOLZNIKA
      Opomba: na podlagi trenutne sheme sodisca ni moc enolicno definirati racunov k ustreznim osebam!!!!????
*************************************************** -->
<xsl:template name="RACUNI_DOLZNIKA">

	<iZbs_ElS:BankeInRacuni>
	
	<xsl:for-each select="//izv:ProcesnoDejanje[@sifProcesnoDejanje=566]/izv:PotrdiloOPravnomocnosti/izv:RacunDolznika">
		<iZbs_ElS:BankaInRacuni Rang="{position()}">
			<iZbs_ElS:BICKodaBanke>NEZNAN99</iZbs_ElS:BICKodaBanke>
			<iZbs_ElS:NazivBanke>NEZNANA_BANKA</iZbs_ElS:NazivBanke>
			<iZbs_ElS:TrrSeznam>
				<iZbs_ElS:TrrIbanStevilka Rang="{position()}"><xsl:value-of select="izv:pretvoriRacun(@racun)"/></iZbs_ElS:TrrIbanStevilka>
			</iZbs_ElS:TrrSeznam>
		</iZbs_ElS:BankaInRacuni>
	</xsl:for-each>
	
	</iZbs_ElS:BankeInRacuni>

</xsl:template>  <!-- konec sklopa RACUNI_DOLZNIKA -->


<!-- **********************************************
      PODATKI O UPNIKIH
*************************************************** -->
<xsl:template name="UPNIKI">

	<xsl:for-each select="//izv:ProcesnoDejanje[@sifProcesnoDejanje=566]/izv:PotrdiloOPravnomocnosti/izv:Upniki/izv:Upniki">
		<xsl:variable name="upnikiSpremenljivka" select="."/>  <!-- spremenljivka Upniki lahko vsebuje poljubno število oseb (tj. upnikov), pooblascencev, zastopnikov -->

		<!-- zanka vseh oseb znotraj elementa Upniki -->
		<xsl:for-each select="izv:Oseba">
		
			<iZbs_ElS:Upnik>
			
				<!-- *********** vstavi odsek izv:Oseba; podatki o SUBJEKTU (fizična oseba | pravna oseba | s.p.) *********** -->
				<xsl:call-template name="SUBJEKT">
					<xsl:with-param name="objekt_oseba" select="."/>
				</xsl:call-template>
				<!-- *********** konec odseka izv:Oseba *********** -->

				<!-- ************************************ -->
				<!-- preverimo, ce ima Oseba tudi pripadajoce Pooblascence, ki jih je lahko poljubno veliko -->
				<xsl:for-each select="$upnikiSpremenljivka/izv:Pooblascenec">
					<iZbs_ElS:Pooblascenec>
						<iZbs_ElS:TipPooblascenca>POOBLASCENEC</iZbs_ElS:TipPooblascenca>
						
						<!-- *********** vstavi odsek izv:Oseba; podatki o SUBJEKTU (fizična oseba | pravna oseba | s.p.) *********** -->
						<xsl:call-template name="SUBJEKT">
							<xsl:with-param name="objekt_oseba" select="."/>
						</xsl:call-template>
						<!-- *********** konec odseka izv:Oseba *********** -->

					</iZbs_ElS:Pooblascenec>
				</xsl:for-each> <!-- konec zanke za Pooblascence, ki jih prilepimo k elementu Oseba -->
			
				<!-- ************************************ -->
				<!-- preveri, ce ima Oseba tudi pripadajoce ZASTOPNIKE, ki jih je lahko poljubno veliko -->
				<xsl:for-each select="$upnikiSpremenljivka/izv:Zastopnik">
					<iZbs_ElS:Pooblascenec>
						<iZbs_ElS:TipPooblascenca>ZASTOPNIK</iZbs_ElS:TipPooblascenca>
							
						<!-- *********** vstavi odsek izv:Oseba; podatki o SUBJEKTU (fizična oseba | pravna oseba | s.p.) *********** -->
						<xsl:call-template name="SUBJEKT">
							<xsl:with-param name="objekt_oseba" select="."/>
						</xsl:call-template>
						<!-- *********** konec odseka izv:Oseba *********** -->

					</iZbs_ElS:Pooblascenec>
				</xsl:for-each> <!-- konec zanke za ZASTOPNIKE, ki jih prilepimo k elementu Oseba -->
			
			</iZbs_ElS:Upnik>
			
		</xsl:for-each> <!-- konec zanke za vse elemente OSEBA, znotraj elementa Upniki -->
		
	</xsl:for-each>  <!-- konec zanke za vse elemente Upniki -->

</xsl:template>  <!-- konec sklopa UPNIKI -->


<!-- **********************************************
      PODATKI O TERJATVAH
*************************************************** -->
<xsl:template name="TERJATVE">

	<iZbs_ElS:Terjatve>

	<!-- terjatve VerodostojnaListina se gledajo v procesnem dejanju 568 (=VL predlog za izvrsbo) -->
	<xsl:for-each select="//izv:ProcesnoDejanje[@sifProcesnoDejanje=568]/izv:Predlog/izv:OznakaZahtevka/izv:VerodostojneListine/izv:VerodostojnaListina">
		<iZbs_ElS:Terjatev>
			<iZbs_ElS:TipTerjatve>
				<xsl:choose>
					<xsl:when test="@sifListina='1'">GLAVNICA</xsl:when>
					<xsl:when test="@sifListina='2'">GLAVNICA</xsl:when>
					<xsl:when test="@sifListina='3'">GLAVNICA</xsl:when>
					<xsl:when test="@sifListina='4'">GLAVNICA</xsl:when>
					<xsl:when test="@sifListina='5'">GLAVNICA</xsl:when>
					<xsl:when test="@sifListina='6'">GLAVNICA</xsl:when>
					<xsl:when test="@sifListina='7'">STROSKI</xsl:when>
					<xsl:when test="@sifListina='8'">STROSKI</xsl:when>
					<xsl:when test="@sifListina='9'">STROSKI</xsl:when>
					<xsl:when test="@sifListina='10'">STROSKI</xsl:when>
					<xsl:otherwise>NEZNANA_VHODNA_VREDNOST (<xsl:value-of select="@sifListina"/>)</xsl:otherwise>
				</xsl:choose>
			</iZbs_ElS:TipTerjatve>  <!-- STROSKI ali GLAVNICA -->
			<iZbs_ElS:ZnesekZValuto>
				<iZbs_Spl:Znesek><xsl:value-of select="@znesek"/></iZbs_Spl:Znesek>
				<iZbs_Spl:Valuta><xsl:value-of select="@valuta"/></iZbs_Spl:Valuta>
			</iZbs_ElS:ZnesekZValuto>

			<!-- OPOMBA: ko ni obresti, lahko izpustimo celoten sklop elementa <ObrestiZaTerjatev> -->
	
			<!-- *** Zanka za vse elemente tipa 'ZamudneObresti'; vsak element lahko vsebuje poljubno število podeleemntov ObrestZakonska in ObrestPogodbena -->
			<xsl:for-each select="izv:ZamudneObresti">
			
				<!-- *** Zanka za vse elemente tipa ObrestZakonska znotraj elementa 'ZamudneObresti' -->
				<xsl:for-each select="izv:ObrestZakonska">
					<iZbs_ElS:ObrestiZaTerjatev>
						<iZbs_ElS:TipObresti>ZAMUDNE_OBRESTI_PO_OZ</iZbs_ElS:TipObresti>
						<iZbs_ElS:OpisZaTipObresti></iZbs_ElS:OpisZaTipObresti>
						<xsl:choose>
							<xsl:when test="@odDatuma"><iZbs_ElS:DatumObrestovanjaOd><xsl:value-of select="izv:pretvoriDatumSodisca2Datum(@odDatuma)"/></iZbs_ElS:DatumObrestovanjaOd></xsl:when>
							<xsl:when test="not(@odDatuma)"><iZbs_ElS:DatumObrestovanjaOd xsi:nil="true"/></xsl:when>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="@doDatuma"><iZbs_ElS:DatumObrestovanjaDo><xsl:value-of select="izv:pretvoriDatumSodisca2Datum(@doDatuma)"/></iZbs_ElS:DatumObrestovanjaDo></xsl:when>
							<xsl:when test="not(@doDatuma)"><iZbs_ElS:DatumObrestovanjaDo xsi:nil="true"/></xsl:when>
						</xsl:choose>						
						<iZbs_ElS:TrrPrejemnikaZaObresti><xsl:value-of select="izv:pretvoriRacun(/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Oseba[1]/izv:RacunZaNakazilo/@racun)"/></iZbs_ElS:TrrPrejemnikaZaObresti>
						<iZbs_ElS:SklicZaNakaziloZaObresti><iZbs_Spl:StrukturiranaReferencaSI><xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Oseba[1]/izv:RacunZaNakazilo/@sklic"/></iZbs_Spl:StrukturiranaReferencaSI></iZbs_ElS:SklicZaNakaziloZaObresti>
					</iZbs_ElS:ObrestiZaTerjatev>
				</xsl:for-each>  <!-- konec zanke za elemente tipa ObrestZakonska znotraj elementa 'ZamudneObresti' -->
				
				<!-- *** Zanka za vse elemente tipa ObrestPogodbena znotraj elementa 'ZamudneObresti' -->
				<xsl:for-each select="izv:ObrestPogodbena">
					<iZbs_ElS:ObrestiZaTerjatev>
						<iZbs_ElS:TipObresti>GLEJ_OPIS</iZbs_ElS:TipObresti>
						<iZbs_ElS:StevilcniParameter>0</iZbs_ElS:StevilcniParameter>
						<iZbs_ElS:OpisZaTipObresti></iZbs_ElS:OpisZaTipObresti>
						<xsl:choose>
							<xsl:when test="@odDatuma"><iZbs_ElS:DatumObrestovanjaOd><xsl:value-of select="izv:pretvoriDatumSodisca2Datum(@odDatuma)"/></iZbs_ElS:DatumObrestovanjaOd></xsl:when>
							<xsl:when test="not(@odDatuma)"><iZbs_ElS:DatumObrestovanjaOd xsi:nil="true"/></xsl:when>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="@doDatuma"><iZbs_ElS:DatumObrestovanjaDo><xsl:value-of select="izv:pretvoriDatumSodisca2Datum(@doDatuma)"/></iZbs_ElS:DatumObrestovanjaDo></xsl:when>
							<xsl:when test="not(@doDatuma)"><iZbs_ElS:DatumObrestovanjaDo xsi:nil="true"/></xsl:when>
						</xsl:choose>
						<iZbs_ElS:TrrPrejemnikaZaObresti><xsl:value-of select="izv:pretvoriRacun(/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Oseba[1]/izv:RacunZaNakazilo/@racun)"/></iZbs_ElS:TrrPrejemnikaZaObresti>
						<iZbs_ElS:SklicZaNakaziloZaObresti><iZbs_Spl:StrukturiranaReferencaSI><xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Oseba[1]/izv:RacunZaNakazilo/@sklic"/></iZbs_Spl:StrukturiranaReferencaSI></iZbs_ElS:SklicZaNakaziloZaObresti>
					</iZbs_ElS:ObrestiZaTerjatev>
				</xsl:for-each>  <!-- konec zanke za elemente tipa ObrestPogodbena znotraj elementa 'ZamudneObresti' -->
			</xsl:for-each>  <!-- konec zanke za elemente tipa elemente tipa 'ZamudneObresti' -->

			<!-- *********** vstavi podatke o racunu za poplacilo terjatev *********** -->
			<xsl:call-template name="RACUNI_ZA_TERJATEV"/>
			<!-- *********** konec vstavljanja odseka RACUNI_ZA_TERJATEV *********** -->
			
		</iZbs_ElS:Terjatev>
	</xsl:for-each>	
	
	<!-- STROSKI UPNIKA se NE preberejo iz procesnega dejanja 568 (=VL predlog za izvrsbo), temvec iz proc. dejanja 536 (=sklep o izvrsbi) -->
	<xsl:for-each select="//izv:ProcesnoDejanje[@sifProcesnoDejanje=536]/izv:SklepOIzvrsbi">
		<iZbs_ElS:Terjatev>
			<iZbs_ElS:TipTerjatve>STROSKI</iZbs_ElS:TipTerjatve>
			<iZbs_ElS:ZnesekZValuto>
				<iZbs_Spl:Znesek><xsl:value-of select="izv:stroskiUpnika"/></iZbs_Spl:Znesek>
				<iZbs_Spl:Valuta><xsl:value-of select="izv:Valuta"/></iZbs_Spl:Valuta>
			</iZbs_ElS:ZnesekZValuto>
			
			<!-- OBRESTI za stroske upnika razbere iz procesnega dejanja 568 (=VL predlog za izvrsbo)?????? -->
			<!-- OPOMBA: ko ni obresti, lahko izpustimo celoten sklop elementa <ObrestiZaTerjatev> -->
			<xsl:choose>
				<xsl:when test="//izv:ProcesnoDejanje[@sifProcesnoDejanje=568]/izv:Predlog/izv:OznakaZahtevka/izv:StroskiUpnika/izv:StrosekDrugo/@zakonskeZamudneObresti='true'">
					<iZbs_ElS:ObrestiZaTerjatev>
						<iZbs_ElS:TipObresti>ZAMUDNE_OBRESTI_PO_OZ</iZbs_ElS:TipObresti>  <!-- NI_OBRESTI, ZAMUDNE_OBRESTI_PO_OZ, ZAMUDNE_OBRESTI_PO_ZDAVP, FIKSNA_LETNA_OBRESTNA_MERA, GLEJ_OPIS -->   
						<iZbs_ElS:DatumObrestovanjaOd xsi:nil="true"/>  <!-- ??? od kod lahko razbere datuma obresti za stroske upnika??? -->
						<iZbs_ElS:DatumObrestovanjaDo xsi:nil="true"/>  <!-- ??? od kod lahko razbere datuma obresti za stroske upnika??? -->
						<iZbs_ElS:TrrPrejemnikaZaObresti><xsl:value-of select="izv:pretvoriRacun(/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Oseba[1]/izv:RacunZaNakazilo/@racun)"/></iZbs_ElS:TrrPrejemnikaZaObresti>
						<iZbs_ElS:SklicZaNakaziloZaObresti>
							<iZbs_Spl:StrukturiranaReferencaSI><xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Oseba[1]/izv:RacunZaNakazilo/@sklic"/></iZbs_Spl:StrukturiranaReferencaSI>
						</iZbs_ElS:SklicZaNakaziloZaObresti>
					</iZbs_ElS:ObrestiZaTerjatev>
				</xsl:when>
			</xsl:choose>

			<!-- *********** vstavi podatke o racunu za poplacilo terjatev *********** -->
			<xsl:call-template name="RACUNI_ZA_TERJATEV" />
			<!-- *********** konec vstavljanja odseka RACUNI_ZA_TERJATEV *********** -->
		</iZbs_ElS:Terjatev>
	</xsl:for-each> <!-- konec zanke za izv:StroskiUpnika/izv:StrosekDrugo -->
	
	</iZbs_ElS:Terjatve>

</xsl:template>  <!-- konec sklopa TERJATVE -->


<!-- **********************************************
      PODATKI O RACUNIH UPNIKA
      Opomba: na podlagi trenutne sheme sodisca ni moc enolicno definirati racunov k ustreznim osebam!!!!????
*************************************************** -->
<xsl:template name="RACUNI_ZA_TERJATEV">
	
	<xsl:for-each select="//izv:ProcesnoDejanje[@sifProcesnoDejanje=568]/izv:Predlog/izv:Upniki/izv:Upniki/izv:Oseba[1]">
		<xsl:choose>
			<xsl:when test="string-length(izv:RacunZaNakazilo/@racun) > 1">
				<iZbs_ElS:TrrPrejemnika><xsl:value-of select="izv:pretvoriRacun(izv:RacunZaNakazilo/@racun)"/></iZbs_ElS:TrrPrejemnika>
				<iZbs_ElS:SklicZaNakazilo>
					<iZbs_Spl:StrukturiranaReferencaSI><xsl:value-of select="izv:RacunZaNakazilo/@sklic"/></iZbs_Spl:StrukturiranaReferencaSI>
				</iZbs_ElS:SklicZaNakazilo>
			</xsl:when>
		</xsl:choose>
	</xsl:for-each>

</xsl:template>  <!-- konec sklopa RACUNI_ZA_TERJATEV -->


<!-- ************************************************************************
     Funkcije ...
     ************************************************************************  -->

<!-- ************************************************************************
     Funkcija, ki pretvori kratice drzav iz oblike sodisca (=ISO 3166-3) v obliko ZBS IzvrsbeXml (=ISO 3166-2) 
-->
<xsl:function name="izv:ediniDolznikAliSodolznik">
	<xsl:param name="steviloDolznikovVhod"/>
	<xsl:choose>
		<xsl:when test="$steviloDolznikovVhod > 1"><xsl:value-of select="'SODOLZNIK'"/></xsl:when>
		<xsl:when test="$steviloDolznikovVhod = 1"><xsl:value-of select="'EDINI_DOLZNIK'"/></xsl:when>
		<xsl:otherwise><xsl:value-of select="concat('NAPAKA - Ni podanih dolznikov! ', $steviloDolznikovVhod)"/></xsl:otherwise>
	</xsl:choose>
</xsl:function>


<!-- ************************************************************************
     Funkcija za pretvorbo datumov sodišča v obliko datum+ura, ki je primerna za XSD validacijo 
-->
<xsl:function name="izv:pretvoriDatumSodisca2DatumInUra">
	<xsl:param name="datumVFormatuSodisca"/>
	
	<xsl:variable name="dolzinaDatumaSodisca" select="string-length($datumVFormatuSodisca)"/>

	<xsl:choose>
		<!-- npr. 2016-01-17 pretvori v 2016-01-17T00:00:00Z -->
		<xsl:when test="$dolzinaDatumaSodisca = 10"><xsl:value-of select="$datumVFormatuSodisca"/>T00:00:00Z</xsl:when>
	
		<!-- npr. 2016-01-17+10:15 pretvori v 2016-01-17T10:15:00Z -->
		<xsl:when test="$dolzinaDatumaSodisca = 16"><xsl:value-of select="substring-before($datumVFormatuSodisca, '+')"/>T<xsl:value-of select="substring-after($datumVFormatuSodisca, '+')"/>:00Z</xsl:when>
	
		<!-- npr. 2016-01-17+10:15:00 pretvori v 2016-01-17T10:15:00Z -->
		<xsl:when test="$dolzinaDatumaSodisca = 19"><xsl:value-of select="substring-before($datumVFormatuSodisca, '+')"/>T<xsl:value-of select="substring-after($datumVFormatuSodisca, '+')"/>Z</xsl:when>

		<xsl:otherwise><xsl:value-of select="$datumVFormatuSodisca"></xsl:value-of></xsl:otherwise>
	</xsl:choose>
</xsl:function>

<!-- ************************************************************************
     Funkcija pretvori datumov sodišča v datumsko obliko brez ure
-->
<xsl:function name="izv:pretvoriDatumSodisca2Datum">
	<xsl:param name="datumVFormatuSodisca"/>
	
	<xsl:variable name="dolzinaDatumaSodisca" select="string-length($datumVFormatuSodisca)"/>

	<xsl:choose>
		<!-- npr. 2016-01-17 pretvori v 2016-01-17T00:00:00Z -->
		<xsl:when test="$dolzinaDatumaSodisca > 10"><xsl:value-of select="substring-before($datumVFormatuSodisca, '+')"/></xsl:when>
	
		<xsl:otherwise><xsl:value-of select="$datumVFormatuSodisca"></xsl:value-of></xsl:otherwise>
	</xsl:choose>
</xsl:function>


<!-- ************************************************************************
     Funkcija, ki pretvori razlicne oblike zapisa racuna v SI56 obliko brez presledkov ...
-->
<xsl:function name="izv:pretvoriRacun">
	<xsl:param name="racunNeurejen"/>
	
	<xsl:variable name="racunPreoblikovan" select="upper-case(translate(translate($racunNeurejen,'SI_IBAN',''),' ',''))"/>
	
	<xsl:choose>

		<xsl:when test="not(starts-with($racunPreoblikovan, 'SI56'))">SI56<xsl:value-of select="$racunPreoblikovan"/></xsl:when>

		<xsl:otherwise><xsl:value-of select="$racunPreoblikovan"/></xsl:otherwise>

	</xsl:choose>
</xsl:function>


<!-- ************************************************************************
     Funkcija, ki pretvori kratice drzav iz oblike sodisca (=ISO 3166-3) v obliko ZBS IzvrsbeXml (=ISO 3166-2) 
-->
<xsl:function name="izv:pretvoriDrzavo">
	<xsl:param name="drzavaVhod"/>
	<xsl:choose>
		<xsl:when test="$drzavaVhod='ALB'"><xsl:value-of select="'AL'"/></xsl:when>
		<xsl:when test="$drzavaVhod='AND'"><xsl:value-of select="'AD'"/></xsl:when>
		<xsl:when test="$drzavaVhod='AUT'"><xsl:value-of select="'AT'"/></xsl:when>
		<xsl:when test="$drzavaVhod='BEL'"><xsl:value-of select="'BE'"/></xsl:when>
		<xsl:when test="$drzavaVhod='BGR'"><xsl:value-of select="'BG'"/></xsl:when>
		<xsl:when test="$drzavaVhod='BIH'"><xsl:value-of select="'BA'"/></xsl:when>
		<xsl:when test="$drzavaVhod='BLR'"><xsl:value-of select="'BY'"/></xsl:when>
		<xsl:when test="$drzavaVhod='CHE'"><xsl:value-of select="'CH'"/></xsl:when>
		<xsl:when test="$drzavaVhod='CYP'"><xsl:value-of select="'CY'"/></xsl:when>
		<xsl:when test="$drzavaVhod='CZE'"><xsl:value-of select="'CZ'"/></xsl:when>
		<xsl:when test="$drzavaVhod='DEU'"><xsl:value-of select="'DE'"/></xsl:when>
		<xsl:when test="$drzavaVhod='DNK'"><xsl:value-of select="'DK'"/></xsl:when>
		<xsl:when test="$drzavaVhod='ESP'"><xsl:value-of select="'ES'"/></xsl:when>
		<xsl:when test="$drzavaVhod='EST'"><xsl:value-of select="'EE'"/></xsl:when>
		<xsl:when test="$drzavaVhod='FIN'"><xsl:value-of select="'FI'"/></xsl:when>
		<xsl:when test="$drzavaVhod='FRA'"><xsl:value-of select="'FR'"/></xsl:when>
		<xsl:when test="$drzavaVhod='FRO'"><xsl:value-of select="'FO'"/></xsl:when>
		<xsl:when test="$drzavaVhod='GBR'"><xsl:value-of select="'GB'"/></xsl:when>
		<xsl:when test="$drzavaVhod='GIB'"><xsl:value-of select="'GI'"/></xsl:when>
		<xsl:when test="$drzavaVhod='GRC'"><xsl:value-of select="'GR'"/></xsl:when>
		<xsl:when test="$drzavaVhod='HRV'"><xsl:value-of select="'HR'"/></xsl:when>
		<xsl:when test="$drzavaVhod='HUN'"><xsl:value-of select="'HU'"/></xsl:when>
		<xsl:when test="$drzavaVhod='IMN'"><xsl:value-of select="'IM'"/></xsl:when>
		<xsl:when test="$drzavaVhod='IRL'"><xsl:value-of select="'IE'"/></xsl:when>
		<xsl:when test="$drzavaVhod='ISL'"><xsl:value-of select="'IS'"/></xsl:when>
		<xsl:when test="$drzavaVhod='ITA'"><xsl:value-of select="'IT'"/></xsl:when>
		<xsl:when test="$drzavaVhod='LIE'"><xsl:value-of select="'LI'"/></xsl:when>
		<xsl:when test="$drzavaVhod='LTU'"><xsl:value-of select="'LT'"/></xsl:when>
		<xsl:when test="$drzavaVhod='LUX'"><xsl:value-of select="'LU'"/></xsl:when>
		<xsl:when test="$drzavaVhod='LVA'"><xsl:value-of select="'LV'"/></xsl:when>
		<xsl:when test="$drzavaVhod='MCO'"><xsl:value-of select="'MC'"/></xsl:when>
		<xsl:when test="$drzavaVhod='MDA'"><xsl:value-of select="'MD'"/></xsl:when>
		<xsl:when test="$drzavaVhod='MKD'"><xsl:value-of select="'MK'"/></xsl:when>
		<xsl:when test="$drzavaVhod='MLT'"><xsl:value-of select="'MT'"/></xsl:when>
		<xsl:when test="$drzavaVhod='MNE'"><xsl:value-of select="'ME'"/></xsl:when>
		<xsl:when test="$drzavaVhod='NLD'"><xsl:value-of select="'NL'"/></xsl:when>
		<xsl:when test="$drzavaVhod='NOR'"><xsl:value-of select="'NO'"/></xsl:when>
		<xsl:when test="$drzavaVhod='POL'"><xsl:value-of select="'PL'"/></xsl:when>
		<xsl:when test="$drzavaVhod='PRT'"><xsl:value-of select="'PT'"/></xsl:when>
		<xsl:when test="$drzavaVhod='ROU'"><xsl:value-of select="'RO'"/></xsl:when>
		<xsl:when test="$drzavaVhod='RSB'"><xsl:value-of select="'RS'"/></xsl:when>
		<xsl:when test="$drzavaVhod='RUS'"><xsl:value-of select="'RU'"/></xsl:when>
		<xsl:when test="$drzavaVhod='SMR'"><xsl:value-of select="'SM'"/></xsl:when>
		<xsl:when test="$drzavaVhod='SRB'"><xsl:value-of select="'RS'"/></xsl:when>
		<xsl:when test="$drzavaVhod='SVK'"><xsl:value-of select="'SK'"/></xsl:when>
		<xsl:when test="$drzavaVhod='SVN'"><xsl:value-of select="'SI'"/></xsl:when>
		<xsl:when test="$drzavaVhod='SWE'"><xsl:value-of select="'SE'"/></xsl:when>
		<xsl:when test="$drzavaVhod='UKR'"><xsl:value-of select="'UA'"/></xsl:when>
		<xsl:when test="$drzavaVhod='VAT'"><xsl:value-of select="'VA'"/></xsl:when>
		<xsl:when test="$drzavaVhod='XKX'"><xsl:value-of select="'RS'"/></xsl:when>
		<xsl:when test="$drzavaVhod=' '"><xsl:value-of select="'SI'"/></xsl:when>
		<xsl:when test="$drzavaVhod=''"><xsl:value-of select="'SI'"/></xsl:when>
		<xsl:when test="not($drzavaVhod)"><xsl:value-of select="'SI'"/></xsl:when>
		<xsl:otherwise><xsl:value-of select="concat('Nepodprta drzava ', $drzavaVhod)"/></xsl:otherwise>
	</xsl:choose>
</xsl:function>

<xsl:template name="ID_DOKUMENTA">
	<xsl:value-of select="'SODISCE_IZDAJATELJ_'"/>
	<xsl:for-each select="//izv:ProcesnoDejanje[@sifProcesnoDejanje=566]/izv:PotrdiloOPravnomocnosti">   <!-- nabere podatke iz sklepa o izvrsbi -->
		<xsl:value-of select="izv:OpravilnaStevilka/@vpisnik"/><xsl:value-of select="'-'"/><xsl:value-of select="izv:OpravilnaStevilka/@zadevaStevilka"/>/<xsl:value-of select="izv:OpravilnaStevilka/@zadevaLeto"/>
	</xsl:for-each>
</xsl:template>

<xsl:template name="DAVCNA_STEVILKA_SODISCA">
	<xsl:choose>
		<xsl:when test="/izv:OpisPosiljke/izv:Glava/izv:Posiljatelj/@davcna">
			<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Posiljatelj/@davcna"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="66807107"/>  <!-- OKRAJNO SODIŠČE V LJUBLJANI, CENTRALNI ODDELEK ZA VERODOSTOJNO LISTINO; davcna=66807107 -->
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>


</xsl:stylesheet>


<!--
VPRAŠANJA in odprte dileme:
- sodišče naj določi ID dokumenta/ID sporočila/ID paketa
- sodišče naj določi podatke o sodišču kot pošiljatelju (enota, e-posta, telefon ...); večina teh podatkov je sicer opcijskih

- če obstaja vec dolžnikov, se vsi/isti racuni ponovijo pri vseh dolžnikih; obstaja kakšna rešitev za odpravo tega?  Ali lahko račune povežemo z bankami?
- od kod lahko razberemo datuma obresti (od/do) za stroske upnika?
- kaj polniti v polje KlasifikatorjiSklepa/OznakaListine?
-->
