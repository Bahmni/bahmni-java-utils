package org.bahmni.fonttransform;

public class TransformationMetaDatum {
    String columnName;
    TransformableFont transformFrom;
    TransformableFont transformTo;

    public TransformationMetaDatum(String columnName, TransformableFont transformFrom, TransformableFont transformTo) {
        this.columnName = columnName;
        this.transformFrom = transformFrom;
        this.transformTo = transformTo;
    }

    public String getColumnName() {
        return columnName;
    }

    public TransformableFont getTransformFrom() {
        return transformFrom;
    }

    public TransformableFont getTransformTo() {
        return transformTo;
    }
}
