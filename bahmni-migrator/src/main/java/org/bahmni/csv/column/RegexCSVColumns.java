package org.bahmni.csv.column;

import org.bahmni.csv.annotation.CSVRegexHeader;
import org.bahmni.csv.KeyValue;
import org.bahmni.csv.exception.MigrationException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RegexCSVColumns {
    private String[] headerNames;

    public RegexCSVColumns(String[] headerNames) {
        this.headerNames = headerNames;
    }

    public void setValue(Object entity, Field field, String[] aRow) throws IllegalAccessException {
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

    protected int getPosition(String headerValueInClass, int startIndex) {
        for (int i = startIndex; i < headerNames.length; i++) {
            String headerName = headerNames[i];
            if (headerName.toLowerCase().startsWith(headerValueInClass.toLowerCase()))
                return i;
        }
        throw new MigrationException("No Column found in the csv file. " + headerValueInClass);
    }

}
