package org.bahmni.fileimport;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportStatus {
    private final String id;
    private final String originalFileName;
    private final String savedFileName;
    private String errorFileName;
    private final String type;
    private final String status;
    private final int successfulRecords;
    private final int failedRecords;
    private final String stageName;
    private final String uploadedBy;
    private final Date startTime;
    private final Date endTime;
    private String stackTrace;
    private String errorMessage;

    public ImportStatus(String id, String originalFileName, String savedFileName, String errorFileName, String type, String status, int successfulRecords, int failedRecords, String stageName, String uploadedBy, Date startTime, Date endTime, String stackTrace) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.savedFileName = savedFileName;
        this.errorFileName = errorFileName;
        this.type = type;
        this.status = status;
        this.successfulRecords = successfulRecords;
        this.failedRecords = failedRecords;
        this.stageName = stageName;
        this.uploadedBy = uploadedBy;
        this.startTime = startTime;
        this.endTime = endTime;
        this.stackTrace = stackTrace;

        Pattern pattern = Pattern.compile(":([^\\n]+)",Pattern.DOTALL);
        if (stackTrace != null && !stackTrace.trim().isEmpty()) {
            Matcher matcher = pattern.matcher(stackTrace);
            if (matcher.find())
                this.errorMessage = matcher.group(1).trim();
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public void setErrorFileName(String errorFileName) {
        this.errorFileName = errorFileName;
    }

    public String getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getSavedFileName() {
        return savedFileName;
    }

    public String getErrorFileName() {
        return errorFileName;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public int getSuccessfulRecords() {
        return successfulRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public String getStageName() {
        return stageName;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}