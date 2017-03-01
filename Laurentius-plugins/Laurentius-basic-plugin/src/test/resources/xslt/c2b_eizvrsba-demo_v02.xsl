<?xml version="1.0" encoding="utf-8"?>
<!-- edited with XMLSpy v2008 (http://www.altova.com) by XMLSpy 2007 Professional Ed., Installed for 5 users (with SMP from 2007-02-06 to 2008-02-07) (CIF VSRS) -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" exclude-result-prefixes="izv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:izv="http://sodisce.si/izvrsba" xmlns:iZbs_AS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_AkcijeSklepov.xsd" xmlns:iZbs_Body="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_Body.xsd" xmlns:iZbs_DocAS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_DokumentAkcijSklepov.xsd" xmlns:iZbs_ElDoc="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ElementiDokumenta.xsd" xmlns:iZbs_ElS="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ElementiSklepa.xsd" xmlns:iZbs_Spl="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_SplosniTipi.xsd" xmlns:iZbs_Zaglavje="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_tipi_ZaglavjeSporocil.xsd" xmlns:iZbs_DokVseb="http://www.zbs-giz.si/Schemas/IzvrsbeXml/1.0/IzvrsbeXml_DokumentiVsebinski.xsd">
	<xsl:output method="xml" indent="yes"/>
	<xsl:variable name="ISOCountryCode" select="document('/sluzba/code/SVEV2.0/Laurentius/Laurentius-msh/Laurentius-msh-ws/src/test/resources/xslt/ISO3166.xml')"/>
	<xsl:key name="ISO-lookup" match="ISO3166" use="@a3"/>
	<xsl:param name="messageid">someuniqueid@receiver.example.com</xsl:param>
	<xsl:param name="timestamp">2000-01-01T00:00:00Z</xsl:param>
	<xsl:template match="/">
		<iZbs_DokVseb:PrevzemiDokumentXmlResponse>
			<xsl:choose>
				<xsl:when test="/izv:OpisPosiljke">
					<xsl:apply-templates select="//izv:OpisPosiljke"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="invalid-input-xml"/>
				</xsl:otherwise>
			</xsl:choose>
		</iZbs_DokVseb:PrevzemiDokumentXmlResponse>
	</xsl:template>
	<!-- 
Sklep o izvrsbi

-->
	<xsl:template match="izv:OpisPosiljke">
		<iZbs_DokVseb:Header>
			<iZbs_Zaglavje:DatumSporocila>
				<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti/izv:datumPravnomocnosti"/>
			</iZbs_Zaglavje:DatumSporocila>
			<iZbs_Zaglavje:IdSporocila>
				<iZbs_Spl:Id>
					<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti/izv:OpravilnaStevilka/@vpisnik"/>-<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti/izv:OpravilnaStevilka/@zadevaStevilka"/>/<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti/izv:OpravilnaStevilka/@zadevaLeto"/>
				</iZbs_Spl:Id>
				<iZbs_Spl:DavcnaStevilka>
					<xsl:variable name="C3A" select="/izv:OpisPosiljke/izv:Glava/izv:Posiljatelj/izv:Naslov/@drzava"/>
					<iZbs_Spl:Drzava>
						<xsl:value-of select="key('ISO-lookup',$C3A, $ISOCountryCode)/@a2"/>
					</iZbs_Spl:Drzava>
					<iZbs_Spl:Vrednost>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Posiljatelj/@davcna"/>
					</iZbs_Spl:Vrednost>
				</iZbs_Spl:DavcnaStevilka>
			</iZbs_Zaglavje:IdSporocila>
			<iZbs_Zaglavje:PrejemnikSporocila>
				<xsl:variable name="C3APrejemnik" select="/izv:OpisPosiljke/izv:Glava/izv:Prejemnik/izv:Naslov/@drzava"/>
				<iZbs_Spl:DavcnaStevilka>
					<iZbs_Spl:Drzava>
						<xsl:value-of select="key('ISO-lookup',$C3APrejemnik, $ISOCountryCode)/@a2"/>
					</iZbs_Spl:Drzava>
					<iZbs_Spl:Vrednost>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Prejemnik/@davcna"/>
					</iZbs_Spl:Vrednost>
				</iZbs_Spl:DavcnaStevilka>
				<iZbs_Spl:Naziv>
					<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Prejemnik/@priimek-naziv"/>
				</iZbs_Spl:Naziv>
				<iZbs_Spl:Naslov>
					<iZbs_Spl:Ulica>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Prejemnik/izv:Naslov/@ulica"/>
					</iZbs_Spl:Ulica>
					<iZbs_Spl:HisnaStevilka>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Prejemnik/izv:Naslov/@hisnaStevilka"/>
					</iZbs_Spl:HisnaStevilka>
					<iZbs_Spl:Kraj>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Prejemnik/izv:Naslov/@kraj"/>
					</iZbs_Spl:Kraj>
					<iZbs_Spl:Posta>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Prejemnik/izv:Naslov/@sifPosta"/>
					</iZbs_Spl:Posta>
					<iZbs_Spl:Drzava>
						<xsl:value-of select="key('ISO-lookup',$C3APrejemnik, $ISOCountryCode)/@a2"/>
					</iZbs_Spl:Drzava>
				</iZbs_Spl:Naslov>
			</iZbs_Zaglavje:PrejemnikSporocila>
			<iZbs_Zaglavje:PosiljateljSporocila>
				<xsl:variable name="C3A" select="/izv:OpisPosiljke/izv:Glava/izv:Posiljatelj/izv:Naslov/@drzava"/>
				<iZbs_Spl:DavcnaStevilka>
					<iZbs_Spl:Drzava>
						<xsl:value-of select="key('ISO-lookup',$C3A, $ISOCountryCode)/@a2"/>
					</iZbs_Spl:Drzava>
					<iZbs_Spl:Vrednost>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Posiljatelj/@davcna"/>
					</iZbs_Spl:Vrednost>
				</iZbs_Spl:DavcnaStevilka>
				<iZbs_Spl:Naziv>
					<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Posiljatelj/@ime-naziv"/>
				</iZbs_Spl:Naziv>
				<iZbs_Spl:Naslov>
					<iZbs_Spl:Ulica>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Posiljatelj/izv:Naslov/@ulica"/>
					</iZbs_Spl:Ulica>
					<iZbs_Spl:HisnaStevilka>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Posiljatelj/izv:Naslov/@hisnaStevilka"/>
					</iZbs_Spl:HisnaStevilka>
					<iZbs_Spl:Kraj>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Posiljatelj/izv:Naslov/@kraj"/>
					</iZbs_Spl:Kraj>
					<iZbs_Spl:Posta>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Glava/izv:Posiljatelj/izv:Naslov/@sifPosta"/>
					</iZbs_Spl:Posta>
					<iZbs_Spl:Drzava>
						<xsl:value-of select="key('ISO-lookup',$C3A, $ISOCountryCode)/@a2"/>
					</iZbs_Spl:Drzava>
				</iZbs_Spl:Naslov>
			</iZbs_Zaglavje:PosiljateljSporocila>
			<iZbs_Zaglavje:TestAliProdukcija>PRODUKCIJA</iZbs_Zaglavje:TestAliProdukcija>
			<iZbs_Zaglavje:IdIzvornegaSistema>SODISCE-IS01</iZbs_Zaglavje:IdIzvornegaSistema>
			<iZbs_Zaglavje:Verzija>
				<iZbs_Zaglavje:VerzijaStoritve>1.0</iZbs_Zaglavje:VerzijaStoritve>
				<iZbs_Zaglavje:VerzijaXsdShemeSporocila>1.0</iZbs_Zaglavje:VerzijaXsdShemeSporocila>
			</iZbs_Zaglavje:Verzija>
		</iZbs_DokVseb:Header>
		<iZbs_DokVseb:ResponseHeader>
			<xsl:choose>
				<!-- MUST HAVE  PotrdiloOPravnomocnost  ADD OTHER TESTS...-->
				<xsl:when test="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti">
					<iZbs_Zaglavje:ResponseStatus>OK</iZbs_Zaglavje:ResponseStatus>
				</xsl:when>
				<xsl:otherwise>
					<iZbs_Zaglavje:ResponseStatus>ERROR</iZbs_Zaglavje:ResponseStatus>
					<iZbs_Zaglavje:Napake>
						<iZbs_Spl:Napaka>
							<iZbs_Spl:NapakaTip>KRITICNA_NAPAKA</iZbs_Spl:NapakaTip>
							<iZbs_Spl:NapakaKoda>20000-NAPAKA_XML</iZbs_Spl:NapakaKoda>
							<iZbs_Spl:NapakaOpis>Vhodni dokument ni pravilen.</iZbs_Spl:NapakaOpis>
						</iZbs_Spl:Napaka>
					</iZbs_Zaglavje:Napake>
				</xsl:otherwise>
			</xsl:choose>
		</iZbs_DokVseb:ResponseHeader>
		<iZbs_DokVseb:Body>
			<!-- MUST HAVE  PotrdiloOPravnomocnost  ADD OTHER TESTS...-->
			<xsl:choose>
				<xsl:when test="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti">
					<iZbs_Body:DokumentAkcijSklepov>
						<xsl:apply-templates select="izv:Vsebina"/>
					</iZbs_Body:DokumentAkcijSklepov>
				</xsl:when>
				<xsl:otherwise>
					<iZbs_Body:DokumentPrazenObNapaki/>
				</xsl:otherwise>
			</xsl:choose>
		</iZbs_DokVseb:Body>
	</xsl:template>
	<!--  
 ************************************
BODY XML RESPONSE 
 ************************************
-->
	<xsl:template match="izv:Vsebina">
		<iZbs_DocAS:PaketAkcijSklepov>
			<iZbs_DocAS:AkcijeSklepov>
				<iZbs_DocAS:AkcijaSklepa>
					<iZbs_AS:Sklep>
						<iZbs_ElS:IdSklepa>
							<!-- VRSTICA 20, 21, 22 -->
							<iZbs_ElS:OpravilnaStevilka>
								<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti/izv:OpravilnaStevilka/@vpisnik"/>-<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti/izv:OpravilnaStevilka/@zadevaStevilka"/>/<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti/izv:OpravilnaStevilka/@zadevaLeto"/>
							</iZbs_ElS:OpravilnaStevilka>
						</iZbs_ElS:IdSklepa>
						<iZbs_ElS:DatumiSklepa>
							<!-- VRSTICA 26 -->
							<iZbs_ElS:DatumIzdaje>
								<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:SklepOIzvrsbi/izv:datumSklepaOIzvrsbi"/>
							</iZbs_ElS:DatumIzdaje>
							<!-- VRSTICA 34 -->
							<iZbs_ElS:DatumIzvrsljivosti>
								<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti/izv:datumPravnomocnosti"/>
							</iZbs_ElS:DatumIzvrsljivosti>
						</iZbs_ElS:DatumiSklepa>
						<iZbs_ElS:StatusSklepaPriSubjektu>
							<!-- VRSTICA 34 -b-->
							<xsl:if test="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:SklepOIzvrsbi/izv:datumSklepaOIzvrsbi">
								<iZbs_ElS:StatusSklepa>30000-IZVRSLJIV</iZbs_ElS:StatusSklepa>
							</xsl:if>
						</iZbs_ElS:StatusSklepaPriSubjektu>
						<!-- DOLŽNIKI-->
						<xsl:apply-templates select="izv:ProcesnoDejanje/izv:Predlog/izv:Dolzniki/izv:Dolznik"/>
						<!-- UPNIKI -->
						<xsl:apply-templates select="izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki"/>
						<!-- TERJATVE -->
						<iZbs_ElS:Terjatve>
							<xsl:apply-templates select="izv:ProcesnoDejanje/izv:Predlog/izv:OznakaZahtevka/izv:VerodostojneListine/izv:VerodostojnaListina"/>
						</iZbs_ElS:Terjatve>
						<iZbs_ElS:IzdajateljSklepaPodrobnosti>
							<iZbs_Spl:Enota>
								<!-- VRSTICA 16 -->
								<iZbs_Spl:NazivEnote>
									<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:OkrajnoSodisce/@nazivSodisce"/>
								</iZbs_Spl:NazivEnote>
								<!-- VRSTICA 17 -->
								<iZbs_Spl:InternaOznakaEnoteId>
									<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:OkrajnoSodisce/@sifSodisce"/>
								</iZbs_Spl:InternaOznakaEnoteId>
							</iZbs_Spl:Enota>
						</iZbs_ElS:IzdajateljSklepaPodrobnosti>
					</iZbs_AS:Sklep>
				</iZbs_DocAS:AkcijaSklepa>
			</iZbs_DocAS:AkcijeSklepov>
		</iZbs_DocAS:PaketAkcijSklepov>
	</xsl:template>



	<!-- ************************************
TERJATVE
 ************************************ -->
	<xsl:template match="izv:VerodostojnaListina">
		<iZbs_ElS:Terjatev>
			
				<iZbs_ElS:ZnesekZValuto>
					<!-- VRSTICA 187  -->
					<iZbs_Spl:Znesek>
						<xsl:value-of select="@znesek"/>
					</iZbs_Spl:Znesek>
					<!-- VRSTICA 188  -->
					<iZbs_Spl:Valuta>
						<xsl:value-of select="@valuta"/>
					</iZbs_Spl:Valuta>
				</iZbs_ElS:ZnesekZValuto>
				<iZbs_ElS:ObrestiZaTerjatev>
				<!-- Vrstica 190 TODO  - manjka preslikava sifranta !! -->
										<iZbs_ElS:TipObresti>ZAMUDNE_OBRESTI_PO_ZDAVP</iZbs_ElS:TipObresti>
										<!-- VRSTICA 189 -->
										<iZbs_ElS:DatumObrestovanjaOd>@odDatuma</iZbs_ElS:DatumObrestovanjaOd>
				<!-- VRSTICA 45  -->
				<iZbs_ElS:TrrPrejemnikaZaObresti>
					<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Oseba[1]/izv:RacunZaNakazilo/@racun"/>
				</iZbs_ElS:TrrPrejemnikaZaObresti>
				<iZbs_ElS:SklicZaNakaziloZaObresti>
					<!-- VRSTICA 44  -->
					<iZbs_Spl:StrukturiranaReferencaSI>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Oseba/izv:RacunZaNakazilo/@sklic"/>
					</iZbs_Spl:StrukturiranaReferencaSI>
				</iZbs_ElS:SklicZaNakaziloZaObresti>
			</iZbs_ElS:ObrestiZaTerjatev>
			<!-- VRSTICA 45  b -->
			<iZbs_ElS:TrrPrejemnika>
				<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Oseba[1]/izv:RacunZaNakazilo/@racun"/>
			</iZbs_ElS:TrrPrejemnika>
			<iZbs_ElS:SklicZaNakazilo>
				<!-- VRSTICA 44  b -->
				<iZbs_Spl:StrukturiranaReferencaSI>
					<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Oseba/izv:RacunZaNakazilo/@sklic"/>
				</iZbs_Spl:StrukturiranaReferencaSI>
			</iZbs_ElS:SklicZaNakazilo>
		</iZbs_ElS:Terjatev>
	</xsl:template>
	<!-- ************************************
DOLŽNIKI
 ************************************ -->
	<xsl:template match="izv:Dolznik">
		<iZbs_ElS:Dolzniki>
			<!-- TODO FOREACH /osebe - vec doznikov-->
			<!-- VRSTICA 132 - 142 -->
			<iZbs_ElS:Dolznik>
				<xsl:apply-templates select="izv:Oseba"/>
				<!-- TODO  - vec doznikov -->
				<iZbs_ElS:TipDolznika>EDINI_DOLZNIK</iZbs_ElS:TipDolznika>
			</iZbs_ElS:Dolznik>
		</iZbs_ElS:Dolzniki>
	</xsl:template>
	<!-- ************************************
UPNIKI
 ************************************ -->
 <!-- samo oseba z nakazilom-->
	<xsl:template match="izv:Upniki">
	<xsl:if test="izv:Oseba/izv:RacunZaNakazilo">
		<iZbs_ElS:Upnik>
		
			<!-- VRSTICA 36  - 49 !! shema sodišča ima lahko več upnikov in pozna samo fizicna/pravna-->
			<xsl:apply-templates select="izv:Oseba"/>
			<iZbs_ElS:Pooblascenec>
				<iZbs_ElS:Subjekt xsi:type="iZbs_Spl:FizicnaOsebaType">
					<!-- VRSTICA 112  -->
					<iZbs_Spl:Ime>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti/izv:Upniki/izv:Upniki/izv:Pooblascenec/@ime-naziv"/>
					</iZbs_Spl:Ime>
					<!-- VRSTICA 112  -->
					<iZbs_Spl:Priimek>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti/izv:Upniki/izv:Upniki/izv:Pooblascenec/@priimek-naziv"/>
					</iZbs_Spl:Priimek>
					<!-- VRSTICA 54  -->
					<iZbs_Spl:Emso>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Pooblascenec/@maticna"/>
					</iZbs_Spl:Emso>
					<!-- VRSTICA 55 -->
					<iZbs_Spl:DavcnaStevilka>
						<xsl:value-of select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:Predlog/izv:Upniki/izv:Upniki/izv:Pooblascenec/@davcna"/>
					</iZbs_Spl:DavcnaStevilka>
					<!-- VRSTICA 115 - 119 -->
					<xsl:apply-templates select="/izv:OpisPosiljke/izv:Vsebina/izv:ProcesnoDejanje/izv:PotrdiloOPravnomocnosti/izv:Upniki/izv:Upniki/izv:Pooblascenec/izv:Naslov"/>
				</iZbs_ElS:Subjekt>
			</iZbs_ElS:Pooblascenec>
		</iZbs_ElS:Upnik>
		</xsl:if>
	</xsl:template>
	<!-- ************************************
Upnik
 ************************************ -->
	<xsl:template match="izv:Oseba">
		<iZbs_ElS:Subjekt>
			<xsl:attribute name="xsi:type"><xsl:choose><xsl:when test="@vrsta='fizicna' "><xsl:text>iZbs_Spl:FizicnaOsebaType</xsl:text></xsl:when><xsl:when test="@vrsta='pravna'"><xsl:text>iZbs_Spl:PravnaOsebaType</xsl:text></xsl:when><xsl:otherwise><xsl:text>ERROR</xsl:text></xsl:otherwise></xsl:choose></xsl:attribute>
			<iZbs_Spl:Ime>
				<xsl:value-of select="@ime-naziv"/>
			</iZbs_Spl:Ime>
			<iZbs_Spl:Priimek>
				<xsl:value-of select="@priimek-naziv"/>
			</iZbs_Spl:Priimek>
			<iZbs_Spl:DatumRojstva xsi:nil="true"/>
			<iZbs_Spl:DavcnaStevilka>
				<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
				<iZbs_Spl:Vrednost>
					<xsl:value-of select="@davcna"/>
				</iZbs_Spl:Vrednost>
			</iZbs_Spl:DavcnaStevilka>
			<!-- VRSTICA 39 TODO pravna oseba-->
			<iZbs_Spl:Emso>
				<xsl:value-of select="@maticna"/>
			</iZbs_Spl:Emso>
			<xsl:apply-templates select="izv:Naslov"/>
		</iZbs_ElS:Subjekt>
	</xsl:template>
	<!-- ************************************
Naslov
 ************************************ -->
	<xsl:template match="izv:Naslov">
		<iZbs_Spl:Naslov>
			<iZbs_Spl:Ulica>
				<xsl:value-of select="@ulica"/>
			</iZbs_Spl:Ulica>
			<iZbs_Spl:HisnaStevilka>
				<xsl:value-of select="@hisnaStevilka"/>
			</iZbs_Spl:HisnaStevilka>
			<iZbs_Spl:Kraj>
				<xsl:value-of select="@nazivPosta"/>
			</iZbs_Spl:Kraj>
			<iZbs_Spl:Posta>
				<xsl:value-of select="@sifPosta"/>
			</iZbs_Spl:Posta>
			<xsl:variable name="UpnC3A" select="@drzava"/>
			<iZbs_Spl:Drzava>
				<xsl:value-of select="key('ISO-lookup',$UpnC3A, $ISOCountryCode)/@a2"/>
			</iZbs_Spl:Drzava>
		</iZbs_Spl:Naslov>
	</xsl:template>
	<!--  
 ************************************
INVALID INPUT XML RESPONSE 
 ************************************
-->
	<xsl:template name="invalid-input-xml">
		<iZbs_DokVseb:Header>
			<iZbs_Zaglavje:DatumSporocila>
				<xsl:value-of select="$timestamp"/>
			</iZbs_Zaglavje:DatumSporocila>
			<iZbs_Zaglavje:IdSporocila>
				<iZbs_Spl:Id>ERROR</iZbs_Spl:Id>
				<iZbs_Spl:DavcnaStevilka>
					<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
					<iZbs_Spl:Vrednost>XSLT-ERROR</iZbs_Spl:Vrednost>
				</iZbs_Spl:DavcnaStevilka>
			</iZbs_Zaglavje:IdSporocila>
			<iZbs_Zaglavje:PosiljateljSporocila>
				<iZbs_Spl:DavcnaStevilka>
					<iZbs_Spl:Drzava>SI</iZbs_Spl:Drzava>
					<iZbs_Spl:Vrednost>XSLT-ERROR</iZbs_Spl:Vrednost>
				</iZbs_Spl:DavcnaStevilka>
				<iZbs_Spl:Naziv>ERROR</iZbs_Spl:Naziv>
			</iZbs_Zaglavje:PosiljateljSporocila>
			<iZbs_Zaglavje:TestAliProdukcija>PRODUKCIJA</iZbs_Zaglavje:TestAliProdukcija>
			<iZbs_Zaglavje:IdIzvornegaSistema>XSLT-TEST</iZbs_Zaglavje:IdIzvornegaSistema>
			<iZbs_Zaglavje:Verzija>
				<iZbs_Zaglavje:VerzijaXsdShemeSporocila>1.0</iZbs_Zaglavje:VerzijaXsdShemeSporocila>
			</iZbs_Zaglavje:Verzija>
		</iZbs_DokVseb:Header>
		<iZbs_DokVseb:ResponseHeader>
			<iZbs_Zaglavje:ResponseStatus>ERROR</iZbs_Zaglavje:ResponseStatus>
			<iZbs_Zaglavje:Napake>
				<iZbs_Spl:Napaka>
					<iZbs_Spl:NapakaTip>KRITICNA_NAPAKA</iZbs_Spl:NapakaTip>
					<iZbs_Spl:NapakaKoda>20000-NAPAKA_XML</iZbs_Spl:NapakaKoda>
					<iZbs_Spl:NapakaOpis>Vhodni dokument ni pravilen.</iZbs_Spl:NapakaOpis>
				</iZbs_Spl:Napaka>
			</iZbs_Zaglavje:Napake>
		</iZbs_DokVseb:ResponseHeader>
		<iZbs_DokVseb:Body>
			<iZbs_Body:DokumentPrazenObNapaki/>
		</iZbs_DokVseb:Body>
	</xsl:template>
</xsl:stylesheet>
