package org.bahmni.fileimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.CSVFile;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.MigrateResult;
import org.bahmni.csv.Migrator;
import org.bahmni.csv.MigratorBuilder;
import org.bahmni.fileimport.dao.ImportStatusDao;
import org.bahmni.common.db.JDBCConnectionProvider;

import java.sql.SQLException;

// Does the actual import. This includes basic validation, import and updating status tables..
class FileImportThread<T extends CSVEntity> implements Runnable {
    public int NUMBER_OF_VALIDATION_THREADS = 5;
    public int numberOfMigrationThreads;

    private String originalFileName;
    private CSVFile csvFile;
    private final EntityPersister<T> persister;
    private final Class csvEntityClass;
    private JDBCConnectionProvider jdbcConnectionProvider;
    private String uploadedBy;
    private boolean skipValidation;

    private static Logger logger = LoggerFactory.getLogger(FileImportThread.class);
    private Migrator migrator;

    public FileImportThread(String originalFileName, CSVFile csvFile, EntityPersister<T> persister, Class csvEntityClass, JDBCConnectionProvider jdbcConnectionProvider, String uploadedBy, boolean skipValidation, int numberOfThreads) {
        this.originalFileName = originalFileName;
        this.csvFile = csvFile;
        this.persister = persister;
        this.csvEntityClass = csvEntityClass;
        this.jdbcConnectionProvider = jdbcConnectionProvider;
        this.uploadedBy = uploadedBy;
        this.skipValidation = skipValidation;
        numberOfMigrationThreads = numberOfThreads;
    }

    public void run() {
        try {
            getNewImportStatusDao().saveInProgress(originalFileName, csvFile, csvEntityClass.getSimpleName(), uploadedBy);

            if (skipValidation) {
                migrator = new MigratorBuilder(csvEntityClass)
                        .readFrom(csvFile.getBasePath(), csvFile.getRelativePath())
                        .persistWith(persister)
                        .skipValidation()
                        .withMultipleMigrators(numberOfMigrationThreads)
                        .withAllRecordsInValidationErrorFile()
                        .build();
            } else {
                migrator = new MigratorBuilder(csvEntityClass)
                        .readFrom(csvFile.getBasePath(), csvFile.getRelativePath())
                        .persistWith(persister)
                        .withMultipleValidators(NUMBER_OF_VALIDATION_THREADS)
                        .withMultipleMigrators(numberOfMigrationThreads)
                        .withAllRecordsInValidationErrorFile()
                        .build();
            }
            MigrateResult migrateResult = migrator.migrate();
            getNewImportStatusDao().saveFinished(csvFile, csvEntityClass.getSimpleName(), migrateResult);

            logger.info("Migration was {}", (migrateResult.hasFailed() ? "unsuccessful" : "successful"));
            logger.info("Stage : {}. Success count : {}. Fail count : {}", migrateResult.getStageName(), migrateResult.numberOfSuccessfulRecords(), migrateResult.numberOfFailedRecords());

        } catch (Throwable e) {
            logger.error("There was an error during migration. {}", e.getMessage(), e);
            try {
                getNewImportStatusDao().saveFatalError(csvFile, csvEntityClass.getSimpleName(), e);
                migrator.shutdown();
            } catch (SQLException e1) {
                logger.error(String.valueOf(e1));
            }
        } finally {
            ImportRegistry.unregister(csvFile);
        }
    }

    public void shutdown() {
        migrator.shutdown();
        try {
            getNewImportStatusDao().saveFatalError(csvFile, csvEntityClass.getSimpleName(), "Server shutdown");
        } catch (SQLException e1) {
            logger.error(String.valueOf(e1));
        }
    }

    private ImportStatusDao getNewImportStatusDao() {
        return new ImportStatusDao(jdbcConnectionProvider);
    }
}
