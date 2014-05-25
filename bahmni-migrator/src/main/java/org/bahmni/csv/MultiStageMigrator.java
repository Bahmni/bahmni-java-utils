package org.bahmni.csv;

import org.apache.log4j.Logger;
import org.bahmni.csv.exception.MigrationException;
import org.omg.CosNaming._BindingIteratorImplBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs Migration in Sequential Stages.
 */
public class MultiStageMigrator {

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

    public List<StageResult> migrate(CSVFile csvFile){
        List<CSVEntity> csvRowsFromFile = readCsvFileToMemory(csvFile);
        List<StageResult> stageResults = new ArrayList<>();
        String stageName = null;

        try{
            for(SimpleStage stage : stageList){
                stageName = stage.getName();
                logger.info("Invoking stage "+ stageName + " with row count: "+ csvRowsFromFile.size());
                StageResult stageResult = stage.execute(csvRowsFromFile);
                stageResults.add(stageResult);
            }
        }catch(MigrationException me){
            logger.error("Aborting! Migration Exception was thrown while executing stage '"+stageName+"': " + me.getMessage());
        }
        return stageResults;
    }

    private List<CSVEntity> readCsvFileToMemory(CSVFile csvFile) {
        try {
            CSVEntity csvEntity;
            List<CSVEntity> csvRowsFromFile = new ArrayList<>();
            csvFile.openForRead();
            while ((csvEntity = csvFile.readEntity()) != null) {
                csvRowsFromFile.add(csvEntity);
            }
            return csvRowsFromFile;
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


}
