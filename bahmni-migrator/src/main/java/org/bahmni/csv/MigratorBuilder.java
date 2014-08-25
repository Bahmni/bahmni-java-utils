package org.bahmni.csv;

public class MigratorBuilder<T extends CSVEntity> {

    public static final String VALIDATION_ERROR_FILE_EXTENSION = ".val.err";
    public static final String MIGRATION_ERROR_FILE_EXTENSION = ".err";

    private String inputCSVFileLocation;
    private String inputCSVFileName;
    private EntityPersister<T> entityPersister;
    private final Class<T> entityClass;
    private int numberOfValidationThreads = 1;
    private int numberOfMigrationThreads = 1;
    private boolean withAllRecordsInValidationErrorFile;
    private boolean abortIfAnyStageFails = true;
    private boolean skipValidation;

    public MigratorBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public MigratorBuilder<T> readFrom(String inputCSVFileLocation, String inputCSVFileName) {
        this.inputCSVFileLocation = inputCSVFileLocation;
        this.inputCSVFileName = inputCSVFileName;
        return this;
    }

    public MigratorBuilder<T> persistWith(EntityPersister<T> entityPersister) {
        this.entityPersister = entityPersister;
        return this;
    }

    public MigratorBuilder<T> withAllRecordsInValidationErrorFile() {
        this.withAllRecordsInValidationErrorFile = true;
        return this;
    }

    public MigratorBuilder<T> dontAbortOnStageFailure() {
        this.abortIfAnyStageFails = false;
        return this;
    }

    public MigratorBuilder<T> withMultipleMigrators(int numberOfMigrationThreads) {
        if (numberOfMigrationThreads < 0)
            throw new RuntimeException("Invalid number of threads. numberOfMigrationThreads:" + numberOfMigrationThreads);
        this.numberOfMigrationThreads = numberOfMigrationThreads;
        return this;
    }

    public MigratorBuilder skipValidation() {
        this.skipValidation = true;
        return this;
    }

    public MigratorBuilder<T> withMultipleValidators(int numberOfValidationThreads) {
        if (numberOfValidationThreads < 0)
            throw new RuntimeException("Invalid number of threads. numberOfValidationThreads:" + numberOfValidationThreads);
        this.numberOfValidationThreads = numberOfValidationThreads;
        return this;
    }

    public Migrator<T> build() {
        CSVFile inputCSVFile = new CSVFile(inputCSVFileLocation, inputCSVFileName);

        Stages allStages = new Stages();
        if (!skipValidation) {
            allStages.addStage(getValidationStage(inputCSVFile));
        }
        allStages.addStage(getMigrationStage(inputCSVFile));

        return new Migrator<>(entityPersister, allStages, entityClass, abortIfAnyStageFails);
    }

    private Stage getMigrationStage(CSVFile inputCSVFile) {
        CSVFile migrationErrorFile = new CSVFile(inputCSVFileLocation, errorFileName(inputCSVFileName, MIGRATION_ERROR_FILE_EXTENSION));
        return new StageBuilder().migration().withInputFile(inputCSVFile).withErrorFile(migrationErrorFile).withNumberOfThreads(numberOfMigrationThreads).build();
    }

    private Stage getValidationStage(CSVFile inputCSVFile) {
        CSVFile validationErrorFile = new CSVFile(inputCSVFileLocation, errorFileName(inputCSVFileName, VALIDATION_ERROR_FILE_EXTENSION));
        if (withAllRecordsInValidationErrorFile) {
            return new StageBuilder().validationWithAllRecordsInErrorFile().withInputFile(inputCSVFile).withErrorFile(validationErrorFile).withNumberOfThreads(numberOfValidationThreads).build();
        } else {
            return new StageBuilder().validation().withInputFile(inputCSVFile).withErrorFile(validationErrorFile).withNumberOfThreads(numberOfValidationThreads).build();
        }
    }

    private String errorFileName(String fileName, String fileNameAddition) {
        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        return fileNameWithoutExtension + fileNameAddition + fileExtension;
    }
}
