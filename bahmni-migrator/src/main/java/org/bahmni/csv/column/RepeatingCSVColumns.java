package org.bahmni.csv.column;

import org.bahmni.csv.annotation.CSVRepeatingHeaders;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RepeatingCSVColumns {
    private final String[] headerNames;

    public RepeatingCSVColumns(String[] headerNames) {
        this.headerNames = headerNames;
    }

    public void setValue(Object entity, Field field, String[] aRow) throws IllegalAccessException, InstantiationException {
        List values = new ArrayList<>();
        CSVRepeatingHeaders annotation = field.getAnnotation(CSVRepeatingHeaders.class);
        String[] repeatingHeaderColumnNames = annotation.names();
        Class valueClassType = annotation.type();

        Integer currentHeaderPosition = getPosition(repeatingHeaderColumnNames[0], 0);
        Integer endPosition = getEndPosition(repeatingHeaderColumnNames[repeatingHeaderColumnNames.length - 1]);
        while (currentHeaderPosition != null && endPosition != null && currentHeaderPosition <= endPosition) {
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

    protected Integer getPosition(String headerValueInClass, int startIndex) {
        for (int i = startIndex; i < headerNames.length; i++) {
            String headerName = headerNames[i];
            if (headerName.toLowerCase().startsWith(headerValueInClass.toLowerCase())) {
                return i;
            }
        }
        return null;
    }

    protected Integer getEndPosition(String headerValueInClass) {
        for (int i = headerNames.length - 1; i >= 0; i--) {
            String headerName = headerNames[i];
            if (headerName.toLowerCase().startsWith(headerValueInClass.toLowerCase())) {
                return i;
            }
        }
        return null;
    }
}
