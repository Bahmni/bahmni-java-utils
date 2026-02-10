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

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.CSVFile;
import org.bahmni.csv.EntityPersister;
import org.bahmni.common.db.JDBCConnectionProvider;

// This is an abstraction over a thread
class Importer<T extends CSVEntity> {
    private final int numberOfThreads;
    private String originalFileName;
    private CSVFile csvFile;
    private final EntityPersister<T> persister;
    private final Class csvEntityClass;
    private String uploadedBy;

    private Thread thread;
    private FileImportThread<T> fileImportThread;

    public Importer(String originalFileName, CSVFile csvFile, EntityPersister persister, Class csvEntityClass, String uploadedBy, int numberOfThreads) {
        this.originalFileName = originalFileName;
        this.csvFile = csvFile;
        this.persister = persister;
        this.csvEntityClass = csvEntityClass;
        this.uploadedBy = uploadedBy;
        this.numberOfThreads = numberOfThreads;
    }

    public void start(JDBCConnectionProvider jdbcConnectionProvider, boolean skipValidation) {
        fileImportThread = new FileImportThread<>(originalFileName, csvFile, persister, csvEntityClass, jdbcConnectionProvider, uploadedBy, skipValidation, numberOfThreads);
        thread = new Thread(fileImportThread);
        thread.start();
    }

    public boolean isUploadFor(CSVFile csvFile) {
        return this.csvFile.getAbsolutePath().equals(csvFile.getAbsolutePath());
    }

    public void shutdown() {
        fileImportThread.shutdown();
    }
}
