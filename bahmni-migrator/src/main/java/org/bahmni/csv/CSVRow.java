/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


package org.bahmni.csv;

import org.bahmni.csv.column.CSVColumns;

import java.lang.reflect.Field;

public class CSVRow<T extends CSVEntity> {
    private final CSVColumns columns;
    private final Class<T> entityClass;

    public CSVRow(CSVColumns columns, Class<T> entityClass) {
        this.columns = columns;
        this.entityClass = entityClass;
    }

    public T getEntity(String[] aRow) throws IllegalAccessException, InstantiationException {
        if (aRow == null)
            return null;

        T entity = entityClass.newInstance();

        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            columns.setValue(entity, field, aRow);
        }
        entity.originalRow(aRow);
        return entity;
    }
}
