/*
 * Copyright 2025, Supreme Court Republic of Slovenia
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
package si.laurentius.commons.utils;

import si.laurentius.commons.SEDSystemProperties;

import java.util.Arrays;

/**
 * Utility class for handling PartyIdentifier-related operations.
 *
 */
public class PartyIdentifierUtils {

    private static final String DOMAIN_SEPARATOR = ",";

    private static final SEDLogger LOG = new SEDLogger(PartyIdentifierUtils.class);

    /**
     * The purpose of this class is to provide utility methods for PartyIdentifier handling. The private constructor
     * ensures that this class cannot be instantiated, as it is intended to be a static utility class.
     */
    protected PartyIdentifierUtils() {
        // Prevent instantiation
    }

    /**
     * Method validates if any item from the party domains match the party domain from the message
     * @param pModePartyDomains party domain list
     * @return true if domain matches the pModeParty
     */
    public static boolean isDomainMatchingPartyDomains(String[] pModePartyDomains, String messagePartyDomain) {
        String messagePartyDomainTrimmed = messagePartyDomain != null ? messagePartyDomain.trim() : "";
        return Arrays.stream(pModePartyDomains)
                .map(String::trim)
                .anyMatch(domain -> {
                    boolean match = messagePartyDomainTrimmed.equalsIgnoreCase(domain);
                    if (match) {
                        LOG.log(messagePartyDomain, "matches",  domain);
                    }
                    return match;
                });
        }


    /**
     * Method returns local domains defined in system properties.
     * @return array of local domains
     */
    public static  String[] getLocalDomains() {
        String domainList = System.getProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN);
        return convertToArray(domainList);
    }

    /**
     * Method converts a comma-separated list of domains into an array of local domains in lowercase.
     * @param domains comma separated list of domains as a String
     * @return array of local domains
     */
    public static String[] convertToArray(String domains) {
        if (domains == null || domains.isEmpty()) {
            return new String[0];
        }
        String [] domainArray =  domains.toLowerCase().split(DOMAIN_SEPARATOR);
        // trim and set to lower case each domain
        for (int i = 0; i < domainArray.length; i++) {
            domainArray[i] = domainArray[i].trim().toLowerCase();
        }
        return domainArray;
    }


    /**
     * Method returns fist domain in comma separated list of domains. If only one domain is provided
     * it is returned as is. If the input is null or empty, null is returned.
     * @param domains comma separated list of domains as a String
     * @return first domain from the list or null if input is null or empty
     */
    public static String getFirstDomain(String domains) {
        String [] domainArray = convertToArray(domains);
        if (domainArray.length == 0) {
            return null;
        }
        return domainArray[0];
    }


}
