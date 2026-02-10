/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.fileimport;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class ImportStatusTest {

    @Test
    public void parse_errormessage_from_stacktrace() {
        String stackTrace= "\"org.bahmni.csv.exception.MigrationException: Input CSV file does not exist. File - /home/jss/uploaded-files/mrs/patient/patient_2014-11-03_11:45:45.csv\n\tat org.bahmni.csv.CSVFile.openForRead(CSVFile.java:44)\\n\\tat org.bahmni.csv.Stage.run(Stage.java:43)\\n\\tat org.bahmni.csv.Migrator.migrate(Migrator.java:34)\\n\\tat org.bahmni.fileimport.FileImportThread.run(FileImportThread.java:58)\\n\\tat java.lang.Thread.run(Thread.java:745)\\n";
        ImportStatus importStatus = new ImportStatus(null, null, null, null, null, null, 0, 0, null, null, null, null, stackTrace);
        assertEquals("Input CSV file does not exist. File - /home/jss/uploaded-files/mrs/patient/patient_2014-11-03_11:45:45.csv", importStatus.getErrorMessage());
    }

    @Test
    public void errormessage_is_null_for_empty_stacktrace() {
        String stackTrace= null;
        ImportStatus importStatus = new ImportStatus(null, null, null, null, null, null, 0, 0, null, null, null, null, stackTrace);
        assertNull("errormessage should be null for empty stacktrace", importStatus.getErrorMessage());
    }

    @Test
    public void errormessage_is_null_for_invalid_stacktrace() {
        String stackTrace= "somestring that is not a stack trace";
        ImportStatus importStatus = new ImportStatus(null, null, null, null, null, null, 0, 0, null, null, null, null, stackTrace);
        assertNull("errormessage should be null for invalid stacktrace", importStatus.getErrorMessage());

        stackTrace = "";
        importStatus = new ImportStatus(null, null, null, null, null, null, 0, 0, null, null, null, null, stackTrace);
        assertNull("errormessage should be null for invalid stacktrace", importStatus.getErrorMessage());
    }

}