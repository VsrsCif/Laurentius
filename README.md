![Build Status](https://api.travis-ci.org/VsrsCif/Laurentius.svg?branch=master)
[Travis build](https://travis-ci.org/VsrsCif/Laurentius)


Aplikacija Laurentius je demo aplikacije za varno elektronsko izmenjavo sporočil po standardu ebMS 3.0 (AS4 profil). 
Aplikacija služi kot primer implementacije protokola SVEV 2.0  za elektronsko vročanje sodnih pošiljk, 
kot to določa Pravilnik o elektronskem poslovanju v civilnih sodnih
postopkih (Ur.l. RS, št. 64/10 in 23/11, v nadaljevanju PEPCSP). Namen aplikacije je 
zmanjšati tehnične in stroškovne ovire izvajalcem logističnih storitev (vročanje izhodne 
pošte, kuvertiranje, skeniranje dohodne pošte) in uporabnikom storitev sodišča 
za prehod na elektronsko poslovanje.

Cilj aplikacije je izdelati delujoči primer izvorne kode aplikacije za izmenjavo dokumentov po 
ebMS 3.0 standardu. Aplikacija omogoča zagotavljanje varnosti (podpisovanje in šifriranje),
zanesljivost prenosa, preverjanje vsebin na standardni način. Ravno tako omogoča nastavljivo 
koreografijo izmenjavo sporočil (primer SVEV 2.0).

Sestava embs-sed projekta:
- Laurentius: osnovni projekt določa vse plugin-e in verzije odvisnih knjižnic;
	- Laurentius-libs: skupne knjižnice, ki jih uporabljajo posamezni moduli;
		- Laurentius-msh-xsd: modul generira java objekte iz ebMS 3.0 shem. SOAP_1.1.xsd (http://schemas.xmlsoap.org/soap/envelope/)
			,SOAP_1.2.xsd (http://www.w3.org/2003/05/soap-envelope/), xml.xsd (http://www.w3.org/2001/03/xml.xsd)
			, ebms-header-3_0-200704.xsd (http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/core/ebms-header-3_0-200704.xsd)
			, ebbp-signals-2.0.xsd  (http://docs.oasis-open.org/ebxml-bp/2.0.4/ebbp-signals-2.0.4.xsd)
			, xenc-schema.xsd  (http://www.w3.org/TR/xmlenc-core/xenc-schema.xsd)
			, xmldsig-core-schema.xsd (http://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd)
			, xlink.xsd (http://www.w3.org/TR/xml-i18n-bp/xmlspec/xlink.xsd);
		- Laurentius-wsdl: modul generira java klienta in WS-API iz spletnega opisa (wsdl) SED-WS modula;
		- Laurentius-commons: skupna orodja in šifiranti;
        - Laurentius-dao: skupne storitve za shranjevanje in branje entitet v relacijsko bazo in datotečni sistem. 
	- Laurentius-app: implementacija Laurentius spletnih in FS storitev za prevzemanje in pošiljanje pošte ter
			   implementacija  spletnega vmesnika;
	- Laurentius-msh: implementacija ebMS 3.0 (AS 4). modula



Glavne Uporabljene tehnologije
- maven 3+, jdk 1.8+:  za izgradnjo in zagon aplikacije
- wildfly aplikacijski strežnik. (Aplikacija uporablja jee vmesnike: JPA 2.0, EJB 3.0, JMS, tako da se lahko z manjšimi napori namesti tudi na drugi "aplikacijski strežnik")
- apacheFOP vizalizacijo  izmenjave sporočil,
- apache-cxf za implementacijo spletnih storitev ws-security, in WS-ReliableMessaging.
- spletni vmesnik je zgrajen s JSF in primefaces knjižnicami: 
Podrobnejši spisek vseh odvisnih tehnologij se nahaja v Laurentius/pom.xml (dependecies)

Podrobnejša dokumentacija o aplikaciji (opis, arhitektura, namestitev in uporabniška navodila) se nahajajo na spletni strani:
http://vsrscif.github.io/Laurentius/


