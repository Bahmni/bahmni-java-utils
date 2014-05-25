package org.bahmni.csv;

public class StageResult {

    private String stageName;
    private int failureCount;
    private int successCount;
    private String message;

    public StageResult(String stageName, int failureCount, int successCount, String message) {
        this.stageName = stageName;
        this.failureCount = failureCount;
        this.successCount = successCount;
        this.message = message;
    }


    public String getStageName() {
        return stageName;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public String getMessage() {
        return message;
    }
}
