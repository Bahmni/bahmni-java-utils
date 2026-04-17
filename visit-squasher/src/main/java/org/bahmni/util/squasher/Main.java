/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.util.squasher;

import org.bahmni.util.squasher.adjacentvisits.AdjacentVisitSquasher;
import org.bahmni.util.squasher.radiology.RadiologyVisitSquasher;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        new RadiologyVisitSquasher().squash();
        new AdjacentVisitSquasher().squash();
    }
}
