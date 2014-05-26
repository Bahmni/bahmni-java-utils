package org.bahmni.csv;

public class FailedRowResult<T extends CSVEntity> extends RowResult<T> {
    public FailedRowResult(T csvEntity) {
        super(csvEntity);
    }

    public FailedRowResult(T csvEntity, Throwable exception) {
        super(csvEntity, exception);
    }

    public FailedRowResult(T csvEntity, String errorMessage) {
        super(csvEntity, errorMessage);
    }
}
