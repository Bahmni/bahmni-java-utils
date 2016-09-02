package org.bahmni.csv;

import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;
import org.bahmni.csv.annotation.CSVRepeatingHeaders;
import org.bahmni.csv.annotation.CSVRepeatingRegexHeaders;
import org.bahmni.csv.column.CSVColumns;
import org.bahmni.csv.exception.MigrationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    public void optional_columns_need_not_have_values() throws InstantiationException, IllegalAccessException {
        String[] headerRows = new String[]{"id", "name"};
        String[] aRow = {"1", "bahmniUser"};
        CSVRow<DummyCSVEntity> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntity.class);
        DummyCSVEntity aDummyEntity = entityCSVRow.getEntity(aRow);
        assertEquals("bahmniUser", aDummyEntity.name);
        assertEquals("1", aDummyEntity.id);
        assertNull("caste is an optional header and is defaulted to null", aDummyEntity.caste);
    }

    @Test
    public void optional_columns_values_are_set_on_csvEntity() throws InstantiationException, IllegalAccessException {
        String[] headerRows = new String[]{"id", "name", "caste"};
        String[] aRow = {"1", "bahmniUser", "human"};
        CSVRow<DummyCSVEntity> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntity.class);
        DummyCSVEntity aDummyEntity = entityCSVRow.getEntity(aRow);
        assertEquals("bahmniUser", aDummyEntity.name);
        assertEquals("human", aDummyEntity.caste);
    }

    @Test
    public void throws_exception_for_missing_header_for_mandatory_column() throws Exception {
        String[] headerRows = new String[]{"id"};
        String[] aRow = new String[]{"1"};
        expectedException.expect(MigrationException.class);
        expectedException.expectMessage("No Column found in the csv file.");
        CSVRow<DummyCSVEntity> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntity.class);
        DummyCSVEntity aDummyEntity = entityCSVRow.getEntity(aRow);
    }

    @Test
    public void throws_exception_for_missing_value_for_mandatory_column() throws Exception {
        String[] headerRows = new String[]{"id"};
        String[] aRow = new String[]{};
        expectedException.expect(MigrationException.class);
        expectedException.expectMessage("No Value found in the csv file for the column");
        CSVRow<DummyCSVEntity> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntity.class);
        DummyCSVEntity aDummyEntity = entityCSVRow.getEntity(aRow);
    }
    
    @Test
    public void trim_non_null_values() throws InstantiationException, IllegalAccessException {
        String[] headerRows = new String[]{"id", "name"};
        String[] aRow = {"1", "     bahmniUser      "};
        CSVRow<DummyCSVEntity> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntity.class);
        DummyCSVEntity aDummyEntity = entityCSVRow.getEntity(aRow);
        assertEquals("bahmniUser", aDummyEntity.name);
    }

    @Test
    public void trim_with_empty_value() throws InstantiationException, IllegalAccessException {
        String[] headerRows = new String[]{"id", "name"};
        String[] aRow = {"1", ""};
        CSVRow<DummyCSVEntity> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntity.class);
        DummyCSVEntity aDummyEntity = entityCSVRow.getEntity(aRow);
        assertEquals("", aDummyEntity.name);
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
        String[] headerRows = new String[]{"Id", "some-key", "Value", "some-key", "Value", "Name"};
        String[] aRow = new String[]{"1", "key1", "Value1", "key2", "Value2", "bahmniUser"};
        CSVRow<DummyCSVEntityWithRepeatingValues> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntityWithRepeatingValues.class);
        DummyCSVEntityWithRepeatingValues aDummyEntity = entityCSVRow.getEntity(aRow);

        assertEquals("1", aDummyEntity.id);
        assertEquals("bahmniUser", aDummyEntity.name);
        assertEquals(2, aDummyEntity.keyValues.size());
        assertEquals(new DummyKeyValue("key1", "Value1"), aDummyEntity.keyValues.get(0));
        assertEquals(new DummyKeyValue("key2", "Value2"), aDummyEntity.keyValues.get(1));
    }

    @Test
    public void parse_a_row_without_repeating_header_column_for_class_containing_repeating_header_fields() throws Exception {
        String[] headerRows = new String[]{"Id", "Name"};
        String[] aRow = new String[]{"1", "bahmniUser"};
        CSVRow<DummyCSVEntityWithRepeatingValues> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntityWithRepeatingValues.class);
        DummyCSVEntityWithRepeatingValues aDummyEntity = entityCSVRow.getEntity(aRow);

        assertEquals(0, aDummyEntity.keyValues.size());
        assertEquals("1", aDummyEntity.id);
        assertEquals("bahmniUser", aDummyEntity.name);
    }

    @Test
    public void parse_a_row_with_repeating_regex_header_columns() throws Exception {
        String[] headerRows = new String[]{"id", "name", "obs.HEIGHT", "obs.WEIGHT", "diagnosis.diagnosis1", "patient.caste", "diagnosis.diagnosis2"};
        String[] aRow = new String[]{"1", "bahmniUser", "178", "92", "Cancer", "HUMAN", "Tubercolosis"};
        CSVRow<DummyCSVEntityWithRegex> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntityWithRegex.class);
        DummyCSVEntityWithRegex aDummyEntity = entityCSVRow.getEntity(aRow);

        assertEquals(2, aDummyEntity.observations.size());
        aDummyEntity.observations.contains(new KeyValue("HEIGHT", "178"));
        aDummyEntity.observations.contains(new KeyValue("WEIGHT", "92"));

        assertEquals(1, aDummyEntity.patientAttributes.size());
        assertEquals(new KeyValue("caste", "HUMAN"), aDummyEntity.patientAttributes.get(0));

        assertEquals(2, aDummyEntity.diagnoses.size());
        assertEquals(new KeyValue("diagnosis1", "Cancer"), aDummyEntity.diagnoses.get(0));
        assertEquals(new KeyValue("diagnosis2", "Tubercolosis"), aDummyEntity.diagnoses.get(1));
    }

    @Test
    public void parse_a_row_with_combination_of_repeating_and_regex_header_columns() throws Exception {
        String[] headerRows = new String[]{"Serial Number", "registrationDate", "patient.id", "patient.name", "Repeat.1.encounterDate", "Repeat.1.obs.HEIGHT", "Repeat.1.obs.WEIGHT", "Repeat.1.diagnosis.diagnosis1", "Repeat.1.diagnosis.diagnosis2", "Repeat.2.encounterDate", "Repeat.2.obs.Pulse"};
        String[] aRow = new String[]{"1", "20/4/1977", "GAN200000", "Dummy Junior", "20/5/2001", "178", "92", "Cancer", "Malaria", "25/5/2005", "72"};

        CSVRow<DummyCSVEntityWithRegexRepeatingValues> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntityWithRegexRepeatingValues.class);
        DummyCSVEntityWithRegexRepeatingValues aDummyEntity = entityCSVRow.getEntity(aRow);

        assertEquals("1", aDummyEntity.serialNumber);
        assertEquals("20/4/1977", aDummyEntity.registrationDate);

        assertEquals(2, aDummyEntity.patientAttributes.size());

        assertEquals(new KeyValue("id", "GAN200000"), aDummyEntity.patientAttributes.get(0));
        assertEquals(new KeyValue("name", "Dummy Junior"), aDummyEntity.patientAttributes.get(1));

        assertEquals(2, aDummyEntity.repeatingRegExes.size());
        assertEquals(2, aDummyEntity.repeatingRegExes.get(0).observations.size());
        assertEquals("20/5/2001", aDummyEntity.repeatingRegExes.get(0).encounterDate);

        assertTrue(aDummyEntity.repeatingRegExes.get(0).observations.contains(new KeyValue("HEIGHT", "178")));
        assertTrue(aDummyEntity.repeatingRegExes.get(0).observations.contains(new KeyValue("WEIGHT", "92")));
        assertTrue(aDummyEntity.repeatingRegExes.get(0).diagnoses.contains(new KeyValue("diagnosis1", "Cancer")));
        assertTrue(aDummyEntity.repeatingRegExes.get(0).diagnoses.contains(new KeyValue("diagnosis2", "Malaria")));

        assertEquals(1, aDummyEntity.repeatingRegExes.get(1).observations.size());
        assertEquals("25/5/2005", aDummyEntity.repeatingRegExes.get(1).encounterDate);
        assertTrue(aDummyEntity.repeatingRegExes.get(1).observations.contains(new KeyValue("Pulse", "72")));
    }

    @Test
    public void no_repeat_headers() throws Exception {
        String[] headerRows = new String[]{"Serial Number", "registrationDate", "encounterDate", "obs.HEIGHT", "obs.WEIGHT", "diagnosis.diagnosis1", "diagnosis.diagnosis2"};
        String[] aRow = new String[]{"1", "bahmniUser", "20/5/2001", "178", "92", "Cancer", "Malaria", "25/5/2005", "72"};

        CSVRow<DummyCSVEntityWithRegexRepeatingValues> entityCSVRow = new CSVRow<>(new CSVColumns(headerRows), DummyCSVEntityWithRegexRepeatingValues.class);
        DummyCSVEntityWithRegexRepeatingValues aDummyEntity = entityCSVRow.getEntity(aRow);

        assertEquals(0, aDummyEntity.repeatingRegExes.size());
    }

    public static class DummyCSVEntityWithRepeatingValues extends CSVEntity {
        @CSVHeader(name = "id")
        public String id;
        @CSVHeader(name = "name")
        public String name;
        @CSVRepeatingHeaders(names = {"some-key", "value"}, type = DummyKeyValue.class)
        public List<DummyKeyValue> keyValues;

        public DummyCSVEntityWithRepeatingValues() {
        }
    }

    public static class DummyCSVEntityWithRegexRepeatingValues extends CSVEntity {
        @CSVHeader(name = "Serial Number")
        public String serialNumber;

        @CSVHeader(name = "registrationDate")
        public String registrationDate;

        @CSVRegexHeader(pattern = "patient.*")
        public List<KeyValue> patientAttributes;

        @CSVRepeatingRegexHeaders(type=DummyRepeatingRegEx.class)
        public List<DummyRepeatingRegEx> repeatingRegExes;
    }

    public static class DummyRepeatingRegEx extends CSVEntity {
        @CSVHeader(name = "encounterDate")
        public String encounterDate;

        @CSVRegexHeader(pattern = "obs.*")
        public java.util.List<KeyValue> observations;

        @CSVRegexHeader(pattern="diagnosis.*")
        public List<KeyValue> diagnoses;

        public DummyRepeatingRegEx() {
        }
    }

    public static class DummyCSVEntityWithRegex extends CSVEntity {
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

        public DummyCSVEntityWithRegex() {
        }
    }

    public static class DummyKeyValue {
        @CSVHeader(name = "some-key")
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

