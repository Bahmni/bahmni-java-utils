package org.bahmni.fileimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.CSVFile;
import org.bahmni.csv.EntityPersister;
import org.bahmni.common.db.JDBCConnectionProvider;

// External API to start the csv file import.
public class FileImporter<T extends CSVEntity> {
    private static Logger logger = LoggerFactory.getLogger(FileImporter.class);

    public boolean importCSV(String originalFileName, CSVFile csvFile, EntityPersister<T> persister, Class csvEntityClass, JDBCConnectionProvider jdbcConnectionProvider, String uploadedBy) {
        return importCSV(originalFileName, csvFile, persister, csvEntityClass, jdbcConnectionProvider, uploadedBy, false, 5);
    }

    public boolean importCSV(String originalFileName, CSVFile csvFile, EntityPersister<T> persister, Class csvEntityClass, JDBCConnectionProvider jdbcConnectionProvider, String uploadedBy, boolean skipValidation) {
        return importCSV(originalFileName, csvFile, persister, csvEntityClass, jdbcConnectionProvider, uploadedBy, skipValidation, 5);
    }

    public boolean importCSV(String originalFileName, CSVFile csvFile, EntityPersister<T> persister, Class csvEntityClass, JDBCConnectionProvider jdbcConnectionProvider, String uploadedBy, boolean skipValidation, int numberOfThreads) {
        logger.info("Starting file import thread for {}", csvFile.getAbsolutePath());
        try {
            Importer importer = ImportRegistry.register(originalFileName, csvFile, persister, csvEntityClass, uploadedBy, numberOfThreads);
            importer.start(jdbcConnectionProvider, skipValidation);
        } catch (Throwable e) {
            logger.error(String.valueOf(e));
            return false;
        }
        logger.info("Initiated upload in background for {}", csvFile.getAbsolutePath());
        return true;
    }


}
