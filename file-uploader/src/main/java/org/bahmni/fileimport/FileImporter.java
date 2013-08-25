package org.bahmni.fileimport;

import org.apache.log4j.Logger;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.EntityPersister;
import org.bahmni.fileimport.dao.JDBCConnectionProvider;

import java.io.File;

// Starts the importer thread. Gives ability to manage the thread.
public class FileImporter<T extends CSVEntity> {
    private static Logger logger = Logger.getLogger(FileImporter.class);

    public boolean importCSV(File csvFile, EntityPersister<T> persister, Class csvEntityClass, JDBCConnectionProvider jdbcConnectionProvider) {
        logger.info("Starting file import thread for " + csvFile.getAbsolutePath());
        return ImportRegistry.register(csvFile, persister, csvEntityClass, jdbcConnectionProvider);
    }

}
