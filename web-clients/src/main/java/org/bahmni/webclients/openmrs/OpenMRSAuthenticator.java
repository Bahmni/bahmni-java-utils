/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients.openmrs;

import org.bahmni.webclients.Authenticator;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.WebClientsException;
import org.bahmni.webclients.oauth.ClientCredentialsTokenProvider;
import org.bahmni.webclients.oauth.OAuthClientCredentials;
import org.bahmni.webclients.utils.PropertyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class OpenMRSAuthenticator {

    public static final String OAUTH2_ENABLED = "oauth2.enabled";
    public static final String TOKEN_URL = "openmrs.oauth.token.url";
    public static final String CLIENT_ID = "openmrs.oauth.client.id";
    public static final String CLIENT_SECRET = "openmrs.oauth.client.secret";
    public static final String SCOPE = "openmrs.oauth.scope";
    public static final String EXPIRY_SKEW_SECONDS = "openmrs.oauth.expiry.skew.seconds";

    private static final Logger logger = LoggerFactory.getLogger(OpenMRSAuthenticator.class);

    private OpenMRSAuthenticator() {
    }

    public static Authenticator getAuthenticator(ConnectionDetails connectionDetails, Properties properties) {
        if (isOAuthEnabled(properties)) {
            logger.info("OpenMRS auth mode: oauth2");
            OAuthClientCredentials credentials = readOAuthCredentials(connectionDetails, properties);
            return new OpenMRSOAuthLoginAuthenticator(connectionDetails, new ClientCredentialsTokenProvider(credentials));
        }
        logger.info("OpenMRS auth mode: basic");
        return new OpenMRSBasicLoginAuthenticator(connectionDetails);
    }

    private static boolean isOAuthEnabled(Properties properties) {
        if (properties == null) return false;
        return "true".equalsIgnoreCase(PropertyReader.optionalTrimmed(properties, OAUTH2_ENABLED));
    }

    private static OAuthClientCredentials readOAuthCredentials(ConnectionDetails connectionDetails, Properties properties) {
        String tokenUrl = PropertyReader.optionalTrimmed(properties, TOKEN_URL);
        String clientId = PropertyReader.optionalTrimmed(properties, CLIENT_ID);
        String clientSecret = PropertyReader.optionalTrimmed(properties, CLIENT_SECRET);
        List<String> missing = new ArrayList<String>();
        if (tokenUrl == null) missing.add(TOKEN_URL);
        if (clientId == null) missing.add(CLIENT_ID);
        if (clientSecret == null) missing.add(CLIENT_SECRET);
        if (!missing.isEmpty()) {
            throw new WebClientsException(OAUTH2_ENABLED + " is true but required OAuth settings are missing: " + missing);
        }
        return new OAuthClientCredentials(tokenUrl, clientId, clientSecret, PropertyReader.optionalTrimmed(properties, SCOPE),
                connectionDetails.getConnectionTimeout(), connectionDetails.getReadTimeout(), readExpirySkewSeconds(properties));
    }

    private static int readExpirySkewSeconds(Properties properties) {
        String value = PropertyReader.optionalTrimmed(properties, EXPIRY_SKEW_SECONDS);
        if (value == null) return OAuthClientCredentials.DEFAULT_EXPIRY_SKEW_SECONDS;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new WebClientsException(EXPIRY_SKEW_SECONDS + " must be a number of seconds but was '" + value + "'");
        }
    }
}
