package org.bahmni.csv;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.csv.exception.MigrationException;

import java.util.concurrent.Callable;

class ValidationCallable<T extends CSVEntity> implements Callable<RowResult<T>> {
    private final EntityPersister entityPersister;
    private final T csvEntity;

    private static Logger logger = LogManager.getLogger(ValidationCallable.class);

    public ValidationCallable(EntityPersister entityPersister, T csvEntity) {
        this.entityPersister = entityPersister;
        this.csvEntity = csvEntity;
    }

    @Override
    public RowResult<T> call() throws Exception {
        try {
            return new RowResult<>(csvEntity, entityPersister.validate(csvEntity));
        } catch (Exception e) {
            logger.error("failed while validating. Record - " + StringUtils.join(csvEntity.getOriginalRow().toArray()));
            throw new MigrationException(e);
        }
    }
}


class MigrationCallable<T extends CSVEntity> implements Callable<RowResult<T>> {
    private final EntityPersister entityPersister;
    private final T csvEntity;

    private static Logger logger = LogManager.getLogger(MigrationCallable.class);

    public MigrationCallable(EntityPersister entityPersister, T csvEntity) {
        this.entityPersister = entityPersister;
        this.csvEntity = csvEntity;
    }

    @Override
    public RowResult<T> call() throws Exception {
        try {
            return new RowResult<>(csvEntity, entityPersister.persist(csvEntity));
        } catch (Exception e) {
            logger.error("failed while persisting. Record - " + StringUtils.join(csvEntity.getOriginalRow().toArray()));
            throw new MigrationException(e);
        }
    }
}