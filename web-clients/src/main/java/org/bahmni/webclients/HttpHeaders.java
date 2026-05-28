/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients;

import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HttpHeaders extends HashMap<String, String> {

    public void addTo(HttpMessage httpMessage) {
        for (Map.Entry<String, String> entry : this.entrySet()) {
            httpMessage.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
        }
    }

}
