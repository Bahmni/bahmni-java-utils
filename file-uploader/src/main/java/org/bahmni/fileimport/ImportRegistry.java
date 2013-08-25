package org.bahmni.fileimport;

import org.apache.log4j.Logger;
import org.bahmni.csv.*;
import org.bahmni.fileimport.dao.ImportStatusDao;
import org.bahmni.fileimport.dao.JDBCConnectionProvider;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Registry can manage the threads
public class ImportRegistry<T extends CSVEntity> {
    public static final int MAX_CONCURRENT_IMPORTS = 5;

    private static List<Importer> fileImportThreads = new ArrayList<>();

    public static boolean register(File csvFile, EntityPersister persister, Class csvEntityClass, JDBCConnectionProvider jdbcConnectionProvider) {
        if (fileImportThreads.size() > MAX_CONCURRENT_IMPORTS)
            // TODO : Mujir - make this an exception, with a message
            return false;

        for (Importer fileImportThread : fileImportThreads) {
            if (fileImportThread.isUploadFor(csvFile)) {
                // TODO : Mujir - make this an exception, with a message
               return false;
            }
        }

        Importer fileImportThread = new Importer(csvFile, persister, csvEntityClass);
        fileImportThreads.add(fileImportThread);
        // TODO : Mujir, Shruthi - registry does not need to start the threads. It just needs to register.. :)
        fileImportThread.start(jdbcConnectionProvider);

        return true;
    }

    public static void unregister(File csvFile) {
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

// This is an abstraction over a thread
class Importer<T extends CSVEntity> {

    private File csvFile;
    private final EntityPersister<T> persister;
    private final Class csvEntityClass;
    private Connection connection;

    private Thread thread;
    private FileImportThread<T> fileImportThread;

    public Importer(File csvFile, EntityPersister<T> persister, Class csvEntityClass) {
        this.csvFile = csvFile;
        this.persister = persister;
        this.csvEntityClass = csvEntityClass;
        this.connection = connection;
    }

    public void start(JDBCConnectionProvider jdbcConnectionProvider) {
        fileImportThread = new FileImportThread<>(csvFile, persister, csvEntityClass, jdbcConnectionProvider);
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




// Does the basic validation and import, and updates the status table.
// TODO : Update status table in db.
class   FileImportThread<T extends CSVEntity> implements Runnable{
    private File csvFile;
    private final EntityPersister<T> persister;
    private final Class csvEntityClass;
    private JDBCConnectionProvider jdbcConnectionProvider;

    private static Logger logger = Logger.getLogger(FileImportThread.class);
    private Migrator migrator;

    public FileImportThread(File csvFile, EntityPersister<T> persister, Class csvEntityClass, JDBCConnectionProvider jdbcConnectionProvider) {
        this.csvFile = csvFile;
        this.persister = persister;
        this.csvEntityClass = csvEntityClass;
        this.jdbcConnectionProvider = jdbcConnectionProvider;
    }

    public void run() {
        try {
            getNewImportStatusDao().saveInProgress(csvFile, "patient");

            migrator = new MigratorBuilder(csvEntityClass)
                    .readFrom(csvFile.getParent(), csvFile.getName())
                    .persistWith(persister)
                    .withMultipleValidators(1)
                    .withMultipleMigrators(1)
                    .build();

            MigrateResult migrateResult = migrator.migrate();
            logger.info("Migration was " + (migrateResult.hasFailed() ? "unsuccessful" : "successful"));
            logger.info("Stage : " + migrateResult.getStageName() + ". Success count : " + migrateResult.numberOfSuccessfulRecords() +
                    ". Fail count : " + migrateResult.numberOfFailedRecords());

            getNewImportStatusDao().saveCompleted(csvFile, "patient", migrateResult.numberOfSuccessfulRecords(), migrateResult.numberOfFailedRecords());

        } catch (Exception e) {
            logger.error("There was an error during migration. " + e.getMessage());
            try {
                getNewImportStatusDao().saveError(csvFile, "patient", e);
            } catch (SQLException e1) {
                logger.error(e1);
            }

        } finally {
            ImportRegistry.unregister(csvFile);
        }
    }

    public void shutdown() {
        migrator.shutdown();
    }

    private ImportStatusDao getNewImportStatusDao() {
        return new ImportStatusDao(jdbcConnectionProvider);
    }
}
