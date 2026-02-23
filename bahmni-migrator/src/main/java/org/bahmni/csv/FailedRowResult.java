/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */


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
