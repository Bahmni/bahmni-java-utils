package org.bahmni.csv;

import java.util.List;

public class StageResult <T extends CSVEntity> {

    private String stageName;
    private List<T> allCsvEntities;
    private List<FailedRowResult<T>> failedCsvEntities;


    public StageResult(String stageName, List<FailedRowResult<T>> failedCsvEntities, List<T> allCsvEntities) {
        this.stageName = stageName;
        this.failedCsvEntities = failedCsvEntities;
        this.allCsvEntities = allCsvEntities;
    }


    public String getStageName() {
        return stageName;
    }

    public int getFailureCount() {
        if(failedCsvEntities == null)
            return 0;
        return failedCsvEntities.size();
    }

    public int getSuccessCount() {
        if(allCsvEntities == null || allCsvEntities.size() == 0)
            return 0;
        return allCsvEntities.size() - getFailureCount();
    }

    @Override
    public String toString() {
        return "Stage "+ getStageName() + " status: PASS:"+ getSuccessCount() + " FAIL: "+ getFailureCount();
    }

    public List<T> getAllCsvEntities() {
        return allCsvEntities;
    }

    public List<FailedRowResult<T>> getFailedCSVEntities() {
        return failedCsvEntities;
    }
}
