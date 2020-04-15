package org.bahmni.form2.model;

import java.util.ArrayList;
import java.util.List;

public class Concept {
    private String name;
    //TODO: uuid can be removed
    private String uuid;
    private String datatype;
    private List<ConceptAnswer> answers = new ArrayList<>();

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ConceptAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ConceptAnswer> answers) {
        this.answers = answers;
    }
}
