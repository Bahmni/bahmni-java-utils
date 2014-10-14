package org.bahmni.csv;

import org.bahmni.csv.annotation.CSVHeader;

public class DummyCSVEntity extends CSVEntity {
    @CSVHeader(name = "id")
    public String id;
    @CSVHeader(name = "name")
    public String name;

    public DummyCSVEntity() {}

    public DummyCSVEntity(String id, String name) {
        this.id = id;
        this.name = name;
        originalRow(new String[] {id, name});
    }
}
