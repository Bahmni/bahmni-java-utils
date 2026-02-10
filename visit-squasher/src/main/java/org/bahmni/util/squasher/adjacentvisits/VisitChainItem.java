/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.util.squasher.adjacentvisits;

import java.sql.Timestamp;

public class VisitChainItem {
    private int visit1Id;
    private int visit2Id;
    private Timestamp visit1DateStopped;
    private Timestamp visit2DateStopped;

    public VisitChainItem(int visit1Id, int visit2Id, Timestamp visit1DateStopped, Timestamp visit2DateStopped) {
        this.visit1Id = visit1Id;
        this.visit2Id = visit2Id;
        this.visit1DateStopped = visit1DateStopped;
        this.visit2DateStopped = visit2DateStopped;
    }

    public int getVisit1Id() {
        return visit1Id;
    }

    public int getVisit2Id() {
        return visit2Id;
    }

    public Timestamp getVisit2DateStopped() {
        return visit2DateStopped;
    }

    @Override
    public String toString() {
        return "VisitChainItem{" +
                "visit1Id=" + visit1Id +
                ", visit2Id=" + visit2Id +
                ", visit1DateStopped=" + visit1DateStopped +
                ", visit2DateStopped=" + visit2DateStopped +
                '}';
    }
}
