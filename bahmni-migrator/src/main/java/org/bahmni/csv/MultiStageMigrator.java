package org.bahmni.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bahmni.csv.exception.MigrationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Runs Migration in Sequential Stages.
 */
public class MultiStageMigrator<T extends CSVEntity> {

    private CSVFile csvFile = null;
    private List<SimpleStage> stageList = null;
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public MultiStageMigrator() {
        stageList = new ArrayList<>();
    }

    public MultiStageMigrator(List<SimpleStage> stageList) {
        this.stageList = stageList;
    }

    public MultiStageMigrator addStage(SimpleStage stage) {
        this.stageList.add(stage);
        return this;
    }

    public List<StageResult> migrate(CSVFile csvFile, Class entityClass) {
        List<T> csvRowsForNextStage = readCsvFileToMemory(csvFile, entityClass);
        List<StageResult> stageResults = new ArrayList<>();
        String stageName = null;

        try {
            for (SimpleStage stage : stageList) {
                stageName = stage.getName();
                StageResult stageResult;
                if (stage.canRunInParallel()) {
                    stageResult = runInParallel(stage, csvRowsForNextStage);
                } else {
                    stageResult = stage.execute(csvRowsForNextStage);
                }
                logger.info("Invoking stage {} with row count: {}", stageName, csvRowsForNextStage.size());

                stageResults.add(stageResult);
                csvRowsForNextStage = removeFailedRows(stageResult.getAllCsvEntities(), stageResult.getFailedCSVEntities());
            }
        } catch (MigrationException me) {
            logger.error("Aborting! Migration Exception was thrown while executing stage '{}': {} ", stageName, me.getMessage());
        } catch (InterruptedException e) {
            //TODO
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        createFailureCsv(stageResults, csvFile, entityClass);
        return stageResults;
    }

    private StageResult runInParallel(SimpleStage stage, List<T> csvRowsForNextStage) throws ExecutionException, InterruptedException {
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        int partitionSize = csvRowsForNextStage.size() / numberOfThreads;
        ArrayList<Future<StageResult>> futureResults = new ArrayList<>();
        int beginIndex = 0, endIndex = partitionSize;
        for (int i = 0; i < numberOfThreads; i++) {

            List<T> partionSetCSVRows = csvRowsForNextStage.subList(beginIndex, endIndex);
            Future<StageResult> futureStageResult = executorService.submit(new SimpleStageCallable(stage, partionSetCSVRows));

            futureResults.add(futureStageResult);

            beginIndex = endIndex;
            endIndex = (i < numberOfThreads - 2) ? endIndex + partitionSize : csvRowsForNextStage.size();
        }

        List<FailedRowResult> failedCSVRows = new ArrayList<>();
        List allCSVRows = new ArrayList();

        for (Future<StageResult> futureResult : futureResults) {
            StageResult stageResult = futureResult.get();
            List failedCSVEntities = stageResult.getFailedCSVEntities();
            List allCsvEntities = stageResult.getAllCsvEntities();

            if (failedCSVEntities != null && failedCSVEntities.size() > 0) {
                failedCSVRows.addAll(failedCSVEntities);
            }
            if (allCsvEntities != null && allCsvEntities.size() > 0) {
                allCSVRows.addAll(allCsvEntities);
            }
        }

        return new StageResult(stage.getName(), failedCSVRows, allCSVRows);

    }

    private void createFailureCsv(List<StageResult> stageResults, CSVFile inputCSVFile, Class entityClass) {
        CSVFile errorCsvFile = new CSVFile(inputCSVFile.getBasePath(), inputCSVFile.getRelativePath() + "_err.csv");
        for (StageResult<T> stageResult : stageResults) {
            List<FailedRowResult<T>> failedCSVEntities = stageResult.getFailedCSVEntities();
            if (failedCSVEntities == null || failedCSVEntities.size() == 0)
                continue;
            for (FailedRowResult<T> failedCSVEntity : failedCSVEntities) {
                try {
                    errorCsvFile.writeARecord(failedCSVEntity, inputCSVFile.getHeaderRow());
                } catch (IOException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }
        errorCsvFile.close();
    }

    private List<T> removeFailedRows(List<T> allCsvEntities, List<FailedRowResult<T>> failedCSVEntities) {
        if (failedCSVEntities == null || failedCSVEntities.size() == 0)
            return allCsvEntities;

        for (FailedRowResult<T> failedCSVEntity : failedCSVEntities) {
            allCsvEntities.remove(failedCSVEntity.getCsvEntity());
        }
        return allCsvEntities;
    }

    private List<T> readCsvFileToMemory(CSVFile csvFile, Class<T> entityClass) {
        try {
            T csvEntity;
            List<T> csvRowsFromFile = new ArrayList<>();
            csvFile.openForRead();
            while ((csvEntity = (T) csvFile.readEntity(entityClass)) != null) {
                csvRowsFromFile.add(csvEntity);
            }
            return csvRowsFromFile;
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
