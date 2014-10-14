package org.bahmni.fileexport;

import org.bahmni.csv.DummyCSVEntity;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class FileExporterTest {

    private FileExporter<DummyCSVEntity> fileExporter;

    @Before
    public void setUp() throws Exception {
        fileExporter = new FileExporter<>();
    }

    @Test
    public void shouldExportCSV() throws Exception {
        ArrayList<DummyCSVEntity> dummyCSVEntities = new ArrayList<>();
        dummyCSVEntities.add(new DummyCSVEntity("id1", "name1"));
        dummyCSVEntities.add(new DummyCSVEntity("id2", "name2"));
        dummyCSVEntities.add(new DummyCSVEntity("id3", "name3"));
        dummyCSVEntities.add(new DummyCSVEntity("id4", "name4"));
        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream outputStream = fileExporter.exportCSV(dummyCSVEntities, zipOutputStream);
        outputStream.close();
        assertEquals("\"id1\",\"name1\"\n" +
                "\"id2\",\"name2\"\n" +
                "\"id3\",\"name3\"\n" +
                "\"id4\",\"name4\"\n", outputStream.toString());
    }
}