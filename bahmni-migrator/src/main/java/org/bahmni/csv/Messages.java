/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Messages extends ArrayList<String>{
    public Messages() {
        super();
    }

    public Messages(String str) {
        super(Arrays.asList(str));
    }

    public Messages(int initialCapacity) {
        super(initialCapacity);
    }

    public Messages(Collection<? extends String> c) {
        super(c);
    }

    public Messages(Throwable e) {
        this(e.getMessage());
    }

    public void add(Throwable e) {
        this.add(e.getMessage());
    }

    public String asString() {
        StringBuffer messageString = new StringBuffer();
        for (String message : this) {
            messageString.append(message);
        }
        return messageString.toString();
    }
}
