package org.bahmni.csv.column;

import org.bahmni.csv.DummyCSVEntity;
import org.bahmni.csv.KeyValue;
import org.bahmni.csv.RegexCSVEntity;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RegexCSVColumnsTest {

    @Test
    public void ensure_columns_populated_with_nonrepeating_headers() throws NoSuchFieldException, IllegalAccessException {
        RegexCSVEntity csvEntity = new RegexCSVEntity();

        RegexCSVColumns columns = new RegexCSVColumns(new String[]{"id","name","Obs.1","Obs.2","Obs.3"});
        columns.setValue(csvEntity,csvEntity.getClass().getDeclaredField("observations"),new String[]{"123","firstname","Sitting","Supine","Posture"});

        assertEquals(3,csvEntity.observations.size());
        assertTrue(csvEntity.observations.contains(new KeyValue("1","Sitting")));
        assertTrue(csvEntity.observations.contains(new KeyValue("2","Supine")));
        assertTrue(csvEntity.observations.contains(new KeyValue("3","Posture")));
    }


    @Test
    public void ensure_columns_populated_with_repeating_headers() throws NoSuchFieldException, IllegalAccessException {
        RegexCSVEntity csvEntity = new RegexCSVEntity();

        RegexCSVColumns columns = new RegexCSVColumns(new String[]{"id","name","Obs.1","Obs.2","Obs.2"});
        columns.setValue(csvEntity,csvEntity.getClass().getDeclaredField("observations"),new String[]{"123","firstname","Sitting","Supine","Posture"});

        assertEquals(3,csvEntity.observations.size());

        Iterator<KeyValue> iterator = csvEntity.observations.iterator();
        while(iterator.hasNext()){
            KeyValue keyValue = iterator.next();

        }

        assertTrue(csvEntity.observations.contains(new KeyValue("1","Sitting")));
        assertTrue(csvEntity.observations.contains(new KeyValue("2","Supine")));
        assertTrue(csvEntity.observations.contains(new KeyValue("2","Posture")));
    }
}
