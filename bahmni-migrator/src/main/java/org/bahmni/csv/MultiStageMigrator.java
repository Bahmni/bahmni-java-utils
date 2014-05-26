package org.bahmni.csv;

import org.apache.log4j.Logger;
import org.bahmni.csv.exception.MigrationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs Migration in Sequential Stages.
 */
public class MultiStageMigrator<T extends CSVEntity> {

    private CSVFile csvFile = null;
    private List<SimpleStage> stageList = null;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public MultiStageMigrator() {
        stageList = new ArrayList<>();
    }

    public MultiStageMigrator(List<SimpleStage> stageList) {
        this.stageList = stageList;
    }

    public MultiStageMigrator addStage(SimpleStage stage){
        this.stageList.add(stage);
        return this;
    }

    public List<StageResult> migrate(CSVFile csvFile, Class entityClass){
        List<T> csvRowsForNextStage = readCsvFileToMemory(csvFile);
        List<StageResult> stageResults = new ArrayList<>();
        String stageName = null;

        try{
            for(SimpleStage stage : stageList){
                stageName = stage.getName();
                logger.info("Invoking stage " + stageName + " with row count: " + csvRowsForNextStage.size());
                StageResult stageResult = stage.execute(csvRowsForNextStage);
                stageResults.add(stageResult);
                csvRowsForNextStage = removeFailedRows(stageResult.getAllCsvEntities(), stageResult.getFailedCSVEntities());
            }
        }catch(MigrationException me){
            logger.error("Aborting! Migration Exception was thrown while executing stage '"+stageName+"': " + me.getMessage());
        }
        createFailureCsv(stageResults, csvFile, entityClass);
        return stageResults;
    }

    private void createFailureCsv(List<StageResult> stageResults, CSVFile inputCSVFile, Class entityClass) {
        CSVFile errorCsvFile = new CSVFile(inputCSVFile.getFileLocation(), inputCSVFile.getFileName() + "_err.csv", entityClass);
        for (StageResult<T> stageResult : stageResults) {
            List<FailedRowResult<T>> failedCSVEntities = stageResult.getFailedCSVEntities();
            if(failedCSVEntities == null || failedCSVEntities.size() == 0)
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
        if(failedCSVEntities == null || failedCSVEntities.size() == 0)
            return allCsvEntities;

        for (FailedRowResult<T> failedCSVEntity : failedCSVEntities) {
            allCsvEntities.remove(failedCSVEntity.getCsvEntity());
        }
        return allCsvEntities;
    }

    private List<T> readCsvFileToMemory(CSVFile csvFile) {
        try {
            T csvEntity;
            List<T> csvRowsFromFile = new ArrayList<>();
            csvFile.openForRead();
            while ((csvEntity = (T)csvFile.readEntity()) != null) {
                csvRowsFromFile.add(csvEntity);
            }
            return csvRowsFromFile;
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
