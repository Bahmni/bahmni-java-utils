package org.bahmni.fonttransform;

import org.bahmni.csv.CSVEntity;

import java.util.List;

public interface EntityTransformer<T extends CSVEntity> {
    void transform(T csvEntity, List<TransformationMetaDatum> metaData);
}
