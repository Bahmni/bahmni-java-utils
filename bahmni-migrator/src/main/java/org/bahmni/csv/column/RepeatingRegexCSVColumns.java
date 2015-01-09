package org.bahmni.csv.column;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVRepeatingRegexHeaders;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RepeatingRegexCSVColumns {
    public static final String REPEAT = "Repeat.";

    private String[] headerNames;

    public RepeatingRegexCSVColumns(String[] headerNames) {
        this.headerNames = headerNames;
    }

    public void setValue(Object parentEntity, Field parentField, String[] aRow) throws IllegalAccessException, InstantiationException {
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
            int firstChildIndex = getFirstIndexFor(headerNames, counter, firstChildHeaderName);
            int lastChildIndex = getLastIndexFor(headerNames, counter, lastChildHeaderName);

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

    private int getFirstIndexFor(String[] headerNames, int counter, String firstChildHeaderName) {
        int index = 0;
        for (String headerName : headerNames) {
            if (headerName.equalsIgnoreCase(REPEAT + counter + "." + firstChildHeaderName)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private int getLastIndexFor(String[] headerNames, int counter, String lastChildHeaderName) {
        int selectedIndex = -1;
        int index = 0;
        for (String headerName : headerNames) {
            if (headerName.equalsIgnoreCase(REPEAT + counter + "." + lastChildHeaderName)) {
                selectedIndex  = index;
            }
            index++;
        }
        return selectedIndex;
    }


}
