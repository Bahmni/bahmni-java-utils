package org.bahmni.csv;

import org.bahmni.csv.exception.MigrationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static junit.framework.Assert.assertEquals;

public class CSVRowTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void parse_a_row() throws InstantiationException, IllegalAccessException {
        String[] headerRows = new String[]{"id", "name"};
        String[] aRow = {"1", "bahmniUser"};
        CSVRow<DummyCSVEntity> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntity.class);
        DummyCSVEntity aDummyEntity = entityCSVRow.getEntity(aRow);
        assertEquals("bahmniUser", aDummyEntity.name);
        assertEquals("1", aDummyEntity.id);
    }

    @Test
    public void parse_a_row_ignoring_casing() throws InstantiationException, IllegalAccessException {
        String[] headerRows = new String[]{"Id", "NAME"};
        String[] aRow = {"1", "bahmniUser"};
        CSVRow<DummyCSVEntity> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntity.class);
        DummyCSVEntity aDummyEntity = entityCSVRow.getEntity(aRow);
        assertEquals("bahmniUser", aDummyEntity.name);
        assertEquals("1", aDummyEntity.id);
    }

    @Test
    public void parse_a_row_with_repeating_header_columns() throws Exception {
        String[] headerRows = new String[]{"Id", "Name", "key", "Value", "key", "Value"};
        String[] aRow = new String[]{"1", "bahmniUser", "key1", "Value1", "key2", "Value2"};
        CSVRow<DummyCSVEntityWithRepeatingValues> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntityWithRepeatingValues.class);
        DummyCSVEntityWithRepeatingValues aDummyEntity = entityCSVRow.getEntity(aRow);

        assertEquals(2, aDummyEntity.keyValues.size());
        assertEquals(new DummyKeyValue("key1", "Value1"), aDummyEntity.keyValues.get(0));
        assertEquals(new DummyKeyValue("key2", "Value2"), aDummyEntity.keyValues.get(1));
    }

    @Test
    public void parse_a_row_with_repeating_regex_header_columns() throws Exception {
        String[] headerRows = new String[]{"id", "name", "obs.HEIGHT", "obs.WEIGHT", "diagnosis.diagnosis1", "patient.caste", "diagnosis.diagnosis2"};
        String[] aRow = new String[]{"1", "bahmniUser", "178", "92", "Cancer", "HUMAN", "Tubercolosis"};
        CSVRow<DummyCSVEntityWithRegexRepeatingValues> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntityWithRegexRepeatingValues.class);
        DummyCSVEntityWithRegexRepeatingValues aDummyEntity = entityCSVRow.getEntity(aRow);

        assertEquals(2, aDummyEntity.observations.size());
        assertEquals(new KeyValue("HEIGHT", "178"), aDummyEntity.observations.get(0));
        assertEquals(new KeyValue("WEIGHT", "92"), aDummyEntity.observations.get(1));

        assertEquals(1, aDummyEntity.patientAttributes.size());
        assertEquals(new KeyValue("caste", "HUMAN"), aDummyEntity.patientAttributes.get(0));

        assertEquals(2, aDummyEntity.diagnoses.size());
        assertEquals(new KeyValue("diagnosis1", "Cancer"), aDummyEntity.diagnoses.get(0));
        assertEquals(new KeyValue("diagnosis2", "Tubercolosis"), aDummyEntity.diagnoses.get(1));
    }

    @Test
    public void throws_exception_for_non_unique_regex_header_columns() throws Exception {
        String[] headerRows = new String[]{"id", "name", "diagnosis.diagnosis", "diagnosis.diagnosis"};
        String[] aRow = new String[]{"1", "bahmniUser", "Cancer", "Tubercolosis"};
        expectedException.expect(MigrationException.class);
        expectedException.expectMessage("A header by name 'diagnosis.diagnosis' already exists. Header names must be unique");
        CSVRow<DummyCSVEntityWithRegexRepeatingValues> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntityWithRegexRepeatingValues.class);
        DummyCSVEntityWithRegexRepeatingValues aDummyEntity = entityCSVRow.getEntity(aRow);
    }

    private static class DummyCSVEntityWithRepeatingValues extends CSVEntity {
        @CSVHeader(name = "id")
        public String id;
        @CSVHeader(name = "name")
        public String name;
        @CSVRepeatingHeaders(names = {"key", "value"}, type = DummyKeyValue.class)
        public java.util.List<DummyKeyValue> keyValues;

        public DummyCSVEntityWithRepeatingValues() {
        }
    }

    private static class DummyCSVEntityWithRegexRepeatingValues extends CSVEntity {
        @CSVHeader(name = "id")
        public String id;

        @CSVHeader(name = "name")
        public String name;

        @CSVRegexHeader(pattern = "obs.*")
        public java.util.List<KeyValue> observations;

        @CSVRegexHeader(pattern="diagnosis.*")
        public List<KeyValue> diagnoses;

        @CSVRegexHeader(pattern = "patient.*")
        public List<KeyValue> patientAttributes;

        public DummyCSVEntityWithRegexRepeatingValues() {
        }
    }

    private static class DummyKeyValue {
        public String key;
        public String value;

        public DummyKeyValue() {
        }

        public DummyKeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DummyKeyValue that = (DummyKeyValue) o;

            if (key != null ? !key.equals(that.key) : that.key != null) return false;
            if (value != null ? !value.equals(that.value) : that.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }
}

