package org.bahmni.csv;

import org.bahmni.csv.exception.MigrationException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

class CSVColumns<T extends CSVEntity> {
    private final String[] headerNames;

    public CSVColumns(String[] headerNames) {
        this.headerNames = headerNames;
    }

    public void setValue(T entity, Field field, String[] aRow) throws IllegalAccessException, InstantiationException {
        if (field.getAnnotation(CSVRepeatingHeaders.class) != null) {
            addRepeatingColumnValues(entity, field, aRow);
        } else if (field.getAnnotation(CSVHeader.class) != null) {
            addColumnValue(entity, field, aRow);
        }
    }

    private void addColumnValue(T entity, Field field, String[] aRow) throws IllegalAccessException {
        CSVHeader headerAnnotation = field.getAnnotation(CSVHeader.class);
        field.setAccessible(true);
        field.set(entity, aRow[getPosition(headerAnnotation.name(), 0)]);
    }

    private void addRepeatingColumnValues(T entity, Field field, String[] aRow) throws IllegalAccessException, InstantiationException {
        List values = new ArrayList<>();
        CSVRepeatingHeaders annotation = field.getAnnotation(CSVRepeatingHeaders.class);
        String[] repeatingHeaderColumnNames = annotation.names();
        Class valueClassType = annotation.type();

        int currentHeaderPosition = getPosition(repeatingHeaderColumnNames[0], 0);
        while (currentHeaderPosition < this.headerNames.length) {
            Object value = valueClassType.newInstance();
            Field[] valueFields = valueClassType.getDeclaredFields();
            for (Field valueField : valueFields) {
                valueField.setAccessible(true);
                valueField.set(value, aRow[getPosition(valueField.getName(), currentHeaderPosition)]);
            }
            values.add(value);
            currentHeaderPosition += valueFields.length;
        }
        field.setAccessible(true);
        field.set(entity, values);
    }

    private int getPosition(String headerValueInClass, int startIndex) {
        for (int i = startIndex; i < headerNames.length; i++) {
            String headerName = headerNames[i];
            if (headerName.toLowerCase().startsWith(headerValueInClass.toLowerCase()))
                return i;
        }
        throw new MigrationException("No Column found in the csv file. " + headerValueInClass);
    }
}
