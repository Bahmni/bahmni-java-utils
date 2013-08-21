import org.apache.log4j.Logger;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.EntityPersister;

public class FileImporter {
    private static Logger logger = Logger.getLogger(FileImporter.class);

    public void importCSV(String fileLocation, String fileName, EntityPersister<CSVEntity> persister, Class csvEntityClass) {

//        ExecutorService fileImportExecutorService = Executors.newSingleThreadExecutor();
//        fileImportExecutorService.
//
//
//        org.bahmni.csv.Migrator migrator = new MigratorBuilder(csvEntityClass)
//                .readFrom(fileLocation, fileName)
//                .persistWith(persister)
//                .withMultipleValidators(1)
//                .withMultipleMigrators(1)
//                .build();
//        try {
//            MigrateResult migrateResult = migrator.migrate();
//            logger.info("Migration was " + (migrateResult.hasFailed() ? "unsuccessful" : "successful"));
//            logger.info("Stage : " + migrateResult.getStageName() + ". Success count : " + migrateResult.numberOfSuccessfulRecords() +
//                    ". Fail count : " + migrateResult.numberOfFailedRecords());
//        } catch (MigrationException e) {
//            logger.error("There was an error during migration. " + e.getMessage());
//        }

    }
}
