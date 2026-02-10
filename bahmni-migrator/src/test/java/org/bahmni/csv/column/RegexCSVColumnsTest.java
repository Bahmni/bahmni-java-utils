/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.csv.column;

import org.bahmni.csv.KeyValue;
import org.bahmni.csv.RegexCSVEntity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RegexCSVColumnsTest {

    @Test
    public void ensure_columns_populated_with_nonrepeating_headers() throws NoSuchFieldException, IllegalAccessException {
        RegexCSVEntity csvEntity = new RegexCSVEntity();

        RegexCSVColumns columns = new RegexCSVColumns(new String[]{"id","name","Obs.1","Obs.2","Obs.3"});
        columns.setValue(csvEntity,csvEntity.getClass().getDeclaredField("observations"),new String[]{"123","firstname","Sitting","Supine","Posture"});

        assertEquals(3,csvEntity.observations.size());
        assertTrue(csvEntity.observations.contains(new KeyValue("1","Sitting")));
        assertTrue(csvEntity.observations.contains(new KeyValue("2","Supine")));
        assertTrue(csvEntity.observations.contains(new KeyValue("3","Posture")));
    }


    @Test
    public void ensure_columns_populated_with_repeating_headers() throws NoSuchFieldException, IllegalAccessException {
        RegexCSVEntity csvEntity = new RegexCSVEntity();

        RegexCSVColumns columns = new RegexCSVColumns(new String[]{"id","name","Obs.1","Obs.2","Obs.2"});
        columns.setValue(csvEntity,csvEntity.getClass().getDeclaredField("observations"),new String[]{"123","firstname","Sitting","Supine","Posture"});

        assertEquals(3,csvEntity.observations.size());
        assertTrue(csvEntity.observations.contains(new KeyValue("1","Sitting")));
        assertTrue(csvEntity.observations.contains(new KeyValue("2","Supine")));
        assertTrue(csvEntity.observations.contains(new KeyValue("2","Posture")));
    }

    @Test
    public void ensure_columns_populated_with_overlapping_header_names_and_values() throws Exception {
        RegexCSVEntity csvEntity = new RegexCSVEntity();

        RegexCSVColumns columns = new RegexCSVColumns(new String[]{"id","name","Obs.1","Obs.2","Obs.2.3"});
        columns.setValue(csvEntity,csvEntity.getClass().getDeclaredField("observations"),new String[]{"123","firstname","Sitting","Supine","Posture"});

        assertEquals(3,csvEntity.observations.size());
        assertTrue(csvEntity.observations.contains(new KeyValue("1","Sitting")));
        assertTrue(csvEntity.observations.contains(new KeyValue("2","Supine")));
        assertTrue(csvEntity.observations.contains(new KeyValue("2.3","Posture")));


    }
}
