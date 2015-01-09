package org.bahmni.csv;

import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;

import java.util.List;

public class RegexCSVEntity extends CSVEntity{

    @CSVHeader(name = "id"  )
    public String id;
    @CSVHeader(name = "name")
    public String name;
    @CSVRegexHeader(pattern="Obs.*")
    public List<KeyValue> observations;

    public RegexCSVEntity() {}

    public RegexCSVEntity(String id, String name) {
        this.id = id;
        this.name = name;
        originalRow(new String[] {id, name});
    }
}
