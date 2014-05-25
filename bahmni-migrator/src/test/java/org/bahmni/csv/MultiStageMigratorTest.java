package org.bahmni.csv;

import org.bahmni.csv.exception.MigrationException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MultiStageMigratorTest {

    @Test
    public void shouldInvokeStageWithMultipleRows() throws IOException, InstantiationException, IllegalAccessException {

        MultiStageMigrator multiStageMigrator = new MultiStageMigrator();
        multiStageMigrator.addStage(createDummyStage());
        List<StageResult> stageResults = multiStageMigrator.migrate(createMockCSVFile());
        assertEquals(1, stageResults.size());
        assertEquals(5, stageResults.get(0).getSuccessCount());

    }


    @Test
    public void shouldInvokeTwoStageWithMultipleRows() throws IOException, InstantiationException, IllegalAccessException {

        MultiStageMigrator multiStageMigrator = new MultiStageMigrator();
        multiStageMigrator
                .addStage(createDummyStage())
                .addStage(createDummyStage());
        List<StageResult> stageResults = multiStageMigrator.migrate(createMockCSVFile());
        assertEquals(2, stageResults.size());
        assertEquals(5, stageResults.get(0).getSuccessCount());
        assertEquals(5, stageResults.get(1).getSuccessCount());

    }

    @Test
    public void shouldAbortIfOneStageThrowsException() throws IOException, InstantiationException, IllegalAccessException {

        MultiStageMigrator multiStageMigrator = new MultiStageMigrator();
        multiStageMigrator
                .addStage(createDummyStage())
                .addStage(createDummyStage())
                .addStage(createDummyErrorStage())
                .addStage(createDummyStage());

        List<StageResult> stageResults = multiStageMigrator.migrate(createMockCSVFile());
        assertEquals("Should receive on 2 stage results",2, stageResults.size());

    }

    private CSVFile createMockCSVFile() throws IOException, IllegalAccessException, InstantiationException {
        CSVFile mockInputFile = mock(CSVFile.class);
        doNothing().when(mockInputFile).openForRead();
        doNothing().when(mockInputFile).close();

        when(mockInputFile.getHeaderRow()).thenReturn(new String[]{"id", "name"});
        when(mockInputFile.readEntity())
                .thenReturn(new DummyCSVEntity("1", "dummyEntity1"))
                .thenReturn(new DummyCSVEntity("2", "dummyEntity2"))
                .thenReturn(new DummyCSVEntity("3", "dummyEntity3"))
                .thenReturn(new DummyCSVEntity("4", "dummyEntity4"))
                .thenReturn(new DummyCSVEntity("5", "dummyEntity5"))
                .thenReturn(null);
        return mockInputFile;
    }



    private SimpleStage createDummyStage() {
        SimpleStage dummyStage = new SimpleStage() {
            @Override
            public String getName() {
                return "DummyStage";
            }

            @Override
            public boolean canRunInParallel() {
                return false;
            }

            @Override
            public StageResult execute(List<CSVEntity> csvEntityList) throws MigrationException {
                System.out.println("Invoked with list of size: " + csvEntityList.size());
                return new StageResult(getName(), 0, csvEntityList.size(), "All good. Validation passed.");
            }
        };
        return dummyStage;
    }

    private SimpleStage createDummyErrorStage() {
        SimpleStage dummyErrorStage = new SimpleStage() {
            @Override
            public String getName() {
                return "DummyERRORStage";
            }

            @Override
            public boolean canRunInParallel() {
                return false;
            }

            @Override
            public StageResult execute(List<CSVEntity> csvEntityList) throws MigrationException {
                throw new MigrationException("Unable to proceed. Data is invalid!");
            }
        };
        return dummyErrorStage;
    }


}
