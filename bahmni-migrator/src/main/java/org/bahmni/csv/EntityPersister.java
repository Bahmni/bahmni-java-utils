package org.bahmni.csv;

public interface EntityPersister<T extends CSVEntity> {

    Messages persist(T csvEntity);

    Messages validate(T csvEntity);
}
