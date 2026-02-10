/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.openmrsconnector;

import java.util.HashMap;
import java.util.Map;

public class AllPatientAttributeTypes {
    private Map<String, String> personAttributeTypes = new HashMap<String, String>();

    public void addPersonAttributeType(String name, String uuid) {
        personAttributeTypes.put(name, uuid);
    }

    public String getAttributeUUID(String name) {
        return personAttributeTypes.get(name);
    }
}