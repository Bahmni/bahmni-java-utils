package org.bahmni.csv.column;

import org.bahmni.csv.DummyCSVEntity;

import static org.junit.Assert.assertEquals;

public class CSVColumnTest {

    public void ensure_columns_populated_with_repeating_headers() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        DummyCSVEntity entity = new DummyCSVEntity();

        CSVColumns<DummyCSVEntity> columns = new CSVColumns<>(new String[]{"id","name","caste","name"});
        columns.setValue(entity,entity.getClass().getDeclaredField("name"),new String[]{"123","firstname","somecaste","lastname"});

        assertEquals(entity.name, "firstname");
    }
}
