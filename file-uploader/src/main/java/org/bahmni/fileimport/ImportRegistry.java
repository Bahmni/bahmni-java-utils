package org.bahmni.fileimport;

import org.apache.log4j.Logger;
import org.bahmni.csv.*;
import org.bahmni.csv.exception.MigrationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Registry can manage the threads
public class ImportRegistry<T extends CSVEntity> {
    public static final int MAX_CONCURRENT_IMPORTS = 5;

    private static List<Importer> fileImportThreads = new ArrayList<>();

    public static boolean register(String fileLocation, String fileName, EntityPersister persister, Class csvEntityClass) {
        if (fileImportThreads.size() > MAX_CONCURRENT_IMPORTS)
            // TODO : Mujir - make this an exception, with a message
            return false;

        for (Importer fileImportThread : fileImportThreads) {
            if (fileImportThread.isUploadFor(fileLocation, fileName)) {
                // TODO : Mujir - make this an exception, with a message
               return false;
            }
        }

        Importer fileImportThread = new Importer(fileLocation, fileName, persister, csvEntityClass);
        fileImportThreads.add(fileImportThread);
        // TODO : Mujir, Shruthi - registry does not need to start the threads. It just needs to register.. :)
        fileImportThread.start();

        return true;
    }

    public static void unregister(String fileLocation, String fileName) {
        Iterator<Importer> iterator = fileImportThreads.iterator();
        while (iterator.hasNext()) {
            Importer fileImportThread = iterator.next();
            if (fileImportThread.isUploadFor(fileLocation, fileName)) {
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

    private final String fileLocation;
    private final String fileName;
    private final EntityPersister<T> persister;
    private final Class csvEntityClass;

    private Thread thread;
    private FileImportThread<T> fileImportThread;

    public Importer(String fileLocation, String fileName, EntityPersister<T> persister, Class csvEntityClass) {
        this.fileLocation = fileLocation;
        this.fileName = fileName;
        this.persister = persister;
        this.csvEntityClass = csvEntityClass;
    }

    public void start() {
        fileImportThread = new FileImportThread<>(fileLocation, fileName, persister, csvEntityClass);
        thread = new Thread(fileImportThread);
        thread.start();
    }

    public boolean isUploadFor(String fileLocation, String fileName) {
        return this.fileLocation.equals(fileLocation) && this.fileName.equals(fileName);
    }

    public void shutdown() {
        fileImportThread.shutdown();
    }
}




// Does the basic validation and import, and updates the status table.
// TODO : Update status table in db.
class FileImportThread<T extends CSVEntity> implements Runnable{
    private final String fileLocation;
    private final String fileName;
    private final EntityPersister<T> persister;
    private final Class csvEntityClass;

    private static Logger logger = Logger.getLogger(FileImportThread.class);
    private Migrator migrator;

    public FileImportThread(String fileLocation, String fileName, EntityPersister<T> persister, Class csvEntityClass) {
        this.fileLocation = fileLocation;
        this.fileName = fileName;
        this.persister = persister;
        this.csvEntityClass = csvEntityClass;
    }

    public void run() {
        migrator = new MigratorBuilder(csvEntityClass)
                .readFrom(fileLocation, fileName)
                .persistWith(persister)
                .withMultipleValidators(1)
                .withMultipleMigrators(1)
                .build();
        try {
            MigrateResult migrateResult = migrator.migrate();
            logger.info("Migration was " + (migrateResult.hasFailed() ? "unsuccessful" : "successful"));
            logger.info("Stage : " + migrateResult.getStageName() + ". Success count : " + migrateResult.numberOfSuccessfulRecords() +
                    ". Fail count : " + migrateResult.numberOfFailedRecords());
        } catch (MigrationException e) {
            logger.error("There was an error during migration. " + e.getMessage());
        } finally {
            ImportRegistry.unregister(fileLocation, fileName);
        }

    }

    public void shutdown() {
        migrator.shutdown();
    }
}
