package org.bahmni.csv;

public class MigrateResult<T extends CSVEntity> {
    private int successCount;
    private int failCount;
    private String stageName;
    private CSVFile errorFile;

    public MigrateResult(String stageName) {
        this.stageName = stageName;
    }

    public boolean hasFailed() {
        return failCount > 0;
    }

    public void addResult(RowResult<T> rowResult) {
        if (rowResult.isSuccessful()) {
            successCount++;
        } else {
            failCount++;
        }
    }

    public int numberOfFailedRecords() {
        return failCount;
    }

    public int numberOfSuccessfulRecords() {
        return successCount;
    }

    public String getStageName() {
        return stageName;
    }

    public void setErrorFile(CSVFile errorFile) {
        this.errorFile = errorFile;
    }

    public String getErrorFileName() {
        if (errorFile == null)
            return null;

        return errorFile.getRelativePath();
    }

    public boolean isValidationStage() {
        return stageName.equals(Stage.VALIDATION_STAGENAME);
    }

    @Override
    public String toString() {
        String s = "Stage Name: " + stageName + " Status: "+
                ((hasFailed())? "Failed " : "Passed ") +
                " Success Count : " + successCount +
                " Fail Count: " + failCount;
        return s;
    }
}
