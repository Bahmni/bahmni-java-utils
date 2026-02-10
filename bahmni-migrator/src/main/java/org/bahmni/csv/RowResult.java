/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. Bahmni is also distributed under
 * the terms of the Healthcare Disclaimer located at https://www.bahmni.org/license
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */


package org.bahmni.csv;

import org.apache.commons.lang.StringUtils;

public class RowResult<T extends CSVEntity> {
    private Messages messages;
    private T csvEntity;

    public RowResult(T csvEntity) {
        this(csvEntity, (Messages) null);
    }

    public RowResult(T csvEntity, Throwable exception) {
        this.csvEntity = csvEntity;
        this.messages = new Messages(getInnerErrorMessage(exception));
    }

    public RowResult(T csvEntity, String errorMessage) {
        this(csvEntity, new Messages(errorMessage));
    }

    public RowResult(T csvEntity, Messages errorMessage) {
        this.csvEntity = csvEntity;
        this.messages = errorMessage;
    }

    public boolean isSuccessful() {
        return getMessages().isEmpty();
    }

    public String[] getRowWithErrorColumn() {
        if (csvEntity == null)
            return new String[]{};

        return csvEntity.getRowWithErrorColumn(messages.asString());
    }

    public String getRowWithErrorColumnAsString() {
        return StringUtils.join(getRowWithErrorColumn(), ",");
    }

    private Messages getMessages() {
        if (this.messages == null) this.messages = new Messages();
        return this.messages;
    }

    private String getInnerErrorMessage(Throwable exception) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(exception.getMessage());

        Throwable innerException = null;
        while ((innerException = exception.getCause()) != null) {
            errorMessage.append(" - ").append(innerException.getMessage());
            exception = innerException;
        }
        return errorMessage.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RowResult rowResult = (RowResult) o;

        if (csvEntity != null ? !csvEntity.equals(rowResult.csvEntity) : rowResult.csvEntity != null) return false;
        if (messages != null ? !messages.equals(rowResult.messages) : rowResult.messages != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = messages != null ? messages.hashCode() : 0;
        result = 31 * result + (csvEntity != null ? csvEntity.hashCode() : 0);
        return result;
    }

    public T getCsvEntity() {
        return csvEntity;
    }
}
