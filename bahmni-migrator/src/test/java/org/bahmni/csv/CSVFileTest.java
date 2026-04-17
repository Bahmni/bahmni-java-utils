/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
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
