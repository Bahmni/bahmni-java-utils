package org.bahmni.csv;

import org.bahmni.csv.exception.MigrationException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CSVColumns<T extends CSVEntity> {
    public static final String REPEAT = "Repeat.";
    private final String[] headerNames;

    public CSVColumns(String[] headerNames) {
        this.headerNames = headerNames;
    }

    public void setValue(Object entity, Field field, String[] aRow) throws IllegalAccessException, InstantiationException {
        if (field.getAnnotation(CSVRepeatingHeaders.class) != null) {
            addRepeatingColumnValues(entity, field, aRow);
        } else if (field.getAnnotation(CSVHeader.class) != null) {
            addColumnValue(entity, field, aRow);
        } else if (field.getAnnotation(CSVRegexHeader.class) != null) {
            addRegexColumnValue(entity, field, aRow);
        } else if (field.getAnnotation(CSVRepeatingRegexHeaders.class) != null) {
            addRepeatingRegexColumnValue(entity, field, aRow);
        }
    }

    private void addRepeatingRegexColumnValue(Object parentEntity, Field parentField, String[] aRow) throws IllegalAccessException, InstantiationException {
        CSVRepeatingRegexHeaders repeatingRegexAnnotation = parentField.getAnnotation(CSVRepeatingRegexHeaders.class);
        Class childClass = repeatingRegexAnnotation.type();

        int counter = 1;
        List childEntities = new ArrayList();

        while (true) {
            List<String> childHeaders = findHeadersFor(headerNames, counter);
            if (childHeaders.isEmpty())
                break;

            String firstChildHeaderName = childHeaders.get(0);
            String lastChildHeaderName = childHeaders.get(childHeaders.size() - 1);
            int firstChildIndex = getIndexFor(headerNames, counter, firstChildHeaderName);
            int lastChildIndex = getIndexFor(headerNames, counter, lastChildHeaderName);

            List<String> childRowValues = Arrays.asList(aRow).subList(firstChildIndex, lastChildIndex + 1);

            CSVEntity childEntity = (CSVEntity) childClass.newInstance();
            CSVColumns<CSVEntity> childColumns = new CSVColumns<>(childHeaders.toArray(new String[]{}));

            Field[] childFields = childClass.getDeclaredFields();
            for (Field childField : childFields) {
                childColumns.setValue(childEntity, childField, childRowValues.toArray(new String[]{}));
            }

            childEntities.add(childEntity);
            counter++;
        }

        parentField.setAccessible(true);
        parentField.set(parentEntity, childEntities);
    }

    private int getIndexFor(String[] headerNames, int counter, String firstChildHeaderName) {
        int index = 0;
        for (String headerName : headerNames) {
            if (headerName.equalsIgnoreCase(REPEAT + counter + "." + firstChildHeaderName)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private List<String> findHeadersFor(String[] headerNames, int counter) {
        List<String> childHeaderNames = new ArrayList<>();
        for (String headerName : headerNames) {
            String s = REPEAT + counter;
            if (headerName.toLowerCase().contains(s.toLowerCase())) {
                String childHeaderName = headerName.replace(REPEAT + counter + ".", "");
                childHeaderNames.add(childHeaderName);
            }
        }
        return childHeaderNames;
    }

    private void addRegexColumnValue(Object entity, Field field, String[] aRow) throws IllegalAccessException {
        List values = new ArrayList<>();
        CSVRegexHeader annotation = field.getAnnotation(CSVRegexHeader.class);
        String regexPattern = annotation.pattern();

        List<String> matchingHeaders = getMatchingHeaders(this.headerNames, regexPattern);
        for (String matchingHeader : matchingHeaders) {
            KeyValue keyValue = new KeyValue();
            keyValue.setKey(matchingHeader.replace(regexPattern.replace("*", ""), ""));
            keyValue.setValue(aRow[getPosition(matchingHeader, 0)]);
            values.add(keyValue);
        }
        field.setAccessible(true);
        field.set(entity, values);

    }

    private List<String> getMatchingHeaders(String[] headerNames, String regexPattern) {
        List<String> matchingHeaders = new ArrayList<>();
        for (String headerName : headerNames) {
            if (headerName.matches(regexPattern) && !matchingHeaders.contains(headerName)) {
                matchingHeaders.add(headerName);
            } else if (matchingHeaders.contains(headerName)) {
                throw new MigrationException(String.format("A header by name '%s' already exists. Header names must be unique", headerName));
            }
        }
        return matchingHeaders;
    }

    private void addColumnValue(Object entity, Field field, String[] aRow) throws IllegalAccessException {
        CSVHeader headerAnnotation = field.getAnnotation(CSVHeader.class);
        field.setAccessible(true);
        field.set(entity, aRow[getPosition(headerAnnotation.name(), 0)]);
    }

    private void addRepeatingColumnValues(Object entity, Field field, String[] aRow) throws IllegalAccessException, InstantiationException {
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
