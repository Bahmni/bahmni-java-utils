package org.bahmni.csv;

class AllPassEntityPersister implements EntityPersister<DummyCSVEntity> {

    @Override
    public Messages persist(DummyCSVEntity csvEntity) {
        return new Messages();
    }

    @Override
    public Messages validate(DummyCSVEntity csvEntity) {
        return new Messages();
    }
}

class ValidationFailedEntityPersister implements EntityPersister<DummyCSVEntity> {
    private String message;

    ValidationFailedEntityPersister(String message) {
        this.message = message;
    }

    @Override
    public Messages persist(DummyCSVEntity csvEntity) {
        return new Messages();
    }

    @Override
    public Messages validate(DummyCSVEntity csvEntity) {
        return new Messages(message);
    }
}

class MigrationFailedEntityPersister implements EntityPersister<DummyCSVEntity> {
    private Exception e;

    public MigrationFailedEntityPersister(Exception e) {
        this.e = e;
    }

    @Override
    public Messages persist(DummyCSVEntity csvEntity) {
        return new Messages(e);
    }

    @Override
    public Messages validate(DummyCSVEntity csvEntity) {
        return new Messages();
    }
}
