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
import org.bahmni.fileimport.exception.FileImportException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Registry can manage the threads
public class ImportRegistry<T extends CSVEntity> {
    public static final int MAX_CONCURRENT_IMPORTS = 5;

    private static List<Importer> fileImportThreads = new ArrayList<>();

    public static Importer register(String originalFileName, CSVFile csvFile, EntityPersister persister, Class csvEntityClass, String uploadedBy, int numberOfThreads) {
        if (fileImportThreads.size() > MAX_CONCURRENT_IMPORTS)
            throw new FileImportException("Maximum number of concurrent uploads reached. Max Concurrent uploads - " +
                    MAX_CONCURRENT_IMPORTS + ". Current concurrent uploads - " + fileImportThreads.size() + ".");

        Importer fileImportThread = new Importer(originalFileName, csvFile, persister, csvEntityClass, uploadedBy, numberOfThreads);
        fileImportThreads.add(fileImportThread);
        return fileImportThread;
    }

    public static void unregister(CSVFile csvFile) {
        Iterator<Importer> iterator = fileImportThreads.iterator();
        while (iterator.hasNext()) {
            Importer fileImportThread = iterator.next();
            if (fileImportThread.isUploadFor(csvFile)) {
                iterator.remove();
            }
        }
    }

    public static void shutdownAllThreads() {
        for (Importer fileImportThread : fileImportThreads) {
            fileImportThread.shutdown();
        }
    }
}