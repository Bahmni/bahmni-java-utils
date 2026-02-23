/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpMessage;
import org.apache.http.message.BasicHeader;

import java.util.HashMap;
import java.util.Map;

public class ClientCookies extends HashMap<String, String> {

    public void addTo(HttpMessage httpMessage) {
        httpMessage.setHeader(new BasicHeader("Cookie", getHttpRequestPropertyValue()));
    }

    private String getHttpRequestPropertyValue() {
        if (this.size() <= 0) return null;
        String[] cookieEntryAndValues = new String[this.size()];
        int i = 0;
        for (Map.Entry entry : this.entrySet()) {
            cookieEntryAndValues[i] = String.format(" %s=%s ", entry.getKey(), entry.getValue());
            i++;
        }
        return StringUtils.join(cookieEntryAndValues, ";");
    }

}