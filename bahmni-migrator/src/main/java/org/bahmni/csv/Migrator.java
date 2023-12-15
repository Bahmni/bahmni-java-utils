package org.bahmni.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bahmni.csv.exception.MigrationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

// Assumption - if you use multithreading, there should be no dependency between the records in the file.
public class Migrator<T extends CSVEntity> {
    private Class<T> entityClass;
    private boolean abortIfValidationFails = true;
    private Stages<T> allStages;
    private final EntityPersister entityPersister;

    private static Logger logger = LoggerFactory.getLogger(Migrator.class);

    public Migrator(EntityPersister entityPersister, Stages allStages, Class<T> entityClass) {
        this(entityPersister, allStages, entityClass, true);
    }

    public Migrator(EntityPersister entityPersister, Stages allStages, Class<T> entityClass, boolean abortIfValidationFails) {
        this.entityPersister = entityPersister;
        this.allStages = allStages;
        this.entityClass = entityClass;
        this.abortIfValidationFails = abortIfValidationFails;
    }

    public MigrateResult<T> migrate() {
        MigrateResult<T> stageResult = null;
        try {
            while (allStages.hasMoreStages()) {
                stageResult = allStages.nextStage().run(entityPersister, this.entityClass);
                if (abortIfValidationFails && stageResult.hasFailed()) {
                    return stageResult;
                }
                logger.info(">>> {}", stageResult);
            }
        } catch(MigrationException e) {
            logger.error(getStackTrace(e));
            throw e;
        } catch (Exception e) {
            logger.error(getStackTrace(e));
            if( e.toString().startsWith("java.lang") && allStages.getStages() != null && allStages.getStages().size()>=0){
                int failedRowNumber = allStages.getStages().get(0).getFailedRowNumber();
                throw new MigrationException("One or more column values missing or incorrect in row number " + failedRowNumber);
            }else {
                throw new MigrationException(getStackTrace(e), e);
            }
        }
        return stageResult;
    }


    private static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public void shutdown() {
        allStages.shutdown();
    }
}
