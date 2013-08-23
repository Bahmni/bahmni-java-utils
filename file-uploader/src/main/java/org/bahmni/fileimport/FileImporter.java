package org.bahmni.fileimport;

import org.apache.log4j.Logger;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.EntityPersister;

import java.io.File;

// Starts the importer thread. Gives ability to manage the thread.
public class FileImporter<T extends CSVEntity> {
    private static Logger logger = Logger.getLogger(FileImporter.class);

    public boolean importCSV(String fileLocation, String fileName, EntityPersister<T> persister, Class csvEntityClass) {
        logger.info("Starting file import thread for " + fileLocation + "/" + fileName);
        return ImportRegistry.register(fileLocation, fileName, persister, csvEntityClass);
    }

    public boolean importCSV(File csvFile, EntityPersister<T> persister, Class csvEntityClass) {
        return importCSV(csvFile.getParent(), csvFile.getName(), persister, csvEntityClass);
    }

}
