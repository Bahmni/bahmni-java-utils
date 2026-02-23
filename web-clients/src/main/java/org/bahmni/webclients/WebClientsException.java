/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.webclients;

public class WebClientsException extends RuntimeException {
    public WebClientsException(Throwable cause) {
        super(cause);
    }

    public WebClientsException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebClientsException(String message) {
        super(message);
    }
}