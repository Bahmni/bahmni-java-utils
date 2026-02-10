/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.csv;

import org.bahmni.csv.exception.MigrationException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertTrue;

public class CSVFileTest {
    @Test(expected = MigrationException.class)
    public void cannot_read_without_opening_the_file() throws IllegalAccessException, IOException, InstantiationException {
        CSVFile<DummyCSVEntity> dummyCSVEntityCSVFile = new CSVFile<>(".", "invalidFile.csv");
        dummyCSVEntityCSVFile.readEntity(DummyCSVEntity.class);
    }

    @Test(expected = MigrationException.class)
    public void open_throws_MigrationException_if_file_does_not_exist() throws IllegalAccessException, IOException, InstantiationException {
        CSVFile<DummyCSVEntity> dummyCSVEntityCSVFile = new CSVFile<>(".", "invalidFile.csv");
        dummyCSVEntityCSVFile.openForRead();
    }
}
