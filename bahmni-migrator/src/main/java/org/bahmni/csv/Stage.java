package org.bahmni.csv;

import org.apache.log4j.Logger;
import org.bahmni.csv.exception.MigrationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Stage<T extends CSVEntity> {
    public static final String VALIDATION_WITH_ALL_RECORDS_IN_ERROR_FILE_STAGENAME = "validate.e";
    public static final String VALIDATION_STAGENAME = "validation";
    public static final String MIGRATION_STAGENAME = "migration";

    private String stageName;

    private int numberOfThreads;
    private CSVFile inputCSVFile;
    private CSVFile errorFile;

    private ExecutorService executorService;

    private static Logger logger = Logger.getLogger(Stage.class);

    private Stage(String stageName, CSVFile<T> errorFile, int numberOfThreads, CSVFile inputCSVFile) {
        this.stageName = stageName;
        this.errorFile = errorFile;
        this.numberOfThreads = numberOfThreads;
        this.inputCSVFile = inputCSVFile;
    }

    public Callable<RowResult> getCallable(EntityPersister entityPersister, CSVEntity csvEntity) {
        // TODO : Mujir - can we do this more elegantly? If not for csvEntity we could inject Callable by constructor
        if (this.stageName.equals(VALIDATION_STAGENAME) || this.stageName.equals(VALIDATION_WITH_ALL_RECORDS_IN_ERROR_FILE_STAGENAME))
            return new ValidationCallable(entityPersister, csvEntity);

        return new MigrationCallable(entityPersister, csvEntity);
    }

    public MigrateResult<T> run(EntityPersister entityPersister, Class<T> csvEntityClass) throws IOException, IllegalAccessException, InstantiationException {
        logger.info("Starting " + stageName + " Stage with file - " + inputCSVFile.getAbsolutePath());
        MigrateResult<T> stageResult = new MigrateResult<>(stageName);

        executorService = Executors.newFixedThreadPool(numberOfThreads, new BahmniThreadFactory());
        try {
            inputCSVFile.openForRead();

            CSVEntity csvEntity = inputCSVFile.readEntity(csvEntityClass);
            boolean isErrorFileHeaderRecordWritten = false;
            while (csvEntity != null) {
                List<Future<RowResult>> results = new ArrayList<>();

                for (int i = 0; i <= numberOfThreads; i++) {
                    if (csvEntity == null) {
                        break;
                    }

                    Future<RowResult> rowResult = executorService.submit(getCallable(entityPersister, csvEntity));
                    results.add(rowResult);

                    csvEntity = inputCSVFile.readEntity(csvEntityClass);
                }

                // TODO : Mujir - if multiple jobs are submitted, and one of them fails fatally in code below,
                // TODO : the executor service would get closed, and throw RejectedExecutionException
                for (Future<RowResult> result : results) {
                    RowResult<T> rowResult = result.get();
                    stageResult.addResult(rowResult);

                    if (this.stageName.equals(VALIDATION_WITH_ALL_RECORDS_IN_ERROR_FILE_STAGENAME) && rowResult.isSuccessful()) {
                        addErrorHeader(isErrorFileHeaderRecordWritten, errorFile, inputCSVFile.getHeaderRow());
                        isErrorFileHeaderRecordWritten = true;

                        errorFile.writeARecord(rowResult);
                    }

                    if (!rowResult.isSuccessful()) {
                        logger.info("Failed for record - " + rowResult.getRowWithErrorColumnAsString());

                        addErrorHeader(isErrorFileHeaderRecordWritten, errorFile, inputCSVFile.getHeaderRow());
                        isErrorFileHeaderRecordWritten = true;

                        errorFile.writeARecord(rowResult);
                        // TODO : Mujir - not entirely clean. Can this be merged with stageResult.addResult(rowResult); ????
                        stageResult.setErrorFile(errorFile);
                    }
                }

                errorFile.close();
            }

        } catch (InterruptedException e) {
            logger.error("Thread interrupted exception. " + getStackTrace(e));
            throw new MigrationException("Could not execute threads", e);
        } catch (ExecutionException e) {
            logger.error("Could not execute threads. " + getStackTrace(e));
            throw new MigrationException("Could not execute threads", e);
        } finally {
            logger.info("Stage : " + stageName + ". Successful records count : " + stageResult.numberOfSuccessfulRecords() + ". Failed records count : " + stageResult.numberOfFailedRecords());
            closeResources();

            if (this.stageName.equals(VALIDATION_WITH_ALL_RECORDS_IN_ERROR_FILE_STAGENAME) && !stageResult.hasFailed())
                errorFile.delete();
        }
        return stageResult;
    }

    private void addErrorHeader(boolean areErrorsFound, CSVFile errorFile, String[] headerRow) throws IOException {
        if (!areErrorsFound) {
            errorFile.writeHeaderRecord(headerRow);
        }
    }

    public void closeResources() {
        if (executorService != null) executorService.shutdownNow();
        if (inputCSVFile != null) inputCSVFile.close();
        if (errorFile != null) errorFile.close();
    }

    private static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    @Override
    public String toString() {
        return stageName;
    }

    String getName() {
        return stageName;
    }

    public static Stage validation(CSVFile errorFile, int numberOfThreads, CSVFile inputCSVFileLocation) {
        return new Stage(VALIDATION_STAGENAME, errorFile, numberOfThreads, inputCSVFileLocation);
    }

    public static Stage validationWithAllRecordsInErrorFile(CSVFile errorFile, int numberOfThreads, CSVFile inputCSVFileLocation) {
        return new Stage(VALIDATION_WITH_ALL_RECORDS_IN_ERROR_FILE_STAGENAME, errorFile, numberOfThreads, inputCSVFileLocation);
    }

    public static Stage migration(CSVFile errorFile, int numberOfThreads, CSVFile inputCSVFileLocation) {
        return new Stage(MIGRATION_STAGENAME, errorFile, numberOfThreads, inputCSVFileLocation);
    }
}

class BahmniThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    BahmniThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);

        t.setName("Bahmni_File_Importer:poolNumber:" + poolNumber + "_threadNumber:" + threadNumber);
        return t;
    }
}

class StageBuilder<T extends CSVEntity> {
    private CSVFile<T> errorFile;
    private int numberOfThreads;
    private CSVFile inputCSVFile;

    private boolean isValidationWithAllErrors;
    private boolean isValidation;
    private boolean isMigration;

    public StageBuilder<T> validation() {
        isValidation = true;
        return this;
    }

    public StageBuilder<T> validationWithAllRecordsInErrorFile() {
        isValidationWithAllErrors = true;
        return this;
    }

    public StageBuilder<T> migration() {
        isMigration = true;
        return this;
    }

    public StageBuilder<T> withErrorFile(CSVFile<T> validationErrorFile) {
        this.errorFile = validationErrorFile;
        return this;
    }

    public StageBuilder<T> withNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
        return this;
    }

    public StageBuilder withInputFile(CSVFile inputCSVFile) {
        this.inputCSVFile = inputCSVFile;
        return this;
    }

    public Stage build() {
        if (isValidationWithAllErrors)
            return Stage.validationWithAllRecordsInErrorFile(errorFile, numberOfThreads, inputCSVFile);
        else if (isValidation)
            return Stage.validation(errorFile, numberOfThreads, inputCSVFile);
        else
            return Stage.migration(errorFile, numberOfThreads, inputCSVFile);
    }
}