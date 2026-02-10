/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.webclients;

import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Ignored for the same reason as OpenMRSIT {@link org.bahmni.webclients.openmrs.OpenMRSIT}
 */
@Ignore
class HttpClientIT {

    private static final String BAHMNI_SERVER_BASE_URL = "https://qa-02.hip.bahmni-covid19.in/";
    private static final String PATIENT_UUID = "1df3534b-2841-4be8-90e1-ef17c8126c56";

    @Test
    public void shouldBeAbleToReachOpenMRS() throws URISyntaxException {

        ConnectionDetails connectionDetails = new ConnectionDetails(BAHMNI_SERVER_BASE_URL + "openmrs/ws/rest/v1/session", "admin", "test", 10000, 20000);
        HttpClient httpClient = new HttpClient(connectionDetails, new OpenMRSLoginAuthenticator(connectionDetails));
        System.out.println(httpClient.get(new URI(BAHMNI_SERVER_BASE_URL + "openmrs/ws/rest/v1/person/" + PATIENT_UUID + "?v=full")));
    }

}
