/*
 * Copyright 2016, Supreme Court Republic of Slovenia
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
/**
 *
 * @author Jože Rihtaršič
 */
var jrcxslt = {
    
    transform: function(obj){
        this.displayResult(obj, obj.getAttribute('data-xslt'), obj.getAttribute('data-xml'));
        
    },

    loadXMLDoc: function(filename)
    {

        if (window.ActiveXObject)
        {
            xhttp = new ActiveXObject("Msxml2.XMLHTTP");
        } else
        {
            xhttp = new XMLHttpRequest();
        }
        xhttp.open("GET", filename, false);
        try {
            xhttp.responseType = "msxml-document"
        } catch (err) {
        } // Helping IE11
        xhttp.send("");
        return xhttp.responseXML;
    },

    displayResult: function (div, xslt, xml)
    {
        xml = loadXMLDoc(xml);
        xsl = loadXMLDoc(xslt);
// code for IE
        if (window.ActiveXObject || xhttp.responseType == "msxml-document")
        {
            ex = xml.transformNode(xsl);
            div.innerHTML = ex;
        }
// code for Chrome, Firefox, Opera, etc.
        else if (document.implementation && document.implementation.createDocument)
        {
            alert(xml);
            xsltProcessor = new XSLTProcessor();
            xsltProcessor.importStylesheet(xsl);
            resultDocument = xsltProcessor.transformToFragment(xml, document);
            div.innerHTML = resultDocument;
        }
    }
};


