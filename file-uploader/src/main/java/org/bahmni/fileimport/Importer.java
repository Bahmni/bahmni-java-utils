package org.bahmni.fileimport;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.EntityPersister;
import org.bahmni.fileimport.dao.JDBCConnectionProvider;

import java.io.File;

// This is an abstraction over a thread
class Importer<T extends CSVEntity> {
    private String originalFileName;
    private File csvFile;
    private final EntityPersister<T> persister;
    private final Class csvEntityClass;
    private String uploadedBy;

    private Thread thread;
    private FileImportThread<T> fileImportThread;

    public Importer(String originalFileName, File csvFile, EntityPersister<T> persister, Class csvEntityClass, String uploadedBy) {
        this.originalFileName = originalFileName;
        this.csvFile = csvFile;
        this.persister = persister;
        this.csvEntityClass = csvEntityClass;
        this.uploadedBy = uploadedBy;
    }

    public void start(JDBCConnectionProvider jdbcConnectionProvider, boolean skipValidation) {
        fileImportThread = new FileImportThread<>(originalFileName, csvFile, persister, csvEntityClass, jdbcConnectionProvider, uploadedBy, skipValidation);
        thread = new Thread(fileImportThread);
        thread.start();
    }

    public boolean isUploadFor(File csvFile) {
        return this.csvFile.getAbsolutePath().equals(csvFile.getAbsolutePath());
    }

    public void shutdown() {
        fileImportThread.shutdown();
    }
}
