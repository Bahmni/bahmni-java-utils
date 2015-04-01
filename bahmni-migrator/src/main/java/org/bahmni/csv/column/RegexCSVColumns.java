package org.bahmni.csv.column;

import org.bahmni.csv.annotation.CSVRegexHeader;
import org.bahmni.csv.KeyValue;
import org.bahmni.csv.exception.MigrationException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegexCSVColumns {
    private String[] headerNames;

    public RegexCSVColumns(String[] headerNames) {
        this.headerNames = headerNames;
    }

    public void setValue(Object entity, Field field, String[] aRow) throws IllegalAccessException {
        List<KeyValue> values = new ArrayList<>();
        CSVRegexHeader annotation = field.getAnnotation(CSVRegexHeader.class);
        String regexPattern = annotation.pattern();

        for(String headerName: getMatchingHeaders(headerNames, regexPattern)){
            values.addAll(createKeyValue(headerName, regexPattern, aRow));
        }

        field.setAccessible(true);
        Collections.sort(values);
        field.set(entity, values);
    }

    private Set<String> getMatchingHeaders(String[] headerNames, String regexPattern) {
        Set<String> matchingHeaders = new HashSet<>();
        for (String headerName : headerNames) {
            if (headerName.matches(regexPattern) && !matchingHeaders.contains(headerName)) {
                matchingHeaders.add(headerName);
            }
        }
        return matchingHeaders;
    }


    private List<KeyValue> createKeyValue(String headerName, String regexPattern, String[] aRow){
        List<KeyValue> values = new ArrayList<>();

        for(int i =0; i < headerNames.length;i++){
            String name = headerNames[i];
            if(name.toLowerCase().equals(headerName.toLowerCase())){
                KeyValue keyValue = new KeyValue();
                keyValue.setKey(headerName.replace(regexPattern.replace("*", ""), ""));
                keyValue.setValue(aRow[i]);
                values.add(keyValue);
            }
        }

        if(values.isEmpty()){
            throw new MigrationException("No Column found in the csv file. " + headerName);
        }

        return values;
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
