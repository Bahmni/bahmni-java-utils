package org.bahmni.csv.column;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;
import org.bahmni.csv.annotation.CSVRepeatingHeaders;
import org.bahmni.csv.annotation.CSVRepeatingRegexHeaders;
import org.bahmni.csv.exception.MigrationException;

import java.lang.reflect.Field;

public class CSVColumns<T extends CSVEntity> {
    private final String[] headerNames;

    public CSVColumns(String[] headerNames) {
        this.headerNames = headerNames;
    }

    public void setValue(Object entity, Field field, String[] aRow) throws IllegalAccessException, InstantiationException {
        if (field.getAnnotation(CSVRepeatingHeaders.class) != null) {
            new RepeatingCSVColumns(headerNames).setValue(entity, field, aRow);
        } else if (field.getAnnotation(CSVHeader.class) != null) {
            CSVHeader annotation = field.getAnnotation(CSVHeader.class);
            addColumnValue(entity, field, aRow);
        } else if (field.getAnnotation(CSVRegexHeader.class) != null) {
            new RegexCSVColumns(headerNames).setValue(entity, field, aRow);
        } else if (field.getAnnotation(CSVRepeatingRegexHeaders.class) != null) {
            new RepeatingRegexCSVColumns(headerNames).setValue(entity, field, aRow);
        }
    }

    private void addColumnValue(Object entity, Field field, String[] aRow) throws IllegalAccessException {
        CSVHeader headerAnnotation = field.getAnnotation(CSVHeader.class);
        field.setAccessible(true);
        int position = getPosition(headerAnnotation.name(), 0);
        if (headerColumnNotFoundForOptionalColumn(headerAnnotation, position))
            return;

        if (headerColumnNotFoundForMandatoryColumn(headerAnnotation, position))
            throw new MigrationException("No Column found in the csv file. " + headerAnnotation.name());

        if (headerColumnValueNotFoundForMandatoryColumn(aRow,position))
            throw new MigrationException("No Value found in the csv file for the column " + headerAnnotation.name());

        String value = aRow[position];
        field.set(entity, value != null ? value.trim() : value);
    }

    private boolean headerColumnNotFoundForMandatoryColumn(CSVHeader headerAnnotation, int position) {
        return position < 0 && !headerAnnotation.optional();
    }

    private boolean headerColumnValueNotFoundForMandatoryColumn(String[] row, int position) {
        return position < 0 || position >= row.length;
    }

    private boolean headerColumnNotFoundForOptionalColumn(CSVHeader headerAnnotation, int position) {
        return position < 0 && headerAnnotation.optional();
    }

    protected int getPosition(String headerValueInClass, int startIndex) {
        for (int i = startIndex; i < headerNames.length; i++) {
            String headerName = headerNames[i];
            if (headerName.toLowerCase().startsWith(headerValueInClass.toLowerCase()))
                return i;
        }
        return -1;
    }
}
