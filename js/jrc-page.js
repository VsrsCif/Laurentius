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
var jrcnavigation = {

    lastPage: "",
    init: function () {
        jrcnavigation.registerUrlChangeListener();

        var currentHash = window.location.hash.replace(/^#\/?/, "");
        if (currentHash === "") {
            jrcnavigation.loadPage("home");
        } else {
            jrcnavigation.loadPage(currentHash);
        }
        return false;


    },

    registerUrlChangeListener: function () {
        window.addEventListener("hashchange", function () {

// When the step is entered hash in the location is updated
// (just few lines above from here), so the hash change is 
// triggered and we would call `load` again on the same element.
//
// To avoid this we store last entered hash and compare.
            var currentHash = window.location.hash.replace(/^#\/?/, "");
            var page = currentHash;
            if (currentHash.indexOf(":") > 0) {
                nvg = currentHash.split(":");
                page = nvg[0];
            }

            if (jrcnavigation.lastPage === "" || page !== jrcnavigation.lastPage) {
                jrcnavigation.loadPage(page);
                jrcnavigation.lastPage = page;
                if (currentHash.indexOf(":") > 0) {
                    window.window.location.hash = '#/' + currentHash;
                }
            }
        });

    },

    loadPage: function (currentHash) {

        var page = currentHash;
        if (currentHash.indexOf(":") > 0) {
            nvg = currentHash.split(":");
            page = nvg[0];

        }
        var toc = "toc.html"
        if (currentHash.indexOf("/") > 0) {
            nvg = currentHash.split("/");
            toc = nvg[nvg.length - 2] + "/" + toc;
        }

        $(document).ready(function () {
            $('#entryPage').load(page + '.html', function () {
                $("div[class='jrcxslt']").each(function (i, block) {
                    jrcxslt.transform(block);
                });
                // do other stuff load is completed
                $('pre code').each(function (i, block) {
                    hljs.highlightBlock(block);
                });

                $("img[class='img-locale']").each(function (i, img) {
                    
                    jrcnavigation.setLocaleImage(img);
                });
            });

            $('#entryTOC').load(toc);
        });

        if (currentHash.indexOf(":") > 0) {
            window.window.location.hash = '#/' + currentHash;
        }

        return true;
    },

    setLocaleImage: function (img) {        
        var path = img.src;
        var newPath = path.substring(0, path.lastIndexOf("-"))  + '-' +
                jrcnavigation.getSupportedLocale() + '.png';
        img.src = newPath;
    },

    getSupportedLocale: function () {


        var lng = navigator.languages && navigator.languages[0] || // Chrome / Firefox
                navigator.language || // All browsers
                navigator.userLanguage; // IE <= 10
        switch (lng) {
            case 'sl':
            case 'sl_SI':
                return "sl";
            default:
                return "en";
        }
    }




}




