/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients;

import java.net.URI;

class NullAuthenticator implements Authenticator{
    @Override
    public HttpRequestDetails getRequestDetails(URI uri) {
        return new HttpRequestDetails(uri);
    }

    @Override
    public HttpRequestDetails refreshRequestDetails(URI uri) {
        return new HttpRequestDetails(uri);
    }
}
