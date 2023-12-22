package org.bahmni.fileexport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.CSVFile;
import org.bahmni.fileexport.exception.FileExportException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

// External API to start the csv file export.
public class FileExporter<T extends CSVEntity> {
    private static Logger logger = LoggerFactory.getLogger(FileExporter.class);

    public ByteArrayOutputStream exportCSV(List<T> csvEntities, ByteArrayOutputStream outputStream) throws IOException {
        CSVFile<T> csvFile = new CSVFile<T>(outputStream);
        for (T csvEntity : csvEntities) {
            try {
                logger.info("Writing record {}", csvEntity.toString());
                csvFile.writeARecord(csvEntity);
            } catch (IOException e) {
                throw new FileExportException("Cannot create file");
            }
        }
        return csvFile.getOutputStream();
    }


}
