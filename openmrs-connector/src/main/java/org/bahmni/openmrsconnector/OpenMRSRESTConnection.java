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

import java.util.Base64;

public class OpenMRSRESTConnection {
    private String server;
    private String userId;
    private String password;

    private static Base64.Encoder base64Encoder = Base64.getEncoder();

    public OpenMRSRESTConnection(String server, String userId, String password) {
        this.server = server;
        this.userId = userId;
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getRestApiUrl() {
        return String.format("http://%s:8080/openmrs/ws/rest/v1/", server);
    }

    public String encodedLogin() {
        return base64Encoder.encodeToString((userId + ":" + password).getBytes());
    }
}