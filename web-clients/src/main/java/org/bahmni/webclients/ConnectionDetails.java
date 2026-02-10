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

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class ConnectionDetails {
    private String authUrl;
    private String userId;
    private String password;
    private int connectionTimeout;
    private int readTimeout;
    private PoolingHttpClientConnectionManager connectionManager;

    public ConnectionDetails(String authUrl, String userId, String password, int connectionTimeout, int readTimeout) {
        this.authUrl = authUrl;
        this.userId = userId;
        this.password = password;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    public ConnectionDetails(String authUrl, String userId, String password, int connectionTimeout, int readTimeout, PoolingHttpClientConnectionManager connectionManager) {
        this.authUrl = authUrl;
        this.userId = userId;
        this.password = password;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.connectionManager = connectionManager;
    }


    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public PoolingHttpClientConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
