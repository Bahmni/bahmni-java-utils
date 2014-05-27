package org.bahmni.csv;

import java.util.List;
import java.util.concurrent.Callable;

public class SimpleStageCallable implements Callable<StageResult> {

     private SimpleStage simpleStage;
    private List csvEntityList;

    public SimpleStageCallable(SimpleStage simpleStage, List csvEntityList) {
        this.simpleStage = simpleStage;
        this.csvEntityList = csvEntityList;
    }

    @Override
    public StageResult call() throws Exception {
        return simpleStage.execute(csvEntityList);
    }
}
