package org.bahmni.csv;

import org.bahmni.csv.exception.MigrationException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertTrue;

public class CSVFileTest {
    @Test(expected = MigrationException.class)
    public void cannot_read_without_opening_the_file() throws IllegalAccessException, IOException, InstantiationException {
        CSVFile<DummyCSVEntity> dummyCSVEntityCSVFile = new CSVFile<>(".", "invalidFile.csv");
        dummyCSVEntityCSVFile.readEntity(DummyCSVEntity.class);
    }

    @Test(expected = MigrationException.class)
    public void open_throws_MigrationException_if_file_does_not_exist() throws IllegalAccessException, IOException, InstantiationException {
        CSVFile<DummyCSVEntity> dummyCSVEntityCSVFile = new CSVFile<>(".", "invalidFile.csv");
        dummyCSVEntityCSVFile.openForRead();
    }
}
