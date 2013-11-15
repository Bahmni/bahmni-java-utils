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