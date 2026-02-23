/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


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
