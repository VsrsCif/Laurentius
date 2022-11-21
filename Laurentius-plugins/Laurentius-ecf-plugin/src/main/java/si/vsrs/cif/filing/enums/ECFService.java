/*
 * Copyright 2017, Supreme Court Republic of Slovenia
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
package si.vsrs.cif.filing.enums;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * eCourt eFiling Service names as described in document:
 *  - Funkcionalne in tehnične zahteve za e-Vlaganje v varni elektronski predal sodišča 1.0. Date: 5.05.2022
 *
 * @author Jože Rihtaršič
 * @since 2.0
 */
public enum ECFService {

    CourtFiling("CourtFiling", null, Collections.singletonList(ECFAction.ServeFiling));
    // service definition
    String service;
    String namespace;
    // List of possible actions in the service
    List<ECFAction> actionList;

    ECFService(String service, String namespace, List<ECFAction> actionList) {
        this.service = service;
        this.namespace = namespace;
        this.actionList=actionList;
    }

    public String getService() {
        return service;
    }

    public String getNamespace() {
        return namespace;
    }

    public List<ECFAction> getActionList() {
        return actionList;
    }

    public static ECFService getValueByService(String service) {
        for (ECFService ms : values()) {
            if (Objects.equals(ms.getService(), service)) {
                return ms;
            }
        }
        return null;
    }


}
